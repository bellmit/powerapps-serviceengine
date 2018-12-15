package com.profitera.services.business.batch;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.dataaccess.QuerySpec;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.financial.IFinancialProvider;
import com.profitera.services.business.batch.financial.impl.LoanAccountIntializer;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.loan.LoanAccount;

public class InitializeLoanAccountsBatch extends AbstractFinancialBatch {
  public InitializeLoanAccountsBatch() {
    addRequiredProperty(CURRENCYCODE, String.class, CURRENCYCODE_SHORT_DOC, CURRENCYCODE_LONG_DOC);
    addRequiredProperty(ACCOUNTQUERY, String.class, ACCOUNTQUERY_SHORT_DOC, 
        "Query that returns the list of uninitialized accounts to process in this batch, this query must return:"
        + "<variablelist>"
        + "<varlistentry><term>" + ACCOUNT_ID + "</term><listitem><para>The loan account to which this payment is linked, as represented in PTRACCOUNT.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + IFinancialProvider.PRINCIPAL_AMOUNT + "</term><listitem><para>.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + IFinancialProvider.PRINCIPAL_OVERDUE_AMOUNT + "</term><listitem><para>.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + IFinancialProvider.INTEREST_AMOUNT + "</term><listitem><para>.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + IFinancialProvider.CHARGE_AMOUNT + "</term><listitem><para>.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + IFinancialProvider.PENALTY_AMOUNT + "</term><listitem><para>.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + IFinancialProvider.INTEREST_IN_SUSPENSE_AMOUNT + "</term><listitem><para>.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + IFinancialProvider.CHARGE_IN_SUSPENSE_AMOUNT + "</term><listitem><para>.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + IFinancialProvider.PENALTY_IN_SUSPENSE_AMOUNT + "</term><listitem><para>.</para></listitem></varlistentry>"
        + "</variablelist>"
        );
  }  
  
  protected String getBatchDocumentation() {
    return "Executes a query to retrieve all loan accounts that are eligible but do not have financial accounts created and then adds the appropriate financial accounts.";
  }

  protected String getBatchSummary() {
    
    return "Initializes loan accounts' financial accounts for processing";
  }
  

  protected TransferObject invoke() {
    final IReadWriteDataProvider p = getReadWriteProvider();
    final LoanAccountIntializer[] l = new LoanAccountIntializer[1];
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          l[0] = new LoanAccountIntializer((String) getPropertyValue(CURRENCYCODE), getLoanAccountService(), p, t);
        }});
    } catch (AbortTransactionException e1) {
      return getGeneralAccountFetchError(e1);
    } catch (SQLException e1) {
      return getGeneralAccountFetchError(e1);
    }
    //
    Map arguments = new HashMap();
    arguments.put(EFFECTIVE_DATE_PARAM_NAME, getEffectiveDate());
    QuerySpec spec = new QuerySpec((String) getPropertyValue(ACCOUNTQUERY), 
        new String[]{ACCOUNT_ID, 
        IFinancialProvider.PRINCIPAL_AMOUNT, IFinancialProvider.PRINCIPAL_OVERDUE_AMOUNT, IFinancialProvider.INTEREST_AMOUNT, IFinancialProvider.CHARGE_AMOUNT, IFinancialProvider.PENALTY_AMOUNT,
        IFinancialProvider.INTEREST_IN_SUSPENSE_AMOUNT, IFinancialProvider.CHARGE_IN_SUSPENSE_AMOUNT, IFinancialProvider.PENALTY_IN_SUSPENSE_AMOUNT
        }, 
        new Class[]{Long.class, 
        BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class,
        BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, 
        });
    Iterator i = null;
    try {
      i = p.query(IReadOnlyDataProvider.STREAM_RESULTS, spec.getName(), arguments);
    } catch (SQLException e) {
      getLog().error("Failed to execute primary query for account initialization", e);
      return new TransferObject(TransferObject.EXCEPTION, "INIT_QUERY_FAILURE");
    }
    while (i.hasNext()){
      final Map loan = (Map) i.next();
      Long loanAccountId = null;
      try {
        try {
          // query for all accounts is wrong/invalid, this is bad
          spec.verifyResultInstance(loan);
        } catch (AbortTransactionException e){
          getLog().error(e.getMessage(), e);
          return new TransferObject(TransferObject.ERROR, "INIT_QUERY_FAILURE");
        }
        loanAccountId = (Long) loan.get(ACCOUNT_ID);
        final LoanAccount loanAccount = getLoanAccountService().getLoanAccount(loanAccountId);
        p.execute(new IRunnableTransaction() {
          
          public void execute(ITransaction t) throws SQLException,
              AbortTransactionException {
            l[0].initializeLoan(loanAccount, loan, getEffectiveDate(), getLoanAccountService(), p, t);
          }
        });
        
      } catch (AbortTransactionException e) {
        getLog().error("Account processing aborted for: " + loanAccountId, e);
      } catch (SQLException e) {
        getLog().error("Account processing failed for: " + loanAccountId, e);
      }
    }
    return new TransferObject();
  }
}
