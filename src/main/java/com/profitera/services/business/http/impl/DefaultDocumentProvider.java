package com.profitera.services.business.http.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ISqlMapProvider;
import com.profitera.dataaccess.ITransaction;
import com.profitera.document.IDocumentProvider;
import com.profitera.event.impl.DefaultRequestInformation;
import com.profitera.event.template.impl.TemplateGenerator;
import com.profitera.event.template.impl.TemplateGenerator.Version;
import com.profitera.services.system.dataaccess.IDocumentTransaction;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IDocumentHeader;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.util.io.FileUtil;

public class DefaultDocumentProvider implements IDocumentProvider {
  private final IDocumentService d;

  public DefaultDocumentProvider(IDocumentService d) {
    this.d = d;
  }

  public long updateCharacterDocument(Long docId, long documentType,
      String content, ISqlMapProvider p, ITransaction t) throws AbortTransactionException, SQLException {
    if (docId == null) {
      IDocumentTransaction createDocument = d.createDocument(documentType, "", content, (IReadWriteDataProvider) p);
      createDocument.execute(t);
      return createDocument.getId();
    } else {
      d.updateDocument(docId, documentType, "", content, (IReadWriteDataProvider) p).execute(t);
      return docId;
    }
  }

  public String fetchCharacterDocument(long docId, ISqlMapProvider p, ITransaction t)
      throws SQLException, IOException {
    return d.getCharacterDocumentContent(docId, (IReadOnlyDataProvider) p).toString();
  }

  @Override
  public long updateBinaryDocument(Long docId, long documentType, InputStream content, ISqlMapProvider p,
    ITransaction t) throws AbortTransactionException, SQLException {
    if (docId == null) {
      IDocumentTransaction createDocument = d.createDocument(documentType, "", content, (IReadWriteDataProvider) p);
      createDocument.execute(t);
      return createDocument.getId();
    } else {
      d.updateDocument(docId, documentType, "", content, (IReadWriteDataProvider) p).execute(t);
      return docId;
    }
  }
  

  @Override
  public long updateCharacterDocument(Long docId, long documentType, Reader content, ISqlMapProvider p,
      ITransaction t) throws AbortTransactionException, SQLException {
    if (docId == null) {
      IDocumentTransaction createDocument = d.createDocument(documentType, "", content, (IReadWriteDataProvider) p);
      createDocument.execute(t);
      return createDocument.getId();
    } else {
      d.updateDocument(docId, documentType, "", content, (IReadWriteDataProvider) p).execute(t);
      return docId;
    }
  }

  @Override
  public InputStream getDocumentStream(long docId, String mimeType, ISqlMapProvider p) throws SQLException, IOException {
    IReadWriteDataProvider provider = (IReadWriteDataProvider) p;
    long type = d.getDocument(docId, provider).getType();
    if (type == GENERATED_TEMPLATE_DOCUMENT_TYPE_ID && !"text/xml".equals(mimeType)) {
      // Here is the special case where a conversion can be performed
      // TODo: Fix me!
      TemplateGenerator g = new TemplateGenerator(Version.DEFAULT, getDocumentTemporaryDirectory(), new DefaultRequestInformation(-1, null, null, null, null));
      TemplateGenerator.ExportFormat f = TemplateGenerator.ExportFormat.PDF;
      if ("text/html".equals(mimeType)) {
        f = TemplateGenerator.ExportFormat.HTML;
      }
      File output = g.export(d.getCharacterDocumentContent(docId, provider), f);
      byte[] content = FileUtil.readEntireFile(output, 5000);
      output.delete();
      return new ByteArrayInputStream(content);
    } else {
      return d.getDocumentContentStream(docId, provider);
    }
  }

  @Override
  public String getDocumentMime(long docId, String mime, ISqlMapProvider p) throws SQLException {
    // The mime param can be a raw accepts header so that needs to be considered
    //Accept: text/xml, application/xml, application/xhtml+xml, text/html;q=0.9, 
    //   text/plain;q=0.8, image/png,*/*;q=0.5
    IDocumentHeader header = d.getDocument(docId, (IReadOnlyDataProvider) p);
    String[] mimes = mime == null ? new String[0] : mime.split(",");
    boolean isSingleTypeRequested = mimes.length == 1;
    boolean isOnlyXMLRequested = isSingleTypeRequested && mimes[0].toLowerCase().contains("/xml");
    // No matter what templates and decision trees come down as XML text MIME
    if (header.getType() == TEMPLATE_DOCUMENT_TYPE_ID || header.getType() == DECISION_TREE_XML) {
      return "text/xml";
    } else if (header.getType() == GENERATED_TEMPLATE_DOCUMENT_TYPE_ID && isOnlyXMLRequested) {
      return "text/xml";
    } else if (header.getType() == GENERATED_TEMPLATE_DOCUMENT_TYPE_ID && isSingleTypeRequested && mime.contains("text/html")) {
      return "text/html";
    } else if (header.getType() == GENERATED_TEMPLATE_DOCUMENT_TYPE_ID) {
      return "application/pdf";
    } else if (header.getType() == IMAGE_DOCUMENT_TYPE_ID && !isSingleTypeRequested) {
      return "image/*";
    } else if (isSingleTypeRequested) {
      /*
      EMAIL_ATTACHMENT_DOCUMENT_TYPE_ID = 6;
      EMAIL_CONTENT_DOCUMENT_TYPE_ID = 7;
      UPLOAD_DOCUMENT = 8;
      */
      // Don't know what to do with the remaining types and the client is certain of
      // what it wants, so just say we are going to do that.
      if (mime.contains(";")) {
        return mime.substring(0, mime.indexOf(';'));
      } else {
        return mime;
      }
    } else {
      return "*/*";
    }
  }

  @Override
  public File getDocumentTemporaryDirectory() {
    File dir = d.getTemporaryDirectory();
    if (dir == null) {
      return new File(System.getProperty("java.io.tmpdir"));
      
    } else {
      return dir;
    }
  }
}