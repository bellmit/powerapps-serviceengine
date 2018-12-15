package com.profitera.services.business.batch.financial.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.business.batch.AbstractBatchProcess;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.services.system.financial.AccountAction;
import com.profitera.services.system.financial.AccountType;
import com.profitera.services.system.financial.Commodity;
import com.profitera.services.system.financial.CommodityAction;
import com.profitera.services.system.financial.CreateAccountAction;
import com.profitera.services.system.loan.IAccountTypes;
import com.profitera.services.system.loan.ILoanAccountService;
import com.profitera.services.system.loan.impl.IAccountTypeProvider;
import com.profitera.util.CollectionUtil;

public class GeneralAccountInitializer {

  private AccountAction accountAction;

  public void create(final String[] types, final String commodity, final Date effectiveDate, final ILoanAccountService l, final IReadWriteDataProvider p) 
      throws AbortTransactionException, SQLException {
    p.execute(new IRunnableTransaction(){
      
      public void execute(ITransaction t) throws SQLException,
          AbortTransactionException {
        create(types, commodity, effectiveDate, l.getAccountTypeProvider(), p, t);
      }
    });
  }

  public void create(String[] typeCodes, String commodity,
      Date effectiveDate, IAccountTypeProvider typeProvider,
      IReadWriteDataProvider sqlMapProvider, ITransaction transaction) throws SQLException, AbortTransactionException {
    CommodityAction ca = new CommodityAction();
    Commodity defaultCommodity;
    defaultCommodity = ca.getCommodity(commodity, sqlMapProvider, transaction);
    String[] types = new String[]{IAccountTypes.GENERAL};
    for (int i = 0; i < typeCodes.length; i++) {
      types = (String[]) CollectionUtil.extendArray(types, typeCodes[i]);
    }
    updateGeneralAccounts(types, defaultCommodity, effectiveDate, typeProvider, sqlMapProvider, transaction);
  }
  
  private void updateGeneralAccounts(String[] accountTypes, Commodity c, Date effectiveDate, IAccountTypeProvider l, final IReadWriteDataProvider p,
      ITransaction t) throws SQLException, AbortTransactionException {
    for (int i = 0; i < accountTypes.length; i++) {
      selectOrCreate(l.get(accountTypes[i], p, t), c, effectiveDate, p, t);  
    }
  }
  
  private AccountAction getAccountAction(){
    if (accountAction == null){
      accountAction = new AccountAction();
    }
    return accountAction;
  }
  

  private Account selectOrCreate(AccountType type, Commodity commodity, Date effectiveDate,
      IReadWriteDataProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    Long id = (Long) p.queryObject("getGeneralAccountId", type.getId());
    if (id == null){
      Account a = new CreateAccountAction(type, commodity).create(p, t);
      Map args = new HashMap();
      args.put("ACCOUNT_ID", a.getId());
      args.put(AbstractBatchProcess.EFFECTIVE_DATE_PARAM_NAME, effectiveDate);
      p.insert("insertGeneralAccountId", args, t);
      return a;
    } else {
      return getAccountAction().getAccount(id, p, t);
    }
  }

}
