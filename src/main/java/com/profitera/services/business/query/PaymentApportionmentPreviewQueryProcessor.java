package com.profitera.services.business.query;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Split;
import com.profitera.services.system.financial.Transaction;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.ILoanAccountService;
import com.profitera.services.system.loan.LoanAccount;
import com.profitera.services.system.lookup.LookupManager;

public class PaymentApportionmentPreviewQueryProcessor extends
    BaseListQueryProcessor {

  private static final String ACCOUNT_TYPE_KEY = "ACCOUNT_TYPE_KEY";
  private static final String AMOUNT_KEY = "AMOUNT_KEY";
  private static final String ACCOUNT_ID_ARGUMENT = "ACCOUNT_ID_ARGUMENT";
  private static final String PAYMENT_AMOUNT_ARGUMENT = "PAYMENT_AMOUNT_ARGUMENT";
  
  public PaymentApportionmentPreviewQueryProcessor() {
    addProperty(ACCOUNT_ID_ARGUMENT, String.class, "ACCOUNT_ID", "Account ID argument name", "Account ID argument name.");
    addProperty(PAYMENT_AMOUNT_ARGUMENT, String.class, "PAYMENT_AMOUNT", "Payment amount argument name", "Payment amount argument name.");
    //
    addProperty(AMOUNT_KEY, String.class, "AMOUNT", "Key used to set amount value", "Key used to set amount value.");
    addProperty(ACCOUNT_TYPE_KEY, String.class, "ACCOUNT_TYPE", "Key used to set account type code value", "Key used to set account type code value.");
    
  }
  @Override
  protected String getDocumentation() {
    return "Replaces existing results with a list of transaction splits that would be created "
    + "if the amount provided is automatically apportioned by the server.";
  }

  @Override
  protected String getSummary() {
    return "Returns the details of an intended payment apportionment";
  }
  
  private String getAccountIdArgument() {
    return (String) getProperty(ACCOUNT_ID_ARGUMENT);
  }
  
  private String getPaymentAmountArgument() {
    return (String) getProperty(PAYMENT_AMOUNT_ARGUMENT);
  }
  
  private String getAmountKey() {
    return (String) getProperty(AMOUNT_KEY);
  }
  private String getAccountTypeKey() {
    return (String) getProperty(ACCOUNT_TYPE_KEY);
  }

  @Override
  public List postProcessResults(Map arguments, List result, IQueryService qs)
      throws TransferObjectException {
    result = null;
    final List newresult = new ArrayList();
    final Long accountId = (Long) arguments.get(getAccountIdArgument());
    if (accountId == null) {
      throw new TransferObjectException(new TransferObject(TransferObject.ERROR, "NO_ACCOUNT_ID_PROVIDED"));
      
    }
    final BigDecimal paymentAmount = (BigDecimal) arguments.get(getPaymentAmountArgument());
    if (paymentAmount == null) {
      throw new TransferObjectException(new TransferObject(TransferObject.ERROR, "NO_PAYMENT_AMOUNT_PROVIDED"));
      
    }
    final IReadWriteDataProvider p = getReadWriteProvider();
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          LoanAccount loanAccount = getLoanAccount(accountId);
          AccountType accountType = getLoanAccountService().getAccountType(IAccountTypes.PAYMENT, p, t);
          Account gen = getLoanAccountService().getGeneralAccount(accountType, p, t);
          Transaction apportionPayment = loanAccount.apportionPayment(gen, paymentAmount, new Date(), new Date(), null, p, t);
          Split[] splits = apportionPayment.getSplits();
          Field[] fields = IAccountTypes.class.getFields();
          Map <Long, String> types = new HashMap();
          for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(String.class)){
              try {
                String code = (String) f.get(null);
                AccountType at = getLoanAccountService().getAccountType(code, p, t);
                Long id = at.getId();
                types.put(id, code);
              } catch (IllegalArgumentException e) {
                // Impossible
              } catch (IllegalAccessException e) {
                // Impossible
              }
              
            }
            
          }
          for (int i = 0; i < splits.length; i++) {
            Split s = splits[i];
            Account account = s.getAccount();
            if (account.equals(gen)) continue;
            AccountType type = account.getType();
            Long typeId = type.getId();
            String accountTypeCode = types.get(typeId);
            BigDecimal amount = s.getExchangedAmount();
            Map m = new HashMap();
            m.put(getAmountKey(), amount);
            m.put(getAccountTypeKey(), accountTypeCode);
            newresult.add(m);
          }
          throw new AbortTransactionException("FAKE");
        }});
    } catch (AbortTransactionException e) {
      if (e.getMessage().equals("FAKE")){
        return newresult;
      } else {
        getLog().error("Apportionment aborted", e);
      }
    } catch (SQLException e) {
      getLog().error("Error retrieving apportionment details", e);
    }
    throw new TransferObjectException(new TransferObject(TransferObject.ERROR, "DATABASE_ERROR"));
  }
  
  private LoanAccount getLoanAccount(Long accountId) {
    return getLoanAccountService().getLoanAccount(accountId);
  }
  
  protected ILoanAccountService getLoanAccountService(){
    final ILoanAccountService provider = (ILoanAccountService) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "LoanAccountService");
    if (provider == null){
      throw new IllegalStateException("LoanAccountService system services is not configured");
    }
    return provider;
  }
  protected IReadWriteDataProvider getReadWriteProvider() {
    final IReadWriteDataProvider provider = (IReadWriteDataProvider) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
    return provider;
  }
}
