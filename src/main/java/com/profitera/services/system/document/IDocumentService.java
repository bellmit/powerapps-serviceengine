package com.profitera.services.system.document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.services.system.dataaccess.DocumentService;
import com.profitera.services.system.dataaccess.IDocumentTransaction;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;


/**
 * @author jamison
 * This is not intended to provide complex relationships or
 * sophisticated query mechanisims, if you want that then maintain
 * your document meta-data somewhere else and query this for the exact
 * documents you want.
 * NB: <b>DO NOT EXPECT THIS TO PERFORM WELL!</b> It is for managing large
 * items one at a time, not for bulk operations.  
 */
public interface IDocumentService {

	public static final String IS_ENCODED = "IS_ENCODED";
	public static final String IS_ARCHIVED = "IS_ARCHIVED";
	public static final String FRAGMENT_WIDTH_PROP_NAME = "documentservice.fragmentcharacters";
	public static final String ENCODE_CHARACTER_DOCUMENTS_PROP_NAME = "documentservice.base64encodecharcterdocuments";
	public static final String DOCUMENT_SERVICE_CHARACTER_ENCODING = "documentservice.characterdocumentencoding";
	public static final String DOCUMENT_SERVICE_TEMP_DIR = "documentservice.temporarydirectory";
	public static final String DOCUMENT_SERVICE_ARCHIVE_DIR = "documentservice.archivedirectory";
	
  public static final long REPORT_DESIGN_DOCUMENT_TYPE_ID = 1;
  public static final long REPORT_INSTANCE_DOCUMENT_TYPE_ID = 2;
  public static final long TEMPLATE_DOCUMENT_TYPE_ID = 3;
  public static final long GENERATED_TEMPLATE_DOCUMENT_TYPE_ID = 4;
  public static final long IMAGE_DOCUMENT_TYPE_ID = 5;
  public static final long EMAIL_ATTACHMENT_DOCUMENT_TYPE_ID = 6;
  public static final long EMAIL_CONTENT_DOCUMENT_TYPE_ID = 7;
  public static final long UPLOAD_DOCUMENT = 8;
  public static final long DECISION_TREE_XML = 9;

  /**
   * The returned IDocumentTransaction will hold ANOTHER copy of
   * the document content, so you probably want to commit this
   * and dispose of it quickly.
   * @param documentType
   * @param description
   * @param content
   * @param p
   * @return
   */
  public abstract IDocumentTransaction createDocument(final long documentType,
      final String description, final CharSequence content,
      final IReadWriteDataProvider p);
  
  public abstract IDocumentTransaction createDocument(final long documentType,
      final String description, final Reader content,
      final IReadWriteDataProvider p);
  
  public abstract IDocumentTransaction createDocument(final long documentType,
      final String description, final InputStream content,
      final IReadWriteDataProvider p);

  public abstract IDocumentTransaction updateDocument(final Long documentId,
      final long documentType, final String description,
      final CharSequence content, final IReadWriteDataProvider p);

  public abstract IDocumentTransaction updateDocument(final Long documentId,
      final long documentType, final String description,
      final InputStream content, final IReadWriteDataProvider p);
  public abstract IDocumentTransaction updateDocument(final Long documentId, final long documentType,
      final String description, final Reader content, final IReadWriteDataProvider p);

  public abstract IDocumentTransaction deleteDocument(final Long documentId,
      final IReadWriteDataProvider p);

  /**
   * @param docId
   * @param p
   * @return null means doc not found, else the bytes that make up the document
   * @throws SQLException
   */
  public abstract byte[] getDocumentContent(Long docId, IReadWriteDataProvider p)
      throws SQLException, IOException;

  /**
   * If you know the document you are requesting is character data, then call this method.
   * It is marginally more memory-friendly because it can put the results strings straight
   * into a StringBuffer and also does not incur the Charset encoding overhead.  
   * @param docId
   * @param p
   * @return
   * @throws SQLException
   */
  public abstract StringBuffer getCharacterDocumentContent(Long docId,
      IReadOnlyDataProvider p) throws SQLException, IOException;

  /**
   * @param id
   * @return Does NOT return the doc content.
   * @throws SQLException 
   */
  public abstract IDocumentHeader getDocument(Long id, IReadOnlyDataProvider p)
      throws SQLException;

  /**
   * This method is provided so that documentservice related
   * generated files can be assinged a specific location on disk
   * if required and cleaned up easily. If there is no specified location
   * it uses the standard temp file mechanism.
   * @see DocumentService#DOCUMENT_SERVICE_TEMP_DIR 
   * @return 
   * A file that can be used to store document-related data
   * @throws IOException 
   */
  public File getTemporaryFile(String suffix) throws IOException;
  
  public String[] getDocumentLines(Long documentId, long startLine, int lineCount) throws SQLException, IOException;

  public abstract void archiveDocument(long documentId) throws IOException, SQLException, AbortTransactionException;

  public abstract InputStream getDocumentContentStream(long docId, IReadWriteDataProvider provider) throws IOException;

  public abstract long getDocumentLineCount(Long documentId) throws SQLException;
  public File getTemporaryDirectory();
}