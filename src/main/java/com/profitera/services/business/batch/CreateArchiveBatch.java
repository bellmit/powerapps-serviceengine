package com.profitera.services.business.batch;

import java.util.List;
import java.util.Map;

import com.profitera.deployment.rmi.DataArchivingServiceIntf;
import com.profitera.descriptor.business.TransferObject;

public class CreateArchiveBatch extends AbstractArchiveBatch {
  private static final String RANGE_QUERY_PROP = "rangequery";
  private static final String FAIL_ON_MAX_RECORDS_EXCEEDED_PROP = "failonmaxrecordsexceeded";
  private static final String DELETE_SOURCE_PROP = "deletesource";
  //
  private static final String MAXIMUM = "MAXIMUM";
  private static final String MAX_RECORD = "MAXIMUM_RECORD_COUNT";
  private static final String MIN_RECORD = "MINIMUM_RECORD_COUNT";
  
  public CreateArchiveBatch() {
    addRequiredProperty(RANGE_QUERY_PROP, String.class, "Query to establish the range for archiving and limits", 
        "This query must return a single row and the following columns in the response: "
        + "<variablelist>"
        + "<varlistentry><term>" + MAXIMUM + " (required)</term><listitem>" +
        		"<para>The maximum in the archiving range for this archiving session to be created, the data type of" +
        		" this value should match the data type of the range column.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + MIN_RECORD + " (optional)</term><listitem>" +
            "<para>The minimum number of records that must be archived in the specified range in order to trigger" +
            " an archiving process.</para></listitem></varlistentry>"
        + "<varlistentry><term>" + MAX_RECORD + " (optional)</term><listitem>" +
            "<para>The maximum number of records that are permitted to be archived in a single archiving process," +
            " if this number is exceeded archiving is not executed.</para></listitem></varlistentry>"
      + "</variablelist>");
    addProperty(DELETE_SOURCE_PROP, Boolean.class, Boolean.TRUE + "", 
        "If true the newly archived data is deleted from the source", 
        "If true the data that has just been successfully archived is deleted from the source tables, " +
        "this process runs immediately after archiving if the archiving was successful.");
    addProperty(FAIL_ON_MAX_RECORDS_EXCEEDED_PROP, Boolean.class, Boolean.TRUE + "", 
        "If true an error is returned when the number of records to archive exceeds the specified maximum", 
        "If true an error is returned when the number of records to archive exceeds the specified maximum.");
  }

  @Override
  protected String getBatchSummary() {
    return "Creates a new archiving process, executing the initial copy of the data from the source table to the Archive table";
  }

  @Override
  protected String getBatchDocumentation() {
    return "Creates a new archiving process based on the archive definition file specified by " + ARCHIVE_PROP + " based on" +
    		" the range maximum specifed from the query specified by " + RANGE_QUERY_PROP + ". It is important to note that ";
  }

  @Override
  protected TransferObject invoke(String archive, Long archivePackageId, Map<String, Object> found,
      DataArchivingServiceIntf service) {
    String rangeQuery = (String) getPropertyValue(RANGE_QUERY_PROP);
    boolean failOnOverMax = (Boolean) getPropertyValue(FAIL_ON_MAX_RECORDS_EXCEEDED_PROP);
    TransferObject archiveMinimumRange = service.getArchiveMinimumRange(archivePackageId);
    if (archiveMinimumRange.isFailed()) {
      return archiveMinimumRange;
    }
    Comparable min = (Comparable) archiveMinimumRange.getBeanHolder();
    if (min == null) {
      getLog().error("Specified archive returned no minimum rage value: " + archive);
      return new TransferObject(new Object[]{archive}, TransferObject.ERROR, "ARCHIVE_NO_MINIMUM_RANGE");
    }
    TransferObject queryList = getListQueryService().getQueryList(rangeQuery, found);
    if (queryList.isFailed()) {
      return queryList;
    }
    List maxList = (List) queryList.getBeanHolder();
    if (maxList.size() > 1) {
      getLog().error("Specified archive maximum query " + rangeQuery + " returned multiple values, one row expected");
      return new TransferObject(new Object[]{archive}, TransferObject.ERROR, "ARCHIVE_AMBIGUOUS_MAX_RANGE");
    } else if (maxList.size() == 0) {
      getLog().warn("Specified archive maximum query " + rangeQuery + " returned no value for " + archive + ", no archiving will be performed");
      return new TransferObject();
    }
    Map row = (Map) maxList.get(0);
    Comparable maxValue = (Comparable) row.get(MAXIMUM);
    Number minimumRecords = (Number) row.get(MIN_RECORD);
    Number maximumRecords = (Number) row.get(MAX_RECORD);
    Long count = null;
    {
      TransferObject recordCountInRange = service.getSelectedArchiveRecordCountInRange(archivePackageId, min, maxValue);
      if (recordCountInRange.isFailed()) {
        return recordCountInRange;
      }
      count = (Long) recordCountInRange.getBeanHolder();
      if (count == null) {
        count = 0L;
      }
    }
    if (count.longValue() == 0) {
      getLog().warn("Specified archive range from " + min + " to " + maxValue + " for " + archive + " would archive no records");
      return new TransferObject();      
    }
    if (minimumRecords != null && minimumRecords.longValue() > count) {
      getLog().warn("Specified archive range from " + min + " to " + maxValue + " for " + archive + " would archive " + count + " records, less than the minimum of " + minimumRecords);
      return new TransferObject();
    }
    if (maximumRecords != null && maximumRecords.longValue() < count) {
      String message = "Specified archive range from " + min + " to " + maxValue + " for " + archive + " would archive " + count + " records, more than the maximum of " + maximumRecords;
      if (failOnOverMax) {
        getLog().error(message);
        return new TransferObject(TransferObject.ERROR, "ARCHIVE_MAX_RECORD_COUNT_EXCEEDED");  
      } else {
        getLog().warn(message);
        return new TransferObject();
      }
    }
    TransferObject archived = service.startArchiving(archivePackageId, null, min, maxValue, getCommitSize(), true);
    if (archived.isFailed()) {
      return archived;
    }
    Boolean doDelete = (Boolean) getPropertyValue(DELETE_SOURCE_PROP);
    if (doDelete) {
      Long processId = (Long)archived.getBeanHolder();
      return service.startDeletingSource(archivePackageId, processId, getCommitSize(), true);
    } else {
      return archived;
    }
  }  
}
