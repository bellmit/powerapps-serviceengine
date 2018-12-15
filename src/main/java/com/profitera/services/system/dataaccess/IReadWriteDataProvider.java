package com.profitera.services.system.dataaccess;

import java.sql.SQLException;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;


public interface IReadWriteDataProvider extends IReadOnlyDataProvider {
  public Object insert(String qName, Object args, ITransaction t) throws SQLException;
  public int update(String qName, Object args, ITransaction t) throws SQLException;
  public void execute(IRunnableTransaction trans) throws SQLException, AbortTransactionException;
  public int delete(String string, Object args, ITransaction t)  throws SQLException;
  public boolean isInsertStatement(String qName);
  public boolean isUpdateStatement(String qName);
  public boolean isDeleteStatement(String qName);
}
