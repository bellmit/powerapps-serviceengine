package com.profitera.services.business.batch;

import java.util.List;
import java.util.Map;

import com.profitera.deployment.rmi.DataArchivingServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.lookup.LookupManager;

public abstract class AbstractArchiveBatch extends AbstractBatchProcess {
  private static final String COMMITSIZE = "commitsize";
  protected static final String ARCHIVE_PROP = "archive";
  
  public AbstractArchiveBatch() {
    addRequiredProperty(ARCHIVE_PROP, String.class, "Archive file name", 
    "The name of the archive definition file, including the '.archive' extension.");
    addProperty(COMMITSIZE, Integer.class, "1", "Commit size of archiving process", 
    "The assigned commit size to use during archiving, setting the size of the transactions to be committed.");
  }
  
  @Override
  protected TransferObject invoke() {
    String archive = (String) getPropertyValue(ARCHIVE_PROP);
    DataArchivingServiceIntf archivingService = getArchivingService();
    TransferObject t = archivingService.getArchiveDefinitions();
    if (t.isFailed()) {
      return t;
    }
    Map<String, Object> found = findArchive(archive, t);
    if (found == null) {
      getLog().error("Specified archive not found: " + archive);
      return new TransferObject(new Object[]{archive}, TransferObject.ERROR, "ARCHIVE_NOT_FOUND");
    }
    Long archivePackageId = (Long) found.get("ID");
    return invoke(archive, archivePackageId, found, archivingService);
  }

  protected abstract TransferObject invoke(String archive, Long archivePackageId, Map<String, Object> found,
      DataArchivingServiceIntf archivingService);

  @SuppressWarnings("unchecked")
  private Map<String, Object> findArchive(String archive, TransferObject t) {
    List<Map<String, Object>> archives = (List<Map<String, Object>>) t.getBeanHolder();
    Map<String, Object> found = null;
    for (Map<String, Object> a : archives) {
      if (a.get("FILE_NAME").equals(archive)) {
        found = a;
      }
    }
    return found;
  }
  
  private DataArchivingServiceIntf getArchivingService() {
    LookupManager lm = LookupManager.getInstance();
    return (DataArchivingServiceIntf) lm.getLookupItem(LookupManager.BUSINESS, "DataArchivingService");
  }
  
  protected int getCommitSize() {
    return (Integer) getPropertyValue(COMMITSIZE);
  }

}
