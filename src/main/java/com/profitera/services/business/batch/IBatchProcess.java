package com.profitera.services.business.batch;

import java.util.Date;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;

public interface IBatchProcess {
  public TransferObject invoke(String identifier, Date effectiveDate, Map arguments);
}
