package com.profitera.services.system.document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.io.Base64;
import com.profitera.io.MonitoringPipedInputStream;
import com.profitera.io.StreamUtil;
import com.profitera.io.TransferWithBuffer;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.map.MapUtil;
import com.profitera.server.ServiceEngine;
import com.profitera.services.system.SystemService;
import com.profitera.services.system.dataaccess.IDocumentTransaction;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.impl.ArchiveDocumentRetriever;
import com.profitera.services.system.document.impl.ArchivePathResolver;
import com.profitera.services.system.document.impl.DeleteDocumentTransaction;
import com.profitera.services.system.document.impl.DocumentArchiver;
import com.profitera.services.system.document.impl.DocumentFileArchiveVerifier;
import com.profitera.services.system.document.impl.DocumentLogClient;
import com.profitera.services.system.document.impl.DocumentRetriever;
import com.profitera.services.system.document.impl.InsertDocumentTransaction;
import com.profitera.services.system.document.impl.UpdateDocumentTransaction;
import com.profitera.util.Copy;
import com.profitera.util.io.FileUtil;

public class DocumentService extends SystemService implements IDocumentService {
  private static final Charset UTF8 = Charset.forName("UTF8");
  private static final String DOCUMENT_TYPE_ID = "DOCUMENT_TYPE_ID";
  private final ILogProvider log = new DefaultLogProvider();
  private int fragmentWidth = 2000;
  private static final String GET_DOCUMENT_SQL_NAME = "getDocument";
  public DocumentService() {
    log.register(new DocumentLogClient());
  }
  
  public IDocumentTransaction createDocument(final long documentType, final String description , final CharSequence content, final IReadWriteDataProvider p){
    if (isEncodingCharacterDocuments()){
      byte[] bytes;
      bytes = content.toString().getBytes(UTF8);
      Reader r = encode(bytes);
      return createDocument(documentType, description, true, r, p);
    } else {
      return createDocument(documentType, description, false, new StringReader(content.toString()), p);  
    }
  }
  
  public IDocumentTransaction createDocument(long documentType, String description, Reader content, IReadWriteDataProvider p) {
    if (isEncodingCharacterDocuments()){
      try {
        final File text = encode(content);
        Reader r = new InputStreamReader(new FileInputStream(text), UTF8);
        final IDocumentTransaction createDocument = createDocument(documentType, description, true, r, p);
        return new IDocumentTransaction(){

          public Long getId() {
            return createDocument.getId();
          }

          public void execute(ITransaction t) throws SQLException,
              AbortTransactionException {
            createDocument.execute(t);
            text.delete();
          }};
      } catch (IOException e) {
        throw new RuntimeException("Failed to encode document due to IO Exception", e);
      }
    } else {
      return createDocument(documentType, description, false, content, p);  
    }
  }

  private File encode(Reader content) throws IOException {
    File temp = getTemporaryFile(".enc");
    encodeReaderToFile(content, temp);
    return temp;
  }

  private static void encodeReaderToFile(Reader content, File destinationFile)
      throws FileNotFoundException, IOException, UnsupportedEncodingException {
    Base64.OutputStream os = new Base64.OutputStream(new FileOutputStream(destinationFile), Base64.ENCODE|Base64.GZIP|Base64.DONT_BREAK_LINES);
    char[] c = new char[1000];
    int read = content.read(c);
    while(read != -1){
      os.write(new String(c, 0, read).getBytes(UTF8));
      read = content.read(c);
    }
    os.close();
  }

  public IDocumentTransaction createDocument(long documentType, String description, InputStream bytes, IReadWriteDataProvider p) {
    Reader content = encode(bytes);
    return createDocument(documentType, description, true, content, p);
  }

  private Reader encode(byte[] bytes) {
    return encode(new ByteArrayInputStream(bytes));
  }
  private Reader encode(final InputStream in) {
    try {
      final MonitoringPipedInputStream base64 = new MonitoringPipedInputStream();
      final PipedOutputStream gzippedOut = new PipedOutputStream(base64);
      new Thread(new Runnable() {
        @Override
        public void run() {
          GZIPOutputStream out = null;
          try {
            out = new GZIPOutputStream(gzippedOut);
            new TransferWithBuffer().transfer(in, out, new byte[4096]);
            out.close();
          } catch (IOException e) {
            base64.setException(e);
          } finally {
            StreamUtil.closeFinally(out);
          }
        }
      }).start();
      return new InputStreamReader(new Base64.InputStream(base64, Base64.ENCODE | Base64.DONT_BREAK_LINES));
    } catch (Exception e) {
      throw new IllegalArgumentException("Unexpected failure to create GZip stream", e);
    }
  }

