package com.profitera.services.business.batch;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.QuerySpec;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.financial.TransactionAction;
import com.profitera.services.system.financial.TransactionQuery;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.loan.impl.DefaultKnockoffScheduleManager;
import com.profitera.services.system.loan.impl.IAccountTypeProvider;
import com.profitera.services.system.loan.impl.IKnockoffScheduleManager;
import com.profitera.util.DateParser;

public class PaymentPostingBatch extends
    AbstractFinancialBatch {

  protected static final String PAYMENT_DATE = "PAYMENT_DATE";
  protected static final String PAYMENT_AMOUNT = "AMOUNT";
  protected static final String MQ_TRANSACTION_ID = "TRANSACTION_ID";
  protected static final String[] PAYMENT_QUERY_COLUMNS = new String[]{ACCOUNT_ID, PAYMENT_AMOUNT, PAYMENT_DATE, MQ_TRANSACTION_ID};
  protected static final String PAYMENTQUERY = "paymentquery";
  protected static final String PAYMENTLINKINSERT = "paymentlinkinsert";
  private Account generalPaymentAccount;
  {
    addRequiredProperty(PAYMENTQUERY, String.class, 
        "Query that retrieves payments to post", 
        "Query that with retrieve the payments which are to be posted by this batch, the query has to return:  "
        + "<variablelist>"
        + "<varlistentry><term>" + ACCOUNT_ID + "</term><listitem><para>The loan account to which this payment is linked, as represented in PTRACCOUNT.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + PAYMENT_AMOUNT + "</term><listitem><para>The amount of the payment to be posted.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + PAYMENT_DATE + " (optional)</term><listitem><para>The date of the payment to be posted, ignored for existing transactions and used as the transaction date for payments where it is provided, posting is still dated based on the batch effective date.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + MQ_TRANSACTION_ID + " (optional)</term><listitem><para>The ID from PTRFIN_TRANSACTION where the transaction to be posted already exists.</para></listitem></varlistentry>"
        + "</variablelist>");
    addRequiredProperty(PAYMENTLINKINSERT, String.class, 
        "Insert statement that creates the link between the payment and the posted transaction", 
        "This insert executes with the created or updated transaction's ID as the parameter " + MQ_TRANSACTION_ID + " " +
        "and the batch effective date as " + EFFECTIVE_DATE_PARAM_NAME + ", all other values queried from the payment query" +
        "are available to the insert as well. This statement should be used to create a direct link in the database between" +
        " the PTRFIN_TRANSACTION table and the table that cleared payments reside in.");
  }

  protected String getBatchDocumentation() {
    return "Processes pending payments based on configured query, posting both new and pending transactions.";
  }

  protected String getBatchSummary() {
    return "Posts payments for account with no installment plans";
  }

  protected TransferObject invoke() {
    final IReadWriteDataProvider p = getReadWriteProvider();
    try {
      intializeAccountTypes(
          new String[]{IAccountTypes.CIS, IAccountTypes.PIS, IAccountTypes.IIS, 
              IAccountTypes.PAYMENT, IAccountTypes.PRINCIPAL, IAccountTypes.PINST, 
              IAccountTypes.OVERPAY, 
              IAccountTypes.CHARGE, IAccountTypes.PENALTY, IAccountTypes.INTEREST});
    } catch (TransferObjectException e1) {
      return e1.getTransferObject();
    }
   try {
     p.execute(new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException,
          AbortTransactionException {
        generalPaymentAccount = getGeneralAccount(getAccountType(IAccountTypes.PAYMENT), p, t);
     }});
  } catch (AbortTransactionException e1) {
    return getGeneralAccountFetchError(e1);
  } catch (SQLException e1) {
    return getGeneralAccountFetchError(e1);
  }
    
    //
    QuerySpec spec = new QuerySpec((String) getPropertyValue(PAYMENTQUERY), 
        PAYMENT_QUERY_COLUMNS, 
        new Class[]{Long.class, BigDecimal.class, Date.class, Long.class});
    spec.allowNull(MQ_TRANSACTION_ID);
    spec.allowNull(PAYMENT_DATE);
    Map args = new HashMap();
    args.put(EFFECTIVE_DATE_PARAM_NAME, getEffectiveDate());
    try {
      Iterator i = p.query(IReadWriteDataProvider.STREAM_RESULTS, spec.getName(), args);
      IAccountTypeProvider atp = getLoanAccountService().getAccountTypeProvider();
      final IKnockoffScheduleManager k = new DefaultKnockoffScheduleManager(atp, getEffectiveDate());
      while(i.hasNext()){
        final Map payment = spec.verifyResultInstance((Map) i.next());
        final Long accountId = (Long) payment.get(ACCOUNT_ID);
        final Long transId = (Long) payment.get(MQ_TRANSACTION_ID);
        final BigDecimal paymentAmount = (BigDecimal) payment.get(PAYMENT_AMOUNT);
        Date suppliedPaymentDate = (Date) payment.get(PAYMENT_DATE);
        final Date paymentDate = suppliedPaymentDate != null ? suppliedPaymentDate : getEffectiveDate();
        if (paymentDate.after(getEffectiveDate())){
          throw new RuntimeException("One or more payments retrieved by query are dated into the future");
        }
        final LoanAccount loanAccount = getLoanAccountService().getLoanAccount(accountId);
        
        
        p.execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException,
              AbortTransactionException {
            // Now the real work
            Transaction trans = null;
            if (transId != null){
              // First we need to accrue any outstanding interest
              // so we can knock it off appropriately if our payment covers it
              loanAccount.postImmediateInterestAccrual(DateParser.getPreviousDay(getEffectiveDate()), getEffectiveDate(), null, p, t);
              TransactionQuery tq = new TransactionQuery();
              trans = tq.getTransaction(transId, getAccountFetcher(), p, t);
            } else {
              trans = loanAccount.apportionPayment(generalPaymentAccount, paymentAmount, paymentDate, getEffectiveDate(), k, null, p, t);
              trans = new TransactionAction(trans, paymentDate).enter(p, t);
            }
            Transaction posted = loanAccount.postTransaction(trans, getEffectiveDate(), p, t);
            payment.put("TRANSACTION_ID", posted.getId());
            payment.put(EFFECTIVE_DATE_PARAM_NAME, getEffectiveDate());
            p.insert((String)getPropertyValue(PAYMENTLINKINSERT), payment, t);
          }});
      }
    } catch (SQLException e) {
      getLog().error("Error querying payment to process: " + e.getMessage(), e);
      return new TransferObject(new Object[]{spec.getName(), e.getMessage()}, TransferObject.EXCEPTION, "PAYMENT_QUERY_FAILED");
    } catch (AbortTransactionException e) {
      getLog().error("Error in query for payments to process: " + e.getMessage(), e);
      return new TransferObject(new Object[]{spec.getName(), e.getMessage()}, TransferObject.EXCEPTION, "PAYMENT_QUERY_FAILED");
    }
    return new TransferObject();
  }
  

}
