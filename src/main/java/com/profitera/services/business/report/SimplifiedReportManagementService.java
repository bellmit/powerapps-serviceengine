package com.profitera.services.business.report;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.profitera.deployment.rmi.ReportManagementServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.http.IMessageHandler;
import com.profitera.services.business.login.ServerSession;
import com.profitera.services.system.lookup.LookupManager;

public class SimplifiedReportManagementService extends ProviderDrivenService implements ReportManagementServiceIntf {
  private ReportManagementService delegate;

  @Override
  public TransferObject getReportInstancesBetweenDates(Date startDate, Date endDate, Long groupId) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");
  }

  @Override
  public TransferObject getReportInstanceContent(Long reportId) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");
  }

  @Override
  public TransferObject getReportDesigns(Long groupId) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");
  }

  @Override
  public TransferObject getReportDesign(Long reportId) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");
  }

  @Override
  public TransferObject addNewReportDesignWithContent(TransferObject report) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");
  }

  @Override
  public TransferObject updateReportDesign(TransferObject reportDesign) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");

  }

  @Override
  public TransferObject scheduleReport(Long reportId, String schedule, String userId) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");

  }

  @Override
  public TransferObject unscheduleReport(Long reportId) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");
  }

  @Override
  public TransferObject getReportDesignSubreportNames(TransferObject designContent) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");

  }

  @Override
  public TransferObject generateReport(Long reportId, Map arguments) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");

  }

  @Override
  public TransferObject getReportContent(Long reportId, Map arguments) {
    Map<String, Object> m = new HashMap<String, Object>(arguments);
    m.put("REPORT_ID", reportId);
    HashMap context = new HashMap();
    context.put("session", ServerSession.THREAD_SESSION.get());
    IMessageHandler h = (IMessageHandler) LookupManager.getInstance().getLookupItem(LookupManager.SYSTEM, "MessageHandler");
    Object handleMessage = h.handleMessage(null, "EventService", "sendEvent", new Class[]{String.class, Map.class},
        new Object[]{"reportmanagement.generatecontent", m}, context);
    TransferObject to = (TransferObject) handleMessage;
    if (to.isFailed()) {
      return to;
    } else {
      return getDelegate().getReportContent(reportId, arguments);
    }
  }

  @Override
  public TransferObject markForDeleteReportDesign(Long reportId) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");

  }

  @Override
  public TransferObject markForDeleteReport(Long reportId) {
    return new TransferObject(TransferObject.ERROR, "UNSUPPORTED_OPERATION");
  }

  public ReportManagementService getDelegate() {
    if (delegate == null) {
      delegate = new ReportManagementService();
      delegate.setDataSourceConfigurations(getDataSourceConfigurations());
    }
    return delegate;
  }
}
