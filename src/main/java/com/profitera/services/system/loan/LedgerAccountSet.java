package com.profitera.services.system.loan;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.business.batch.AbstractBatchProcess;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Commodity;
import com.profitera.services.system.financial.CommodityAction;
import com.profitera.services.system.financial.CreateAccountAction;
import com.profitera.services.system.financial.Split;
import com.profitera.services.system.financial.Transaction;

public class LedgerAccountSet extends AbstractAccountSet {

  public LedgerAccountSet(Long id, IGeneralAccountService g) {
    super(id, g);
  }
  
  public void initLedgerAccountSet(String commodity, String[] types, Date effectiveDate, IReadWriteDataProvider p, ITransaction t) throws SQLException, AbortTransactionException {
    Commodity c = new CommodityAction().getCommodity(commodity, p, t);    
    List<Account> createdAccounts = new ArrayList<Account>();
    for (int i = 0; i < types.length; i++) {
      AccountType at = getAccountTypeProvider().get(types[i], p, t);
      if (getAccountSetAccountId(at, p, t) == null) {
        Account a = new CreateAccountAction(at, c).create(p, t);
        createdAccounts.add(a);
      }
    }
    Map arguments = new HashMap();
    arguments.put("ID", getId());
    arguments.put(AbstractBatchProcess.EFFECTIVE_DATE_PARAM_NAME, effectiveDate);
    for (Account account : createdAccounts) {
      arguments.put("FINANCIAL_ACCOUNT_ID", account.getId());
      p.insert("insertLedgerFinancialAccountSetLink", arguments, t);      
    }
  }

  @Override
  protected Long getAccountSetAccountId(AccountType t, IReadOnlyDataProvider p,
      ITransaction tr) throws SQLException {
    Map args = new HashMap();
    args.put("ID", getId());
    args.put("ACCOUNT_TYPE_ID", t.getId());
    return (Long) p.queryObject("getLedgerFinancialAccountId", args);
  }

  public Account getLedgerAccount(Account suspAccount,
      IReadWriteDataProvider p, ITransaction t) throws SQLException,
      AbortTransactionException {
    // Payables don't have suspense
    return suspAccount;
  }

  public boolean isSuspense(Split split, IReadWriteDataProvider p,
      ITransaction t) throws SQLException, AbortTransactionException {
    // Ledger accounts don't have suspense, it has no meaning right now
    return false;
  }

  public Transaction postTransaction(Transaction trans, Date effectiveDate,
      IReadWriteDataProvider p, ITransaction t)
      throws AbortTransactionException, SQLException {
    return postTransactionInternal(trans, effectiveDate, p, t);
  }

  public Transaction reverseTransaction(Long id, Date date,
      IReadWriteDataProvider p, ITransaction t)
      throws AbortTransactionException, SQLException {
    return reverseTransactionInternal(id, date, p, t);
  }

}
