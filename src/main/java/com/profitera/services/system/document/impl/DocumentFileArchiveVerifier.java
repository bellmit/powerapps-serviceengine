package com.profitera.services.system.document.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.profitera.services.system.document.IDocumentFileVerifier;
import com.profitera.util.io.FileUtil;

public class DocumentFileArchiveVerifier implements IDocumentFileVerifier {
  private final MessageDigest digest;
  
  public DocumentFileArchiveVerifier(MessageDigest digest) {
    this.digest = digest;
  }
  
  /* (non-Javadoc)
   * @see com.profitera.services.system.document.impl.IDocumentFileVerifier#verify(java.io.File, com.profitera.services.system.document.impl.IDocumentLineListener)
   */
  public void verify(File source, IDocumentLineListener listener) throws IOException {
    digest.reset();
    ZipFile z = new ZipFile(source);
    {
      ZipEntry e;
      for (int i = 1; (e = z.getEntry(i + "")) != null; i++) {
        InputStream r = z.getInputStream(e);
        byte[] entireStream = FileUtil.readEntireStream(r, 10000);
        r.close();
        digest.update(entireStream);
        if (!listener.line(new String(entireStream, "UTF8"))){
          return;
        }
      }
    }
    ZipEntry hashEntry = z.getEntry("hash");
    if (hashEntry == null) {
      throw new IOException("Archived file '" + source.getAbsolutePath() + "' file 'hash' entry for verification not found");
    }
    InputStream h = z.getInputStream(hashEntry);
    String hash = new String(FileUtil.readEntireStream(h, 1000));
    h.close();
    String realHash = DocumentFileArchiveWriter.getPaddedHash(digest);
    if (!hash.equals(realHash)) {
      throw new IOException("Archived file '" + source.getAbsolutePath() + "' file hash of " + hash + " differs from content " + realHash);
    }
    z.close();
  }
}
