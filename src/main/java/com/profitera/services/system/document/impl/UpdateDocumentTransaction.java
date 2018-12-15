package com.profitera.services.system.document.impl;

import java.io.Reader;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public final class UpdateDocumentTransaction extends
    AbstractDocumentTransaction {
  private static final String UPDATE_DOCUMENT_SQL_NAME = "updateDocument";
  //
  private final boolean isEncoded;
  private final long documentType;
  private final Reader content;
  private final String description;

  public UpdateDocumentTransaction(Long documentId, boolean isEncoded,
      long documentType, Reader content, String description,
      IReadWriteDataProvider p, int width) {
    super(width, p);
    setId(documentId);
    this.isEncoded = isEncoded;
    this.documentType = documentType;
    this.content = content;
    this.description = description;
  }

  public void execute(ITransaction t) throws SQLException, AbortTransactionException {
    Map<String, Object> document = new HashMap<String, Object>();
    document.put(DOCUMENT_TYPE_ID, new Long(documentType));
    document.put(DESCRIPTION, description);
    document.put(MODIFIED_DATE, new Date());
    document.put(ID, getId());
    document.put(IS_ENCODED, isEncoded);
    getProvider().update(UPDATE_DOCUMENT_SQL_NAME, document, t);
    deleteFragments(getId(), t);
    runInsertFragments(isEncoded, content, t);
  }
}