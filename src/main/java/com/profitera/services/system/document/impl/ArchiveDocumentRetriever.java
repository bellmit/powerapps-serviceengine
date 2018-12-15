package com.profitera.services.system.document.impl;

import java.io.File;
import java.io.IOException;

import com.profitera.services.system.document.IArchivePathResolver;
import com.profitera.services.system.document.IDocumentFileVerifier;

public class ArchiveDocumentRetriever {
  private long documentId;
  private final IArchivePathResolver resolver;
  private final IDocumentFileVerifier verifier;
  public ArchiveDocumentRetriever(long documentId, IArchivePathResolver resolver, IDocumentFileVerifier verifier) {
    this.documentId = documentId;
    this.resolver = resolver;
    this.verifier = verifier;
  }
  
  public String[] retrieveLines(long startLine, int lineCount) throws IOException {
    File path = resolver.getPath(documentId);
    LineRangeListener l = new LineRangeListener(startLine, lineCount);
    verifier.verify(path, l);
    return l.getLines();
  }
}
