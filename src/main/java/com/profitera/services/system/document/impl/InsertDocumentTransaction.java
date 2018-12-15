package com.profitera.services.system.document.impl;

import java.io.Reader;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IDocumentTransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class InsertDocumentTransaction extends AbstractDocumentTransaction implements IDocumentTransaction {
  private static final String INSERT_DOCUMENT_SQL_NAME = "insertDocument";
  //
  
  private final long documentType;
  private final String description;
  private final boolean isEncoded;
  private final Reader content;
  
  public InsertDocumentTransaction(final long documentType, 
      final String description , final boolean isEncoded, 
      final Reader content, final IReadWriteDataProvider p,
      int fragmentWidth) {
    super(fragmentWidth, p);
    this.documentType = documentType;
    if("".equals(description)){
    	this.description = "NA"; // work around for Oracle. Oracle treat empty string as null.
    }else{
    	this.description = description;
    }
    this.isEncoded = isEncoded;
    this.content = content;
  }
  
  public void execute(ITransaction t) throws SQLException,
      AbortTransactionException {
    Map document = new HashMap();
    document.put(DOCUMENT_TYPE_ID, new Long(documentType));
    document.put(DESCRIPTION, description);
    document.put(CREATED_DATE, new Date());
    document.put(MODIFIED_DATE, null);
    document.put(IS_ENCODED, isEncoded);
    Long docId = (Long) getProvider().insert(INSERT_DOCUMENT_SQL_NAME, document, t);
    setId(docId);
    runInsertFragments(isEncoded, content, t);
  }
}
