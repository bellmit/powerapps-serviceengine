package com.profitera.services.system.loan.impl;

import java.sql.SQLException;
import java.util.List;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.loan.AccountApportionment;
import com.profitera.services.system.loan.IAccountSet;

public interface IKnockoffScheduleManager {
  List<AccountApportionment> getAllAccountApportionments(IAccountSet loan, boolean allowPrincipal,
      boolean isPostResolution, IReadWriteDataProvider p, ITransaction t) 
      throws SQLException, AbortTransactionException;
  
}
