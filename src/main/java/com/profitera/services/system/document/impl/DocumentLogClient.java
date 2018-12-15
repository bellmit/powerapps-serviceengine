package com.profitera.services.system.document.impl;

import com.profitera.log.ILogClient;
import com.profitera.log.ILogProvider;
import com.profitera.log.ILogProvider.Level;

public class DocumentLogClient implements ILogClient {

  public static final String TARGET_PATH = "DOC_ARCHIVE_TARGET";
  public static final String ARCHIVED = "DOC_ARCHIVE_COMPLETE";
  public static final String ENCODING = "DOC_ENCODING_ERROR";
  public static final String ENCODING_FAILED = "DOC_ENCODING_FAILURE";
  public static final String TEMP_BAD = "DOC_TEMP_FAILURE";

  public String getName() {
    return "Document Service";
  }

  public void registerMessages(ILogProvider provider) {
    provider.registerMessage(this, TARGET_PATH, Level.I, "Archiving document {0} to target path {1}", 
        "The document identified by the ID indicated will be archived to the specified file " +
        "and the document content purged from the database if successful.");
    provider.registerMessage(this, ARCHIVED, Level.I, "Successfully archived document {0} in {1}ms and content of {2} rows was deleted from database", 
    "The document identified by the ID indicated was archived and its contents removed from the database.");
    provider.registerMessage(this, ENCODING, Level.E, "Unsupported encoding for document text, falling back to character storage", 
        "Character encoding has failed and documents will be stored as plain text for the server platform which may cause internationalization issues.");
    provider.registerMessage(this, ENCODING_FAILED, Level.E, "Unsupported encoding found for document {0} with encoding {1}", 
    "Character encoding has failed and the document can not be retrieved properly.");
    provider.registerMessage(this, TEMP_BAD, Level.W, "Unable to create temp files at location specified by {0} ({1})", 
        "The special document temp folder is specified but is not usable, a stacktrace is provided for diagnosis.");
  }

}