  private byte[] decode(StringBuffer content) {
    return Base64.decode(content.toString());
  }
  
  public IDocumentTransaction createDocument(final long documentType, final String description , final boolean isEncoded, final Reader content, final IReadWriteDataProvider p){
    return new InsertDocumentTransaction(documentType, description, isEncoded, content, p, getFragmentWidth());
  }

  private int getFragmentWidth() {
    fragmentWidth = ServiceEngine.getIntProp(FRAGMENT_WIDTH_PROP_NAME, 2000);
    return fragmentWidth;
  }
  
  private boolean isEncodingCharacterDocuments() {
    return ServiceEngine.getProp(ENCODE_CHARACTER_DOCUMENTS_PROP_NAME, "true").toUpperCase().startsWith("T");
  }
  
  public IDocumentTransaction updateDocument(final Long documentId, final long documentType, final String description , final CharSequence content, final IReadWriteDataProvider p){
    return updateDocument(documentId, documentType, description, false, new StringReader(content.toString()), p);
  }

  public IDocumentTransaction updateDocument(Long documentId, long documentType, String description, InputStream bytes, IReadWriteDataProvider p) {
    Reader content = encode(bytes);
    return updateDocument(documentId, documentType, description, true, content, p);
  }
  
  public IDocumentTransaction updateDocument(final Long documentId, final long documentType, final String description , final boolean isEncoded, final Reader content, final IReadWriteDataProvider p){
    return new UpdateDocumentTransaction(documentId, isEncoded, documentType,
        content, description, p, getFragmentWidth());
  }

  public IDocumentTransaction deleteDocument(final Long documentId, final IReadWriteDataProvider p) {
    return new DeleteDocumentTransaction(getFragmentWidth(), documentId, p);
  } 
  
  /**
   * @param docId
   * @param p
   * @return null means doc not found, else the bytes that make up the document
   * @throws SQLException
   * @throws IOException 
   */
  public byte[] getDocumentContent(Long docId, IReadWriteDataProvider p) throws SQLException, IOException {
    Object o = getDocumentContentAsObject(docId, p);
    if (o == null){
      return null;
    }
    if (o instanceof byte[]){
      return (byte[]) o;
    } else {
      return o.toString().getBytes(UTF8);
    }
  }
  
  /**
   * If you know the document you are requesting is character data, then call this method.
   * It is marginally more memory-friendly because it can put the results strings straight
   * into a StringBuffer and also does not incur the Charset encoding overhead.  
   * @param docId
   * @param p
   * @return
   * @throws SQLException
   * @throws IOException 
   */
  public StringBuffer getCharacterDocumentContent(Long docId, IReadOnlyDataProvider p) throws SQLException, IOException{
    Object o = getDocumentContentAsObject(docId, p);
    if (o == null){
      return null;
    }
    if (o instanceof byte[]){
      byte[] bytes = (byte[]) o;
      return new StringBuffer(new String(bytes, UTF8));
    } else if (o instanceof StringBuffer){
      return (StringBuffer) o; 
    } else if (o instanceof CharSequence){
      return new StringBuffer(((CharSequence)o).toString());
    }
    throw new IllegalArgumentException("getDocumentAsObject returned an object that could not be converted to a StringBuffer");
  }
  
