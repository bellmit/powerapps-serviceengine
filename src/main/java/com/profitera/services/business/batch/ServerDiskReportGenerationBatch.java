package com.profitera.services.business.batch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.profitera.deployment.rmi.ReportManagementServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.report.ReportManagementService;
import com.profitera.services.system.lookup.LookupManager;

public class ServerDiskReportGenerationBatch extends AbstractBatchProcess {
  private static final String PATH = "path";
  private static final String QUERY = "query";
  private static final String FILENAMECOLUMN = "filenamecolumn";
  private static final String REPORTDESIGNIDCOLUMN = "reportdesignidcolumn";
  

  {
    addRequiredProperty(PATH, String.class, 
        "The path of the directory to which to store the reports generated", 
        "The path specified is used as the directory into which the resulting reports will be saved. " +
        "If the directory does not exsit the batch will attempt to create it and fail if that fails " +
        "or the directory exists but is now writable.");
    addRequiredProperty(QUERY, String.class, 
        "The query to execute to generate the arguments to pass to the reports to generate, one report is " +
        "generated for each row returned by this query", 
        "This query is executed using the ListQueryService and receives the batch EFFECTIVE_DATE and ID as arguments. " +
        "Each row of the the results is used in the generation of individual reports.");
    addProperty(REPORTDESIGNIDCOLUMN, String.class, "REPORT_DESIGN_ID", 
        "The column that specifies the report to execute", 
        "The result returned is used as the report design ID to execute in generating the report.");
    addProperty(FILENAMECOLUMN, String.class, "FILE_NAME", 
        "The column that specifies the file name to save the report to", 
        "The result returned is used as the name of the file to which the report is to be saved.");
    addRequiredProperty("format", String.class, "The format of the report to be saved as", 
        "This property value specifies the format to store the report in, the currently supported value is 'PDF' and 'XLS'.");
  }

  @Override
  protected String getBatchDocumentation() {
    return "For each row returned by " + QUERY + " a report is generated. The report is passed" +
    		" the row as arguments in addition to an " + EFFECTIVE_DATE_PARAM_NAME + " argument as the " +
    		" effective date of the batch execution. Each report is generated in the directory " + PATH + " " +
    		" with the file name specified by " + FILENAMECOLUMN + ", if a file of that name already exisists the " +
    				"report will not be generated and the batch will stop with an error. The report to be generated is " +
    		" specified by ID as specified by " + REPORTDESIGNIDCOLUMN + ".";
  }

  @Override
  protected String getBatchSummary() {
    return "Generates reports in the specified format to the specified location on the server";
  }

  @Override
  protected TransferObject invoke() {
    String format = (String) getPropertyValue("format");
    if (!format.equals(ReportManagementServiceIntf.PDF_FORMAT) && !format.equals(ReportManagementServiceIntf.XLS_FORMAT)) {
      getLog().error("Format specified by " + "format" + " is '" + format + "', valid values are 'PDF' or 'XLS'");
      return new TransferObject(TransferObject.ERROR, "INVALID_REPORT_FORMAT");
    }
    String path = (String) getPropertyValue(PATH);
    String fileNameColumn = (String) getPropertyValue(FILENAMECOLUMN);
    File dir = new File(path);
    if (!dir.exists()) {
      if (!dir.mkdirs()){
        getLog().error("Failed to create path desginated by '" + path + "'(" + dir.getAbsolutePath() + ")");
        return new TransferObject(TransferObject.ERROR, "INVALID_REPORT_PATH");
      }
    }
    if (!dir.isDirectory()) {
      getLog().error("Path desginated by '" + path + "'(" + dir.getAbsolutePath() + ") is not a directory");
      return new TransferObject(TransferObject.ERROR, "INVALID_REPORT_PATH");
    }
    String reportDesignIdColumn = (String) getPropertyValue(REPORTDESIGNIDCOLUMN);
    String query = (String) getPropertyValue(QUERY);
    Map args = new HashMap();
    args.put("ID", getIdentifier());
    args.put(EFFECTIVE_DATE_PARAM_NAME, getEffectiveDate());
    TransferObject lqsResult = getListQueryService().getQueryList(query, args);
    if (lqsResult.isFailed()) {
      return lqsResult;
    }
    List reports = (List) lqsResult.getBeanHolder();
    
    for (int i = 0;i<reports.size(); i++) {
      Map row = (Map) reports.get(i);
      String fileName = (String) row.get(fileNameColumn);
      if(fileName!=null && !fileName.toUpperCase().endsWith(format)){
      	fileName = fileName.concat("."+format);
      }
      File target = new File(dir, fileName);
      try {
				if (!target.createNewFile()) {
					getLog().error("Failed to create file at '" + target.getAbsolutePath() + "'");
					return new TransferObject(TransferObject.ERROR, "FILE_CREATION_FAILED");
				}
			} catch (IOException e) {
				getLog().error("Failed to create file at '" + target.getAbsolutePath() + "'", e);
				return new TransferObject(TransferObject.EXCEPTION, "FILE_CREATION_FAILED");
			}
      row.put(EFFECTIVE_DATE_PARAM_NAME, getEffectiveDate());
      Long reportDesign = (Long) row.get(reportDesignIdColumn);
      ReportManagementService service = (ReportManagementService) getReportManagementService();
      TransferObject to = service.generateReport(reportDesign, row, target, format);
      if (to.isFailed()) {
        return to;
      }
    }
    return new TransferObject();
  }
  
  private ReportManagementServiceIntf getReportManagementService() {
    LookupManager lm = LookupManager.getInstance();
    final ReportManagementServiceIntf service = (ReportManagementServiceIntf) 
    lm.getLookupItem(LookupManager.BUSINESS, "ReportManagementService");
    return service;
  } 

}
