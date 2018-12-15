package com.profitera.services.business.batch;

import java.util.List;
import java.util.Map;

import com.profitera.deployment.rmi.DataArchivingServiceIntf;
import com.profitera.descriptor.business.TransferObject;

public class DeleteArchiveBatch extends AbstractArchiveBatch {
  
  public DeleteArchiveBatch() {
    addProperty("tableset", String.class, null, "The tableset to delete", 
        "If not specified the source data is deleted, otherwise the tableset named is deleted.");
    addRequiredProperty("processidquery", String.class, "A list of one or more archive process Ids from which to delete data", 
        "A query that must return the a single column 'ARCHIVE_PROCESS_ID' which specifies " +
        "the archive processes that should be deleted in this batch execution.");
  }

  @Override
  protected String getBatchDocumentation() {
    return "Archived data is deleted from the tableset specified or from the source data. " +
    		"A query, specified by batch property, dictates exactly which archive process instance " +
    		"will be processed.";
  }

  @Override
  protected String getBatchSummary() {
    return "Deletes archived data from a tableset";
  }

  @Override
  protected TransferObject invoke(String archive, Long archivePackageId, Map<String, Object> found,
      DataArchivingServiceIntf archivingService) {
    String tableSet = (String) getPropertyValue("tableset");
    String query = (String) getPropertyValue("processidquery");
    TransferObject queryList = getListQueryService().getQueryList(query, found);
    if (queryList.isFailed()) {
      return queryList;
    }
    List<Map> processes = (List) queryList.getBeanHolder();
    for (Map row : processes) {
      Long archiveProcessId = (Long) row.get("ARCHIVE_PROCESS_ID");
      TransferObject to;
      if (tableSet == null) {
        to = archivingService.startDeletingSource(archivePackageId, archiveProcessId, getCommitSize(), true);
      } else {
        to = archivingService.startDeleting(archivePackageId, archiveProcessId, tableSet, true);  
      }
      if (to.isFailed()) {
        return to;
      }
    }
    return new TransferObject();
  }
}
