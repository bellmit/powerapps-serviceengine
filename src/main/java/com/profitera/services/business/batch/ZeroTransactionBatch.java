package com.profitera.services.business.batch;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
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
import com.profitera.services.system.loan.LoanAccount;

public class ZeroTransactionBatch extends AbstractFinancialBatch {
  {
    addRequiredProperty("query", String.class, "The query to execute to retrieve the list of accounts", "Must return an ACCOUNT_ID field that is the valid ID of an initialized loan account.");
  }
  @Override
  protected String getBatchDocumentation() {
    return "Executes a zero-value transaction against each of the loans returned by the main query.";
  }

  @Override
  protected String getBatchSummary() {
    return "Executes a zero transaction for each loan";
  }

  @Override
  protected TransferObject invoke() {
    String query = (String) getPropertyValue("query");
    try {
      intializeAccountTypes(new String[]{IAccountTypes.CHARGE});
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
    final AccountType type = getAccountType(IAccountTypes.CHARGE);
    try {
      Iterator i = getReadWriteProvider().query(IReadWriteDataProvider.LIST_RESULTS, query, new HashMap());
      while(i.hasNext()) {
        Map m = (Map) i.next();
        Long accountId = (Long) m.get("ACCOUNT_ID");
        final LoanAccount loanAccount = getLoanAccountService().getLoanAccount(accountId);
        getReadWriteProvider().execute(new IRunnableTransaction(){
          public void execute(ITransaction t) throws SQLException,
              AbortTransactionException {
            final Account misc = loanAccount.getSetFinancialAccount(type, getReadWriteProvider(), t);
            Split s = new Split(misc, new BigDecimal(0), null, null);
            Transaction tran = new Transaction(misc.getCommodity(), new Split[]{s});
            loanAccount.postTransaction(tran, getEffectiveDate(), getReadWriteProvider(), t);
          }});
      }
    } catch (SQLException e) {
      getLog().error("Error executing SQL for " + getIdentifier(), e);
      return new TransferObject(TransferObject.EXCEPTION, "DATABASE_ERROR");
    } catch (AbortTransactionException e) {
      // Unreachable
    }
    return new TransferObject();
  }

}
