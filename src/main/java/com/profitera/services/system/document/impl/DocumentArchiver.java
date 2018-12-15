package com.profitera.services.system.document.impl;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Arrays;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.log.ILogProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IArchivePathResolver;
import com.profitera.services.system.document.IDocumentFileVerifier;

public class DocumentArchiver {
  private IArchivePathResolver resolver;
  private MessageDigest digest;
  private final DocumentRetriever retriever;
  private final int rows;
  private final ILogProvider log;

  public DocumentArchiver(MessageDigest d, IArchivePathResolver r, DocumentRetriever retriever, int rowsPerFetch, ILogProvider log) {
    this.resolver = r;
    this.digest = d;
    this.retriever = retriever;
    this.rows = rowsPerFetch;
    this.log = log;
  }
  
  public void archive(final long documentId, File swap, final IReadWriteDataProvider p) throws SQLException, IOException, AbortTransactionException {
    File path = resolver.getPath(documentId);
    log.emit(DocumentLogClient.TARGET_PATH, documentId, path.getCanonicalPath());
    path.getParentFile().mkdirs();
    DocumentFileArchiveWriter archiver = new DocumentFileArchiveWriter(swap, path, digest);
    long startTime = System.currentTimeMillis();
    archiver.start();
    int start = 0;
    int count = -1;
    while (count != 0) {
      String[] documentLines = retriever.retrieveLines(start, rows);
      archiver.archiveFragments(Arrays.asList(documentLines));
      count = documentLines.length;
      start = start + count;
    }
    archiver.end();
    IDocumentFileVerifier verifier = new DocumentFileArchiveVerifier(digest);
    verifier.verify(path, new NoopDocumentLineListener());
    final int[] deletedCount = new int[1];
    p.execute(new IRunnableTransaction(){
      public void execute(ITransaction t) throws SQLException,
          AbortTransactionException {
        p.update("updateDocumentToArchived", documentId, t);
        deletedCount[0] = p.delete("deleteArchivedDocumentFragments", documentId, t);
      }});
    long duration = System.currentTimeMillis() - startTime;
    log.emit(DocumentLogClient.ARCHIVED, documentId, duration, deletedCount[0]);
  }
}
