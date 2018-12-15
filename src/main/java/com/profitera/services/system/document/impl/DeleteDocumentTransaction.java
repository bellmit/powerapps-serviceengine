package com.profitera.services.system.document.impl;

import java.sql.SQLException;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public final class DeleteDocumentTransaction extends
    AbstractDocumentTransaction {
  private static final String DELETE_DOCUMENT_SQL_NAME = "deleteDocument";

  public DeleteDocumentTransaction(int width, Long documentId,
      IReadWriteDataProvider p) {
    super(width, p);
    setId(documentId);
  }

  public void execute(ITransaction t) throws SQLException, AbortTransactionException {
    deleteFragments(getId(), t);
    getProvider().delete(DELETE_DOCUMENT_SQL_NAME, getId(), t);
  }
}