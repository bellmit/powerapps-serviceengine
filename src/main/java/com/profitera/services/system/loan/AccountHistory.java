package com.profitera.services.system.loan;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.finance.PeriodicBalance;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.Account;
import com.profitera.util.BigDecimalUtil;
import com.profitera.util.CollectionUtil;
import com.profitera.util.MapListUtil;

public class AccountHistory {
  private static final String Q_SPLIT_HIST = "getFinancialAccountSplitHistory";
  private final Account[] accounts;

  public AccountHistory(Account account){
    this.accounts = new Account[]{account};
  }
  
  public AccountHistory(Account[] accounts){
    this.accounts = accounts;
  }
  
  public PeriodicBalance getAccountBalance(Date startDate, Date endDate, IReadWriteDataProvider p) throws SQLException, AbortTransactionException{
    final List splits = new ArrayList();
    BigDecimal balance = BigDecimalUtil.ZERO; 
    for (int i = 0; i < accounts.length; i++) {
      Account account = accounts[i];
      Map arguments = new HashMap();
      arguments.put("ACCOUNT_ID", account.getId());
      arguments.put("START_DATE", startDate);
      arguments.put("END_DATE", endDate);
      List theseSplits = CollectionUtil.asList(p.query(IReadWriteDataProvider.LIST_RESULTS, Q_SPLIT_HIST, arguments));
      splits.addAll(theseSplits);
      // Switched to assume no results means no transactions, I can 
      // only hope this won't come back to bite me later
      BigDecimal endbalance = BigDecimalUtil.ZERO ;
      if (theseSplits.size() > 0) {
        endbalance = (BigDecimal) ((Map)theseSplits.get(0)).get("END_BALANCE");
      }
      balance = balance.add(endbalance);      
    }
    int findMin = MapListUtil.findMin("SPLIT_DATE", splits);
    if (findMin != -1) {
      Map firstSplit = (Map) splits.get(findMin);
      Date earliestDate = (Date) firstSplit.get("SPLIT_DATE");
      if (earliestDate.after(startDate)) {
        Map dummyFirstSplit = new HashMap();
        dummyFirstSplit.put("SPLIT_DATE", startDate);
        dummyFirstSplit.put("AMOUNT", BigDecimal.ZERO);
        splits.add(dummyFirstSplit);
      }
    }
    return new PeriodicBalance(balance, splits, "SPLIT_DATE", "AMOUNT");
  }

}
