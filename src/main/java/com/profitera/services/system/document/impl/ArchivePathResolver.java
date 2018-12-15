package com.profitera.services.system.document.impl;

import java.io.File;

import com.profitera.services.system.document.IArchivePathResolver;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.util.Strings;

public class ArchivePathResolver implements IArchivePathResolver {

	private final String rootPath;
  private final String extension;

  public ArchivePathResolver(String rootPath, String ext) {
  	if(rootPath==null) {
  		throw new IllegalArgumentException(IDocumentService.DOCUMENT_SERVICE_ARCHIVE_DIR +" not specified.");
  	}
    this.rootPath = Strings.ensureEndsWith(rootPath, "/");
    this.extension = ext;
  }
  
  /* (non-Javadoc)
   * @see com.profitera.services.system.document.impl.IArchivePathResolver#getPath(long)
   */
  public File getPath(long id) {
    String dir = getParentDirectories(id);
    return new File(rootPath + dir + id + extension);
  }

  private String getParentDirectories(long id) {
    String asText = id + "";
    return getParentDirectories(asText);
  }

  private String getParentDirectories(String asText) {
    if (asText.length() < 3) {
      return "";
    } else {
      String lastTwo = asText.substring(asText.length() - 2);
      return lastTwo + "/" + getParentDirectories(asText.substring(0, asText.length() - 2));
    }
  }

}