  private Object getDocumentContentAsObject(Long docId, IReadOnlyDataProvider p) throws SQLException, IOException {
    IDocumentHeader document = getDocument(docId, p);
    boolean isEncoded = document.isEncoded();
    boolean isArchived = document.isArchived();
    String[] docLines = null;
    if (isArchived) {
    	IArchivePathResolver resolver = getPathResolver();
      IDocumentFileVerifier v = new DocumentFileArchiveVerifier(getDigester());
      ArchiveDocumentRetriever r = new ArchiveDocumentRetriever(docId, resolver, v);
      docLines = r.retrieveLines(0, Integer.MAX_VALUE);
    } else {
      DocumentRetriever r = new DocumentRetriever(docId, p);
      docLines = r.retrieveLines(0, Integer.MAX_VALUE);
    }
    StringBuffer content = new StringBuffer();
    for (int i = 0; i < docLines.length; i++) {
      content.append(docLines[i]); 
    }
    if (isEncoded){
      return decode(content);
    } else {
      return content;
    }
  }

  private IArchivePathResolver getPathResolver() throws IOException{
    return new ArchivePathResolver(getArchiveDirectory().getAbsolutePath(), ".arc");
  }
  
  /**
   * @param id
   * @return Does NOT return the doc content.
   * @throws SQLException 
   */
  public IDocumentHeader getDocument(final Long id, IReadOnlyDataProvider p) throws SQLException {
    Iterator<Map<String, Object>> i = getDocumentHeader(id, p);
    if (i.hasNext()) {
      final Map<String, Object> m = i.next();
      return new IDocumentHeader(){
        public long getId() {
          return id.longValue();
        }
        public boolean isArchived() {
          Object v = m.get(IS_ARCHIVED);
          if (v == null) {
            throw new IllegalArgumentException("Missing required return value 'IS_ARCHIVED' from " + GET_DOCUMENT_SQL_NAME);
          }
          return ((Boolean)v).booleanValue();
        }
        public boolean isEncoded() {
          Object v = m.get(IS_ENCODED);
          if (v == null) {
            throw new IllegalArgumentException("Missing required return value '" + IS_ENCODED + "' from " + GET_DOCUMENT_SQL_NAME);
          }
          return ((Boolean)v).booleanValue();
        }
        public long getType() {
          Object v = m.get(DOCUMENT_TYPE_ID);
          if (v == null) {
            throw new IllegalArgumentException("Missing required return value '" + DOCUMENT_TYPE_ID + "' from " + GET_DOCUMENT_SQL_NAME);
          }
          return (Long) v;          
        }};
    }
    throw new IllegalArgumentException("No document found in database for document ID " + id);
  }

  @SuppressWarnings("unchecked")
  private Iterator<Map<String, Object>> getDocumentHeader(final Long id, IReadOnlyDataProvider p) throws SQLException {
    return p.query(IReadOnlyDataProvider.LIST_RESULTS, GET_DOCUMENT_SQL_NAME, id);
  }

  public static void main(String[] args) throws FileNotFoundException, IOException {
    if (args.length < 1){
      System.err.println("File for reading Base64 encoded text required");
      return;
    }
    if (args.length < 2){
      System.err.println("File for writing decoded text required");
      return;
    }
    File destination = File.createTempFile("report", "-xxxxx" + ".xml");
    if (Base64.decodeAndStoreFile(args[0], destination.getAbsolutePath())){
      File temp = File.createTempFile("report", "-xxxxy" + ".xml");
      Base64.unzipAndStoreFile(destination.getAbsolutePath(), temp.getAbsolutePath());
      destination.delete();
      destination = temp;
    }
    Copy.toFile(destination, new File(args[1]));
    destination.delete();
  }
  public File getTemporaryDirectory() {
    String path = ServiceEngine.getProp(DOCUMENT_SERVICE_TEMP_DIR);
    File dir = null;
    if (path != null && path.trim().length() > 0){
      dir = new File(path);
      if (!(dir.exists() && dir.isDirectory())){
        dir = null;
      }
    }
    return dir;
  }

  public File getTemporaryFile(String suffix) throws IOException {
    File dir = getTemporaryDirectory();
    if (dir != null){
      try {
        return File.createTempFile("docser-", suffix, dir);
      } catch (IOException e) {
        log.emit(DocumentLogClient.TEMP_BAD, e, DOCUMENT_SERVICE_TEMP_DIR, FileUtil.tryCanonical(dir));
      }
    }
    return File.createTempFile("docser-", suffix);
  }
  
