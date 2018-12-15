package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountAction;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.loan.ILoanAccountService;
import com.profitera.services.system.lookup.LookupManager;


public abstract class AbstractFinancialBatch extends AbstractProviderBatch {
  protected static final String CURRENCYCODE = "currencycode";
  protected static final String CURRENCYCODE_SHORT_DOC = "Base currency to use for evaluation";
  protected static final String CURRENCYCODE_LONG_DOC = "Base currency to use for evaluation, any transactions or new accounts created will be evaluated using this as the base commodity.";
  protected static final String ACCOUNT_ID = "ACCOUNT_ID";
  protected static final String ACCOUNTQUERY = "loanaccountquery";
  protected static final String ACCOUNTQUERY_SHORT_DOC = "Query to retrieve accounts to evaluate";
  protected static final String ACCOUNTQUERY_LONG_DOC = "Query that retrieves the accounts which should be evaluated by this batch. " +
  "This query must return: " + 
  "<variablelist>"
  + "<varlistentry><term>" + ACCOUNT_ID + "</term><listitem><para>The loan account to which the penalty is to be assessed, as represented in PTRACCOUNT.</para></listitem></varlistentry>"
  + "</variablelist>";
  //
  protected static final String STOPONERROR = "stoponerror";
  protected static final String STOPONERROR_DEFAULT = "true";
  protected static final String STOPONERROR_SHORT_DOC = "Controls the process response to an error in processing";
  protected static final String STOPONERROR_LONG_DOC = "If this property is false the batch will contitue processing other accounts after an error" +
          " is encoutentered during processing. A 'false' setting is <emphasis>not</emphasis> recommended for" +
          " production environments.";

  private final AccountAction accountFetcher = new AccountAction();
  private Map accountTypes = new HashMap();
  

  protected void addStopOnError() {
    addProperty(STOPONERROR, Boolean.class, STOPONERROR_DEFAULT, 
        STOPONERROR_SHORT_DOC, 
        STOPONERROR_LONG_DOC);
  }
  
  /**
   * Use of this method is discouraged, if possible use
   * the "getAccount" variants available.
   * @return 'global' AccountAction instance for the batch instance
   */
  protected AccountAction getAccountFetcher(){
    return accountFetcher;
  }
  
  protected AccountType getAccountType(String code){
    if (accountTypes == null){
      throw new IllegalStateException("Account types are not initalized for this batch");
    }
    if (accountTypes.containsKey(code)){
      return (AccountType) accountTypes.get(code);
    } else {
      throw new IllegalStateException("Account type requested was not initalized: " + code);
    }
  }
  
  protected void intializeAccountTypes(final String[] typeCodes) throws TransferObjectException{
    final IReadWriteDataProvider p = getReadWriteProvider();
    try {
      p.execute(new IRunnableTransaction(){
        public void execute(ITransaction t) throws SQLException,
            AbortTransactionException {
          ILoanAccountService s = getLoanAccountService();
          for (int i = 0; i < typeCodes.length; i++) {
            AccountType type = s.getAccountType(typeCodes[i], p, t);
            accountTypes.put(typeCodes[i], type);
          }
        }});
    }  catch (SQLException e){
      getLog().error("Failed to initialize required account types", e);
      throw new TransferObjectException(new TransferObject(TransferObject.EXCEPTION, "INIT_ACCOUNT_TYPE_FAILED"));
    } catch (AbortTransactionException e){
      getLog().error("Failed to initialize required account types", e);
      throw new TransferObjectException(new TransferObject(TransferObject.ERROR, "INIT_ACCOUNT_TYPE_FAILED"));
    }
  }
  
  protected Account getGeneralAccount(AccountType type, IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException {
    String errorCode = "NO_SUCH_GENERAL_ACCOUNT";
    Account acc = getLoanAccountService().getGeneralAccount(type, p, t);
    if (acc == null){
      getLog().error("Failed to retrieve account required account (type ID: " + type.getId() + "): " + errorCode);
      throw new AbortTransactionException(errorCode);
    }
    return acc;
  }
  
  protected ILoanAccountService getLoanAccountService(){
    final ILoanAccountService provider = (ILoanAccountService) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "LoanAccountService");
    if (provider == null){
      throw new IllegalStateException("LoanAccountService system services is not configured");
    }
    return provider;
  }
  
  protected TransferObject getGeneralAccountFetchError(
      Exception e1) {
    getLog().error("Failed to fetch general accounts", e1);
    return new TransferObject(TransferObject.EXCEPTION, "GENERAL_ACCOUNTS_FAILURE");
  }
  
}
