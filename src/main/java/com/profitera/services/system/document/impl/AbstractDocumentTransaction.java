package com.profitera.services.system.document.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.services.system.dataaccess.IDocumentTransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public abstract class AbstractDocumentTransaction implements IDocumentTransaction {
  private static final String INSERT_DOCUMENT_FRAGMENT_SQL_NAME = "insertDocumentFragment";
  private static final String DELETE_DOCUMENT_FRAGMENTS_SQL_NAME = "deleteDocumentFragments";
  //
  static final String IS_ENCODED = "IS_ENCODED";
  static final String MODIFIED_DATE = "MODIFIED_DATE";
  static final String CREATED_DATE = "CREATED_DATE";
  static final String DESCRIPTION = "DESCRIPTION";
  static final String DOCUMENT_TYPE_ID = "DOCUMENT_TYPE_ID";
  static final String FRAGMENT_SEQUENCE = "FRAGMENT_SEQUENCE";
  static final String FRAGMENT_TEXT = "FRAGMENT_TEXT";
  static final String LEGACY_IS_HEX_ENCODED = "IS_HEX_ENCODED";
  static final String DOCUMENT_ID = "DOCUMENT_ID";
  static final String ID = "ID";
  private final int width;
  private Long id;
  private IReadWriteDataProvider prov;

  public AbstractDocumentTransaction(int width, IReadWriteDataProvider p) {
    this.width = width;
    this.prov = p;
  }
  
  protected void insertFragments(Long docId, boolean hexEncoded, Reader content, ITransaction t) throws SQLException, IOException {
    int fragmentWidth = getFragmentWidth();
    BufferedReader buffer = new BufferedReader(content);
    char[] characters = new char[fragmentWidth];
    int read = buffer.read(characters);
    for (int i = 0; read!=-1; i++) {
      Map<String, Object> m = new HashMap<String, Object>(); 
      m.put(DOCUMENT_ID, docId);
      m.put(LEGACY_IS_HEX_ENCODED, new Boolean(hexEncoded));
      m.put(FRAGMENT_TEXT, new String(characters, 0, read));
      m.put(FRAGMENT_SEQUENCE, new Long(i));
      getProvider().insert(INSERT_DOCUMENT_FRAGMENT_SQL_NAME, m, t); 
      read = buffer.read(characters);
    }
  }
  
  protected void deleteFragments(Long id, ITransaction t) throws SQLException {
    getProvider().delete(DELETE_DOCUMENT_FRAGMENTS_SQL_NAME, id, t);
  }

  private int getFragmentWidth() {
    return width;
  }
  
  public Long getId() {
    return id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  protected IReadWriteDataProvider getProvider() {
    return prov;
  }
  
  protected void runInsertFragments(boolean isEncoded, Reader content, ITransaction t) throws SQLException, AbortTransactionException {
    try {
      insertFragments(getId(), isEncoded, content, t);
    } catch (IOException e) {
      throw new AbortTransactionException("Failed to read content to end", e);
    }
  }


}
