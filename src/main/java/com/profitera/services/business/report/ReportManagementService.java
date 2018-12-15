package com.profitera.services.business.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.deployment.rmi.ReportManagementServiceIntf;
import com.profitera.deployment.rmi.ScheduleListenerIntf;
import com.profitera.deployment.rmi.SchedulingServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.IReportBatch;
import com.profitera.descriptor.business.meta.IReportBatchRel;
import com.profitera.descriptor.business.meta.IReportDesign;
import com.profitera.descriptor.business.meta.IReportInstance;
import com.profitera.descriptor.business.meta.IUser;
import com.profitera.descriptor.business.schedule.CronSchedule;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.system.dataaccess.IDocumentTransaction;
import com.profitera.services.system.dataaccess.IReadOnlyDataProvider;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.services.system.report.IReportGenerationService;
import com.profitera.services.system.report.IReportOutputGenerator;
import com.profitera.services.system.report.ReportCompilationException;
import com.profitera.services.system.report.ReportGenerationException;
import com.profitera.services.system.report.ReportOutputGeneratorFactory;
import com.profitera.util.exception.DatabaseQueryException;
import com.profitera.util.exception.GenericException;
import com.profitera.util.exception.InvalidArgumentException;

public class ReportManagementService extends ProviderDrivenService implements ReportManagementServiceIntf,
    ScheduleListenerIntf {
  public static String MY_SERVICE_NAME = "ReportManagementService";
  private final static Log log = LogFactory.getLog(ReportManagementService.class);
  private final static String defaultTransferSize = "1000000";
  private static final String MAXIMUM_TRANSFER_SIZE = "reportmanagementservice.maximumtransfersize";
  private static final String MAX_STORE_SIZE = "reportmanagementservice.maximumreportstoragesize";
  private final static int TICK_SIZE = 60 * 1000;
  private static final long MAXIMUM_REPORT_STORAGE_PERIOD = 5 * 60 * 1000;
  private static final String TIMESTAMP = "TIMESTAMP";
  private static final String INPUT_STREAM = "INPUT_STREAM";
  private Integer maximumTransferSize;
  private Map reportStorage;
  private Timer tickTimer;

  public ReportManagementService() {
    reportStorage = new HashMap();
    tickTimer = new Timer();
    tickTimer.scheduleAtFixedRate(new TimerTask() {
      public void run() {
        try {
          if (!reportStorage.isEmpty())
            removeUnclaimedReports();
        } catch (Exception e) {
          log.error("Scheduled timer for removing unclaimed report firing failed.", e);
        }
      }
    }, TICK_SIZE, TICK_SIZE);
  }

  private void removeUnclaimedReports() {
    long currentTime = System.currentTimeMillis();
    List idsToBeRemoved = new ArrayList();
    for (Iterator ids = reportStorage.keySet().iterator(); ids.hasNext();) {
      Object id = ids.next();
      if (currentTime - ((Long) ((Map) reportStorage.get(id)).get(TIMESTAMP)).longValue() > MAXIMUM_REPORT_STORAGE_PERIOD) {
        idsToBeRemoved.add(id);
      }
    }
    for (int i = 0; i < idsToBeRemoved.size(); i++) {
      Map storageMap = (Map) reportStorage.get(idsToBeRemoved.get(i));
      FileInputStream fis = (FileInputStream) storageMap.get(INPUT_STREAM);
      try {
        fis.close();
      } catch (IOException e) {
        log.error("Unable to close file streaming for report id '" + idsToBeRemoved.get(i) + "'", e);
      }
      reportStorage.remove(idsToBeRemoved.get(i));
    }
  }

  @Override
  public TransferObject getReportDesigns(Long groupId) {
    IReadOnlyDataProvider provider = getReadOnlyProvider();
    try {
      Iterator designs = provider.query(IReadOnlyDataProvider.LIST_RESULTS, "getReportDesigns", groupId);
      List reportDesigns = new ArrayList();
      while (designs.hasNext()) {
        Map design = (Map) designs.next();
        reportDesigns.add(design);
      }
      return new TransferObject(reportDesigns);
    } catch (SQLException e) {
      GenericException exception = new DatabaseQueryException("Unable to query report designs.", e);
      log(null, null, exception);
      return getExceptionTransferObject(exception);
    }
  }

  @Override
  public TransferObject scheduleReport(Long reportId, String schedule, String userId) {
    if (reportId == null)
      return getExceptionTransferObject(new InvalidArgumentException("Report design id is null"));
    if (userId == null)
      return getExceptionTransferObject(new InvalidArgumentException("User id is null."));
    IReadOnlyDataProvider provider = getReadOnlyProvider();
    Map reportDesign = null;
    try {
      reportDesign = getReportDesignById(reportId);
    } catch (SQLException e) {
      GenericException ex = new DatabaseQueryException(e);
      log(null, reportId.toString(), ex);
      return getExceptionTransferObject(ex);
    }

    try {
      Map newBatch = (Map) provider.queryObject("getReportBatchByName", reportDesign.get(IReportDesign.ID).toString());
      if (newBatch == null)
        newBatch = createAutoBatch(reportDesign, userId);
      SchedulingServiceIntf scheduler = getSchedulingService();
      if (schedule == null || schedule.trim().length() == 0) {
        TransferObject schedremoved = scheduler.removeEventSchedule(ServiceEngine.getBaseServerURL(), MY_SERVICE_NAME,
            (String) newBatch.get(IReportBatch.NAME));
        if (schedremoved.isFailed())
          return new TransferObject(reportDesign, schedremoved.getFlag(), schedremoved.getMessage());
      } else {
        CronSchedule cron = new CronSchedule(schedule);
        TransferObject scheduled = scheduler.scheduleEvent(ServiceEngine.getBaseServerURL(), MY_SERVICE_NAME,
            (String) newBatch.get(IReportBatch.NAME), cron);
        if (scheduled.isFailed())
          return new TransferObject(reportDesign, scheduled.getFlag(), scheduled.getMessage());
        reportDesign.put(IReportDesign.SCHEDULE, schedule);
      }
      return new TransferObject(reportDesign);
    } catch (GenericException e) {
      log(null, reportId.toString(), e);
      return getExceptionTransferObject(e);
    } catch (SQLException e) {
      GenericException ex = new DatabaseQueryException(e);
      log(null, reportId.toString(), ex);
      return getExceptionTransferObject(ex);
    }
  }

  @Override
  public TransferObject unscheduleReport(Long reportId) {
    if (reportId == null)
      return getExceptionTransferObject(new InvalidArgumentException("Report design id is null"));
    Map reportDesign = null;
    try {
      reportDesign = getReportDesignById(reportId);
    } catch (SQLException e) {
      GenericException ex = new DatabaseQueryException(e);
      log(null, reportId.toString(), ex);
      return getExceptionTransferObject(ex);
    }
    SchedulingServiceIntf scheduler = getSchedulingService();
    TransferObject to = scheduler.removeEventSchedule(ServiceEngine.getBaseServerURL(), MY_SERVICE_NAME,
        getReportDesignSchedulingId(reportDesign));
    if (to.isFailed())
      return to;
    return getReportDesign(reportId);
  }

  private String getReportDesignSchedulingId(Map reportDesign) {
    Long id = (Long) reportDesign.get(IReportDesign.ID);
    return id.toString();
  }

  private Map createAutoBatch(final Map reportDesign, String userId) throws AbortTransactionException, SQLException {
    final IReadWriteDataProvider provider = getReadWriteProvider();
    final Long reportId = (Long) reportDesign.get(IReportDesign.ID);
    final Map newBatch = new HashMap();
    newBatch.put(IReportBatch.CREATED_BY, userId);
    newBatch.put(IReportBatch.CREATED_DATE, new Date());
    newBatch.put(IReportBatch.DESCRIPTION, "Auto batch for report " + reportDesign.get(IReportDesign.NAME));
    newBatch.put(IReportBatch.NAME, ((Long) reportDesign.get(IReportDesign.ID)).toString());

    final Map newRepBatchRel = new HashMap();
    newRepBatchRel.put(IReportBatchRel.REPORT_DESIGN_ID, reportId);
    newRepBatchRel.put(IReportBatchRel.SEQUENCE, new Integer(0));
    provider.execute(new IRunnableTransaction() {
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        Long batchId = (Long) provider.insert("insertReportBatch", newBatch, t);
        newRepBatchRel.put(IReportBatchRel.BATCH_ID, batchId);
        provider.insert("addReportForBatch", newRepBatchRel, t);
        newBatch.put(IReportBatch.ID, batchId);
      }
    });
    return newBatch;
  }

  @Override
  public void invokeScheduledEvent(String id) {
    TransferObject to = generateReport(new Long(id), new HashMap());
    if (to.isFailed()) {
      throw new RuntimeException("Report generation failed: " + to.getMessage());
    }
  }

  private File generateReportToFile(Map design, Map arguments, File f, String format) throws TransferObjectException,
      ReportCompilationException, ReportGenerationException, IOException, SQLException {
    Long reportId = (Long) design.get(IReportDesign.ID);
    String content = getReportDesignContentByReportId(reportId);
    if (content == null) {
      log.error(design.get(IReportDesign.NAME) + " does not have design, can't generate.");
      throw new TransferObjectException(getErrorTransferObject("No valid design template available for report "
          + reportId));
    }
    String generatedReport = generateReportInternal(reportId, content, arguments, format, f);
    File file = new File(generatedReport);
    return file;
  }

  public TransferObject generateReport(Long reportId, Map arguments, File dest, String format) {
    if (!isValidFormat(format)) {
      return new TransferObject(TransferObject.ERROR, "INVALID_OUTPUT_FORMAT");
    }
    try {
      Map design;
      design = getReportDesignById(reportId);
      generateReportToFile(design, arguments, dest, format);
      return new TransferObject();
    } catch (SQLException e) {
      GenericException ex = new DatabaseQueryException(e);
      log(null, reportId.toString(), ex);
      return getExceptionTransferObject(ex);
    } catch (ReportCompilationException e) {
      log(null, reportId.toString(), e);
      return getExceptionTransferObject(e);
    } catch (ReportGenerationException e) {
      log(null, reportId.toString(), e);
      return getExceptionTransferObject(e);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    } catch (IOException e) {
      GenericException ex = new GenericException("IOException while trying to generate report " + reportId, e);
      log(null, reportId.toString(), ex);
      return getExceptionTransferObject(ex);
    }
  }

  private boolean isValidFormat(String format) {
    if (format == null) {
      return false;
    } else if (format.equals(XML_FORMAT)) {
      return true;
    } else if (format.equals(PDF_FORMAT)) {
      return true;
    } else if (format.equals(XLS_FORMAT)) {
      return true;
    }
    return false;
  }

  @Override
  public TransferObject generateReport(Long reportId, Map arguments) {
    File generatedReportFile = null;
    try {
      Map design = getReportDesignById(reportId);
      String reportDesignName = (String) design.get(IReportDesign.NAME);
      generatedReportFile = generateReportToFile(design, arguments, null, XML_FORMAT);
      //
      long byteSize = generatedReportFile.length();
      long megSize = byteSize / 1024 / 1024;
      long maxMegSize = getMaximumReportFileSize();
      if (maxMegSize < megSize) {
        log.error("Unable to store report, size of " + megSize + " exceeds maximum of " + maxMegSize
            + "(adjust/override using property " + MAX_STORE_SIZE + ")");
        return new TransferObject(TransferObject.ERROR, "REPORT_EXCEEDS_MAX_SIZE");
      }
      //
      FileInputStream fis = new FileInputStream(generatedReportFile);
      ReportOutputGeneratorFactory instance = ReportOutputGeneratorFactory.getInstance();
      String generatorType = ServiceEngine.getProp(ReportOutputGeneratorFactory.REPORT_OUTPUT_GENERATOR, "NULL");
      IReportOutputGenerator og = instance.getReportOutputGenerator(generatorType);
      og.outputReport(reportId, reportDesignName, fis);
      fis.close();
      InputStreamReader is = new InputStreamReader(new FileInputStream(generatedReportFile), "UTF8");
      Map newReportInstance = storeReportInstanceInDatabase(reportId, is);
      is.close();
      return new TransferObject(newReportInstance);
    } catch (SQLException e) {
      GenericException ex = new DatabaseQueryException(e);
      log(null, reportId.toString(), ex);
      return getExceptionTransferObject(ex);
    } catch (ReportCompilationException e) {
      log(null, reportId.toString(), e);
      return getExceptionTransferObject(e);
    } catch (IOException e) {
      GenericException ex = new GenericException("IOException while trying to generate report " + reportId, e);
      log(null, reportId.toString(), ex);
      return getExceptionTransferObject(ex);
    } catch (ReportGenerationException e) {
      log(null, reportId.toString(), e);
      return getExceptionTransferObject(e);
    } catch (TransferObjectException e) {
      GenericException ex = new GenericException(e);
      log(null, reportId.toString(), ex);
      return getExceptionTransferObject(ex);
    } catch (AbortTransactionException e) {
      log(null, reportId.toString(), e);
      return getExceptionTransferObject(e);
    } finally {
      if (generatedReportFile != null) {
        generatedReportFile.delete();
      }
    }
  }

  private long getMaximumReportFileSize() {
    return ServiceEngine.getIntProp(MAX_STORE_SIZE, 20 * 1024 * 1024);
  }

  private Map storeReportInstanceInDatabase(final Long reportId, final Reader content)
      throws AbortTransactionException, SQLException {
    final IReadWriteDataProvider readWriter = getReadWriteProvider();
    final Map reportInstance = new HashMap();
    reportInstance.put(IReportInstance.GENERATION_DATE, new Date());
    reportInstance.put(IReportInstance.REPORT_DESIGN_ID, reportId);
    readWriter.execute(new IRunnableTransaction() {
      public void execute(ITransaction t) throws SQLException, AbortTransactionException {
        IDocumentService docService = getDocumentService();
        IDocumentTransaction transaction = docService.createDocument(IDocumentService.REPORT_INSTANCE_DOCUMENT_TYPE_ID,
            "Report " + reportId, content, readWriter);
        transaction.execute(t);
        reportInstance.put(IReportInstance.DOCUMENT_ID, transaction.getId());
        Long id = (Long) readWriter.insert("insertReportInstance", reportInstance, t);
        reportInstance.put(IReportInstance.ID, id);
      }
    });
    return reportInstance;
  }

  private String generateReportInternal(Long reportId, String content, Map arguments, String format, File dest)
      throws ReportCompilationException, IOException, SQLException, ReportGenerationException, TransferObjectException {
    IReportGenerationService generator = getReportGenerationService();
    IDataSourceConfiguration defaultDataSource = getDataSourceConfigurations().getDefaultDataSource();
    if (dest == null) {
      return generator.generateReport(reportId, content, arguments, format, defaultDataSource);
    } else {
      return generator.generateReport(reportId, content, arguments, dest, format, defaultDataSource);
    }
  }

  @Override
  public TransferObject updateReportDesign(TransferObject contentValues) {
    if (contentValues == null)
      return null;
    Map changes = (Map) contentValues.getBeanHolder();
    if (changes.get(IReportDesign.ID) == null)
      return getExceptionTransferObject(new InvalidArgumentException("Report design id is null."));
    if (changes.get(IReportDesign.CONTENT) == null
        || ((String) changes.get(IReportDesign.CONTENT)).trim().length() == 0)
      return getExceptionTransferObject(new InvalidArgumentException("Report content is null or empty."));
    if (changes.get(IUser.USER_ID) == null || ((String) changes.get(IUser.USER_ID)).trim().length() == 0)
      return getExceptionTransferObject(new InvalidArgumentException("Report design modifier id is null or empty."));
    TransferObject queriedReportDesign = getReportDesign((Long) changes.get(IReportDesign.ID));
    final Map modified = (Map) queriedReportDesign.getBeanHolder();
    try {
      final IReadWriteDataProvider readWriter = getReadWriteProvider();
      final IDocumentService docService = getDocumentService();
      final String content = updateSubreportLocations((String) changes.get(IReportDesign.CONTENT),
          (Map) changes.get(SUBREPORT_MAP_KEY));
      modified.put(IReportDesign.MODIFIED_BY, changes.get(IUser.USER_ID));
      modified.put(IReportDesign.MODIFIED_DATE, new Date());
      readWriter.execute(new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          Long documentId = (Long) modified.get(IReportDesign.DOCUMENT_ID);
          IDocumentTransaction transaction = null;
          if (documentId != null)
            transaction = docService.updateDocument(documentId, IDocumentService.REPORT_DESIGN_DOCUMENT_TYPE_ID,
                "Report design template for " + modified.get(IReportDesign.NAME), content, readWriter);
          else
            transaction = docService.createDocument(IDocumentService.REPORT_DESIGN_DOCUMENT_TYPE_ID,
                "Report design template for " + modified.get(IReportDesign.NAME), content, readWriter);
          transaction.execute(t);
          modified.put(IReportDesign.DOCUMENT_ID, transaction.getId());
          readWriter.update("setReportDesignModified", modified, t);
          modified.put(IReportDesign.CONTENT, content);
        }
      });
    } catch (GenericException e) {
      log(null, "" + changes.get(IReportDesign.ID), e);
      return getExceptionTransferObject(e);
    } catch (SQLException e) {
      GenericException ex = new DatabaseQueryException(e);
      log(null, "" + changes.get(IReportDesign.ID), ex);
      return getExceptionTransferObject(ex);
    }
    return new TransferObject(modified);
  }

  private String updateSubreportLocations(String content, Map subreportAssignments) throws GenericException {
    if (content == null || content.trim().length() == 0)
      return null;
    // added for convenience, if the report do not have any sub reports, we
    // don't have to risk
    // null pointer exceptions.
    if (subreportAssignments == null)
      subreportAssignments = new HashMap();
    Map subreportIds = new HashMap();
    for (Iterator i = subreportAssignments.entrySet().iterator(); i.hasNext();) {
      Map.Entry r = (Map.Entry) i.next();
      subreportIds.put(r.getKey(), ((Map) r.getValue()).get(IReportDesign.ID));
    }
    try {
      content = getReportGenerationService().replaceDesignSubreportLocations(content, subreportIds);
    } catch (Exception e) {
      throw new GenericException(e);
    }
    return content;
  }

  /*
   * This method just returns the same map, with additional key IReportDesign.ID
   * and IReportDesign.DOCUMENT_ID It does not however requery after the
   * succesful insert to provide a fresh copy
   */
  private TransferObject addNewReportDesign(TransferObject report) {
    if (report == null)
      return null;
    Map reportDesignValues = (Map) report.getBeanHolder();
    if (reportDesignValues == null || reportDesignValues.isEmpty())
      return getExceptionTransferObject(new InvalidArgumentException("Report design values map is null or empty."));
    if (!reportDesignValues.containsKey(IReportDesign.NAME) || reportDesignValues.get(IReportDesign.NAME) == null
        || ((String) reportDesignValues.get(IReportDesign.NAME)).trim().length() == 0)
      return getExceptionTransferObject(new InvalidArgumentException("Report design name is not provided."));
    if (!reportDesignValues.containsKey(IUser.USER_ID) || reportDesignValues.get(IUser.USER_ID) == null
        || ((String) reportDesignValues.get(IUser.USER_ID)).trim().length() == 0)
      return getExceptionTransferObject(new InvalidArgumentException("Report design creator id is not provided."));
    reportDesignValues.put(IReportDesign.CREATED_BY, reportDesignValues.get(IUser.USER_ID));
    try {
      insertNewReportDesign(reportDesignValues);
      return new TransferObject(reportDesignValues);
    } catch (GenericException e) {
      log(null, (String) reportDesignValues.get(IReportDesign.NAME), e);
      return getExceptionTransferObject(e);
    }
  }

  /*
   * This method just returns the same map, with additional key IReportDesign.ID
   * and IReportDesign.DOCUMENT_ID It does not however requery after the
   * succesful insert to provide a fresh copy
   */
  @Override
  public TransferObject addNewReportDesignWithContent(TransferObject report) {
    if (report == null)
      return null;
    Map reportDesignValues = (Map) report.getBeanHolder();
    if (reportDesignValues == null || reportDesignValues.isEmpty())
      return getExceptionTransferObject(new InvalidArgumentException("Report design values map is null or empty."));
    if (!reportDesignValues.containsKey(IReportDesign.CONTENT) || reportDesignValues.get(IReportDesign.CONTENT) == null
        || ((String) reportDesignValues.get(IReportDesign.CONTENT)).trim().length() == 0)
      return getExceptionTransferObject(new InvalidArgumentException("Report design content is not provided."));
    return addNewReportDesign(report);
  }

  private Long insertNewReportDesign(final Map reportDesignValues) throws GenericException {
    final IReadWriteDataProvider readWriter = getReadWriteProvider();
    reportDesignValues.put(IReportDesign.CREATED_DATE, new Date());
    reportDesignValues.put(IReportDesign.MODIFIED_BY, null);
    reportDesignValues.put(IReportDesign.MODIFIED_DATE, null);
    if (!reportDesignValues.containsKey(IReportDesign.DESCRIPTION))
      reportDesignValues.put(IReportDesign.DESCRIPTION, null);
    final String content = reportDesignValues.containsKey(IReportDesign.CONTENT) ? updateSubreportLocations(
        (String) reportDesignValues.get(IReportDesign.CONTENT), (Map) reportDesignValues.get(SUBREPORT_MAP_KEY)) : null;
    try {
      readWriter.execute(new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          if (content != null) {
            IDocumentService docService = getDocumentService();
            IDocumentTransaction transaction = docService.createDocument(
                IDocumentService.REPORT_DESIGN_DOCUMENT_TYPE_ID,
                "Report design template for " + reportDesignValues.get(IReportDesign.NAME), content, readWriter);
            transaction.execute(t);
            reportDesignValues.put(IReportDesign.DOCUMENT_ID, transaction.getId());
          }
          Long id = (Long) readWriter.insert("insertReportDesign", reportDesignValues, t);
          reportDesignValues.put(IReportDesign.ID, id);
        }
      });
    } catch (SQLException e) {
      throw new DatabaseQueryException(e);
    }
    return (Long) reportDesignValues.get(IReportDesign.ID);
  }

  @Override
  public TransferObject getReportDesign(Long reportId) {
    if (reportId == null)
      return getExceptionTransferObject(new InvalidArgumentException("Report id is null."));
    try {
      Map reportDesign = getReportDesignById(reportId);
      return new TransferObject(reportDesign);
    } catch (SQLException e) {
      GenericException exception = new DatabaseQueryException(
          "Unable to query report design with id " + reportId + ".", e);
      log(null, null, exception);
      return getExceptionTransferObject(exception);
    }
  }

  private Map getReportDesignById(Long reportId) throws SQLException {
    IReadOnlyDataProvider provider = getReadOnlyProvider();
    Map design = (Map) provider.queryObject("getReportDesignById", reportId);
    return design;
  }

  private String getReportDesignContentByReportId(Long reportId) throws SQLException, IOException {
    IReadOnlyDataProvider provider = getReadOnlyProvider();
    Long docId = (Long) provider.queryObject("getReportDesignDocumentId", reportId);
    IDocumentService docService = getDocumentService();
    StringBuffer content = docService.getCharacterDocumentContent(docId, getReadWriteProvider());
    if (content != null)
      return content.toString();
    return null;
  }

  @Override
  public TransferObject getReportInstancesBetweenDates(Date startDate, Date endDate, Long groupId) {
    if (startDate == null || endDate == null)
      return getExceptionTransferObject(new InvalidArgumentException("Start date/End date is null."));
    Map param = new HashMap();
    param.put("START_DATE", startDate);
    param.put("END_DATE", endDate);
    param.put("REPORT_GROUP", groupId);
    IReadOnlyDataProvider provider = getReadOnlyProvider();
    try {
      Iterator instances = provider.query(IReadOnlyDataProvider.LIST_RESULTS, "getReportInstancesBetweenDates", param);
      List reportInstances = new ArrayList();
      while (instances.hasNext()) {
        Map tmpInstance = (Map) instances.next();
        Map design = getReportDesignById((Long) tmpInstance.get(IReportInstance.REPORT_DESIGN_ID));
        tmpInstance.put(IReportDesign.NAME, design.get(IReportDesign.NAME));
        reportInstances.add(tmpInstance);
      }
      return new TransferObject(reportInstances);
    } catch (SQLException e) {
      GenericException exception = new DatabaseQueryException("Unable to query report instances between dates.", e);
      log(null, null, exception);
      return getExceptionTransferObject(exception);
    }
  }

  @Override
  public TransferObject getReportDesignSubreportNames(TransferObject designContent) {
    String content = (String) designContent.getBeanHolder();
    try {
      String[] subreportArray = getDesignSubreportValues(content);
      return new TransferObject(subreportArray);
    } catch (TransferObjectException e) {
      return e.getTransferObject();
    }
  }

  private String[] getDesignSubreportValues(String content) throws TransferObjectException {
    return getReportGenerationService().getDesignSubreportLocations(content);
  }

  @Override
  public TransferObject getReportInstanceContent(Long reportId) {
    if (reportId == null)
      return getExceptionTransferObject(new InvalidArgumentException("Report id is null."));
    IReadOnlyDataProvider provider = getReadOnlyProvider();
    try {
      Long docId = (Long) provider.queryObject("getReportInstanceDocumentId", reportId);
      IDocumentService docService = getDocumentService();
      StringBuffer content = docService.getCharacterDocumentContent(docId, getReadWriteProvider());
      if (content == null) {
        return null;
      } else {
        return new TransferObject(content.toString());
      }
    } catch (SQLException e) {
      GenericException exception = new DatabaseQueryException("Unable to query content of report " + reportId + ".", e);
      log(null, null, exception);
      return getExceptionTransferObject(exception);
    } catch (IOException e) {
      GenericException exception = new DatabaseQueryException("Unable to query content of report " + reportId + ".", e);
      log(null, null, exception);
      return getExceptionTransferObject(exception);
    }
  }

  private IReportGenerationService getReportGenerationService() {
    final IReportGenerationService repGenService = (IReportGenerationService) LookupManager.getInstance()
        .getLookupItem(LookupManager.SYSTEM, "ReportGenerationService");
    return repGenService;
  }

  private IDocumentService getDocumentService() {
    final IDocumentService docService = (IDocumentService) LookupManager.getInstance().getLookupItem(
        LookupManager.SYSTEM, "DocumentService");
    return docService;
  }

  private SchedulingServiceIntf getSchedulingService() {
    final SchedulingServiceIntf scheduler = (SchedulingServiceIntf) LookupManager.getInstance()
        .getLookup(LookupManager.BUSINESS).getService("SchedulingService");
    if (scheduler == null) {
      throw new RuntimeException("SchedulingService not configured or available for report management");
    }
    return scheduler;
  }

  private void log(String batchName, String reportName, GenericException e) {
    log.error("Error occured in report management service.");
    if (batchName != null)
      log.error("Batch Name: " + batchName);
    if (reportName != null)
      log.error("Report Name/Id: " + reportName);
    log.error("Error Code: " + e.getErrorCode());
    log.error("Error Message: " + e.getMessage());
    Throwable causer = e.getCause();
    while (causer != null) {
      logCause(e);
      causer = causer.getCause();
    }
  }

  private void logCause(GenericException e) {
    log.error("Caused by: " + e.getClass());
    log.error("Error Code: " + e.getErrorCode());
    log.error("Message: " + e.getMessage());
    log.error("Stacktrace: ", e);
  }

  private int getMaximumTransferSize() {
    if (maximumTransferSize == null)
      maximumTransferSize = new Integer(ServiceEngine.getProp(MAXIMUM_TRANSFER_SIZE, defaultTransferSize));
    return maximumTransferSize.intValue();
  }

  private TransferObject retrieveReport(Object id) throws IOException {
    Map storageMap = (Map) reportStorage.get(id);
    FileInputStream fis = (FileInputStream) storageMap.get(INPUT_STREAM);
    byte[] ba = new byte[fis.available() < getMaximumTransferSize() ? fis.available() : getMaximumTransferSize()];
    int read = fis.read(ba);
    Map m = new HashMap(3);
    m.put(ReportManagementServiceIntf.CONTENT, new String(ba));
    m.put(ReportManagementServiceIntf.END_OF_FILE, read <= 0 ? Boolean.TRUE : Boolean.FALSE);
    m.put(ReportManagementServiceIntf.ID, id);
    if (read <= 0) {
      fis.close();
      reportStorage.remove(id);
    } else {
      storageMap.put(TIMESTAMP, new Long(System.currentTimeMillis()));
    }
    return new TransferObject(m);
  }

  private void storeReport(Object id, FileInputStream fis) {
    Map storageMap = new HashMap(2);
    storageMap.put(INPUT_STREAM, fis);
    storageMap.put(TIMESTAMP, new Long(System.currentTimeMillis()));
    reportStorage.put(id, storageMap);
  }

  @Override
  public TransferObject getReportContent(Long reportId, Map arguments) {
    Map design = null;
    try {
      if (reportStorage.containsKey(arguments.get(ReportManagementServiceIntf.ID))) {
        return retrieveReport(arguments.get(ReportManagementServiceIntf.ID));
      }
      design = getReportDesignById(reportId);
      if (design == null) {
        log.error("Unable to find report design for report with ID " + reportId
            + ", can't generate report without design.");
        return getErrorTransferObject("No valid report design template is available for the requested report with ID "
            + reportId);
      }
      String content = getReportDesignContentByReportId((Long) design.get(IReportDesign.ID));
      if (content == null) {
        log.error(design.get(IReportDesign.NAME) + " does not have design, can't generate.");
        return getErrorTransferObject("No valid design template available for report " + reportId);
      }
      String path = generateReportInternal(reportId, content, arguments, XML_FORMAT, null);
      FileInputStream fis = new FileInputStream(new File(path));
      byte[] ba = new byte[fis.available() < getMaximumTransferSize() ? fis.available() : getMaximumTransferSize()];
      int read = fis.read(ba);
      Double id = new Double(Math.random());
      Map m = new HashMap(3);
      m.put(ReportManagementServiceIntf.CONTENT, new String(ba));
      m.put(ReportManagementServiceIntf.END_OF_FILE, read <= 0 ? Boolean.TRUE : Boolean.FALSE);
      m.put(ReportManagementServiceIntf.ID, id);
      if (read > 0)
        storeReport(id, fis);
      return new TransferObject(m);
    } catch (SQLException e) {
      GenericException ex = new DatabaseQueryException(e);
      log(null, reportId.toString(), ex);
      return getExceptionTransferObject(ex);
    } catch (ReportCompilationException e) {
      log(null, reportId.toString(), e);
      return getExceptionTransferObject(e);
    } catch (IOException e) {
      GenericException ex = new GenericException("IOException while trying to generate report " + reportId, e);
      log(null, reportId.toString(), ex);
      return getExceptionTransferObject(ex);
    } catch (ReportGenerationException e) {
      log(null, reportId.toString(), e);
      return getExceptionTransferObject(e);
    } catch (TransferObjectException e) {
      GenericException ex = new GenericException(e);
      log(null, reportId.toString(), ex);
      return getExceptionTransferObject(ex);
    }
  }

  @Override
  public TransferObject markForDeleteReportDesign(Long reportId) {
    final TransferObject[] tos = new TransferObject[1];
    final Map m = new HashMap();
    m.put(IReportDesign.ID, reportId);
    final IReadWriteDataProvider provider = getReadWriteProvider();
    try {
      provider.execute(new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          int updated = provider.update("markForDeleteReportDesign", m, t);
          tos[0] = new TransferObject(new Integer(updated));
        }
      });
    } catch (AbortTransactionException e) {
      log(null, "" + reportId, e);
      return getExceptionTransferObject(e);
    } catch (SQLException e) {
      GenericException ex = new DatabaseQueryException(e);
      log(null, "" + reportId, ex);
      return getExceptionTransferObject(ex);
    }
    return tos[0];
  }

  @Override
  public TransferObject markForDeleteReport(Long reportId) {
    final TransferObject[] tos = new TransferObject[1];
    final Map m = new HashMap();
    m.put(IReportDesign.ID, reportId);
    final IReadWriteDataProvider provider = getReadWriteProvider();
    try {
      provider.execute(new IRunnableTransaction() {
        public void execute(ITransaction t) throws SQLException, AbortTransactionException {
          int updated = provider.update("markForDeleteReport", m, t);
          tos[0] = new TransferObject(new Integer(updated));
        }
      });
    } catch (AbortTransactionException e) {
      log(null, "" + reportId, e);
      return getExceptionTransferObject(e);
    } catch (SQLException e) {
      GenericException ex = new DatabaseQueryException(e);
      log(null, "" + reportId, ex);
      return getExceptionTransferObject(ex);
    }
    return tos[0];
  }
}
