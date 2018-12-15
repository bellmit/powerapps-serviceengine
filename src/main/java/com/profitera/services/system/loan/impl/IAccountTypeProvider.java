package com.profitera.services.system.loan.impl;

import java.sql.SQLException;

import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.AccountType;

public interface IAccountTypeProvider {
  public AccountType get(String code, IReadWriteDataProvider p, ITransaction t) throws SQLException;
}