  public String[] getDocumentLines(Long documentId, long startLine, int lineCount) throws SQLException, IOException {
    IReadWriteDataProvider p = getReadWriteProvider();
    IDocumentHeader h = getDocument(documentId, p);
    if (h.isArchived()) {
      IDocumentFileVerifier v = new DocumentFileArchiveVerifier(getDigester());
      return new ArchiveDocumentRetriever(documentId, getPathResolver(), v).retrieveLines(startLine, lineCount);   
    } else {
      return new DocumentRetriever(documentId, p).retrieveLines(startLine, lineCount);
    }
  }
  
  public void archiveDocument(final long documentId) throws IOException, SQLException, AbortTransactionException {
    File archiveDirectory = getArchiveDirectory();
    File swap = new File(archiveDirectory, "archiveswap.tmp");
    MessageDigest digest = getDigester(); 
    IArchivePathResolver archivePathResolver = getPathResolver();
    DocumentRetriever retriever = new DocumentRetriever(documentId, getReadWriteProvider());
    DocumentArchiver archiver2 = new DocumentArchiver(digest, archivePathResolver, retriever, 250, log);
    archiver2.archive(documentId, swap, getReadWriteProvider());
  }

  private MessageDigest getDigester() throws IOException {
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new IOException(e.getMessage());
    }
    return digest;
  }
  
  private File getArchiveDirectory() throws IOException {
    String path = ServiceEngine.getProp(DOCUMENT_SERVICE_ARCHIVE_DIR);
    if (path == null) {
    	throw new IOException("Archive path not specified by " + DOCUMENT_SERVICE_ARCHIVE_DIR);
    } else {
      File dir = new File(path);
      if (!dir.exists()) {
        throw new IOException("Archive path specified by " + DOCUMENT_SERVICE_ARCHIVE_DIR + "(" + path + ") does not exist");
      } else if (!dir.isDirectory()){
        throw new IOException("Archive path specified by " + DOCUMENT_SERVICE_ARCHIVE_DIR + "(" + path + ") is not a directory");
      } else {
        return dir;
      }
    }
  }

  @Override
  public InputStream getDocumentContentStream(final long docId, IReadWriteDataProvider p) throws IOException {
    final MonitoringPipedInputStream docOut = new MonitoringPipedInputStream();
    final PipedOutputStream out = new PipedOutputStream(docOut);
    new Thread(new Runnable() {
        @Override
        public void run() {
          Base64.OutputStream b64 = new Base64.OutputStream(out, Base64.DECODE);
          try {
            int i = 0;
            boolean go = true;
            while (go) {
              String[] documentLines = getDocumentLines(docId, i, 100);
              if (documentLines.length < 100) {
                go = false;
              }
              for (int j = 0; j < documentLines.length; j++) {
                b64.write(documentLines[j].getBytes(UTF8));
              }
              i = i + documentLines.length;
            }
          } catch (IOException e) {
            docOut.setException(e);
          } catch (SQLException e) {
            docOut.setException(new IOException(e));
          } finally {
            StreamUtil.closeFinally(b64);
            StreamUtil.closeFinally(out);
          }
        }
    }).start();
    return new GZIPInputStream(docOut);
  }

  @Override
  public IDocumentTransaction updateDocument(Long documentId, long documentType, String description, Reader content,
      IReadWriteDataProvider p) {
    boolean encodingCharacterDocuments = isEncodingCharacterDocuments();
    if (encodingCharacterDocuments) {
      File encodedContentFile = null;
      Reader encodedContentReader = null;
      try {
        encodedContentFile = encode(content);
        encodedContentReader = new FileReader(encodedContentFile);
        return updateDocument(documentId, documentType, description, true, encodedContentReader, p);
      } catch (IOException e) {
        throw new IllegalArgumentException("Unexpected failure to create GZip stream", e);
      } finally {
        if (encodedContentFile != null) {
          encodedContentFile.delete();
        }
        StreamUtil.closeFinally(encodedContentReader);
      }
    } else {
      return updateDocument(documentId, documentType, description, encodingCharacterDocuments, content, p);
    }
  }

  @Override
  public long getDocumentLineCount(Long documentId) throws SQLException {
    IReadWriteDataProvider p = getReadWriteProvider();
    return (Long) p.queryObject("getDocumentFragmentCount", MapUtil.create("DOCUMENT_ID", documentId));
  }
}