package com.profitera.services.system.loan;

import java.sql.SQLException;

import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.financial.AccountType;

public interface ILoanAccountService extends IGeneralAccountService {
  public LoanAccount getLoanAccount(Long accountId);
  public AccountType getAccountType(String typeCode, IReadWriteDataProvider p,
      ITransaction t) throws SQLException;
}
