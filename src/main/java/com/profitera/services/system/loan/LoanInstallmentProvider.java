package com.profitera.services.system.loan;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.loan.impl.IInstallmentProvider;
import com.profitera.util.CollectionUtil;
import com.profitera.util.DateParser;

class LoanInstallmentProvider implements IInstallmentProvider {
  private final Date effectiveDate;
  private final IReadWriteDataProvider p;
  private final Long id;

  LoanInstallmentProvider(Long id, Date effectiveDate, IReadWriteDataProvider p) {
    this.effectiveDate = effectiveDate;
    this.p = p;
    this.id = id;
  }

  public List fetchNextInstallments(Date lastDate) throws SQLException {
    Map args = new HashMap();
    args.put("ACCOUNT_ID", id);
    args.put("LEDGER_ID", id);
    args.put("AFTER_DATE", lastDate);
    args.put("EFFECTIVE_DATE", effectiveDate);
    String q = "getPostingRecordHistoryAfterDate";
    if (lastDate == null) {
      q = "getFirstPostingRecordHistory";
    }
    Iterator i = p.query(IReadOnlyDataProvider.LIST_RESULTS, q, args);
    List list = CollectionUtil.asList(i);
    if (list.size() ==  0 && lastDate == null) {
      return fetchNextInstallments(DateParser.getNextDay(effectiveDate));
    }
    return list;
  }
}