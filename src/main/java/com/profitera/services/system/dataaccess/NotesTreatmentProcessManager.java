package com.profitera.services.system.dataaccess;

import java.util.Map;

import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.descriptor.business.reference.TreatmentProcessTypeStatusRefBusinessBean;

public class NotesTreatmentProcessManager extends
    DefaultTreatmentProcessManager {
  private static final Long SUCCESSFUL = new Long(TreatmentProcessTypeStatusRefBusinessBean.SUCCESSFUL_TREATMENT_PROCESS_STATUS.longValue());
  protected Long getProcessStatus(Map process, IReadWriteDataProvider p) {
    Long status = (Long) process.get(ITreatmentProcess.PROCESS_STATUS_ID);
    if (status == null || !status.equals(SUCCESSFUL)){
      getLog().warn("Processes handled by " + getClass().getName() + " are always successful, changing status");
      status = SUCCESSFUL;
    }
    return status;
  }

}
