package com.profitera.services.system.report;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.services.business.ProviderDrivenService.TransferObjectException;

public interface IReportGenerationService {

  /**
   * This method returns a URL that can be used to "find" the generated report.
   * Since the reports can be quite large this allows the generated report to
   * be handed back via a local file or some other streaming protocol (like http
   * or ftp) as opposed to in-memory or RMI.
   * @param reportId
   * @param designContent
   * @param arguments
   * @param format 
   * @return
   * @throws IOException
   * @throws SQLException
   * @throws ReportCompilationException
   * @throws ReportGenerationException
   * @throws TransferObjectException 
   */
  public abstract String generateReport(Long reportId, String designContent,
      Map arguments, String format, IDataSourceConfiguration conf) throws IOException, SQLException,
      ReportCompilationException, ReportGenerationException, TransferObjectException;
  
  public abstract String generateReport(Long reportId, String designContent,
      Map arguments, File destinationFile, String format, IDataSourceConfiguration conf) throws IOException, SQLException,
      ReportCompilationException, ReportGenerationException, TransferObjectException;
  
  /**
   * Generates a report the same as generateReport(Long, String, Map)
   * but the report is not a "Report" in the classic sense, but a
   * document generated from a report design.
   * @see #generateReport(Long, String, Map)
   * @param designContent
   * @param arguments
   * @return as file path to the result output file
   * @throws IOException
   * @throws SQLException
   * @throws ReportCompilationException
   * @throws ReportGenerationException
   * @throws TransferObjectException
   */
  public String generateReport(String designContent, Map arguments, IDataSourceConfiguration conf)
    throws IOException, SQLException, ReportCompilationException, ReportGenerationException, TransferObjectException;

  /**
   * The toString of the object being used to identify the subreport needs to be
   * able to be interpreted proeprly to refer to the identifier for the report
   * (i.e. basicly should be a String instance)
   * 
   * @param content
   * @param subreportAssignments
   * @return
   * @throws TransferObjectException
   */
  public abstract String replaceDesignSubreportLocations(String content,
      Map subreportAssignments) throws TransferObjectException;

  public abstract String[] getDesignSubreportLocations(String content)
      throws TransferObjectException;

  /**
   * This method returns a URL that can be used to "find" the generated template.
   * @param templateId
   * @param content
   * @param arguments
   * @return
   * @throws IOException
   * @throws SQLException
   * @throws ReportCompilationException
   * @throws ReportGenerationException
   * @throws TransferObjectException 
   */
  public String generateTemplate(Long templateId, String content, Map arguments, IDataSourceConfiguration conf)
  	throws IOException, SQLException,
    ReportCompilationException, ReportGenerationException, TransferObjectException;
  
  public String generateEmptyDataReport(Long reportId, String designContent, Map arguments, IDataSourceConfiguration iDataSourceConfiguration)
  	throws ReportCompilationException, TransferObjectException, SQLException, IOException, ReportGenerationException;
  
  public String compileReport(Object reportId, String designContent) throws IOException, ReportCompilationException;
  public File generateReportContent(String id, Object jasperReport, Map arguments, String exportFormat,
      File dest, IDataSourceConfiguration conf) throws IOException, ReportGenerationException;

}