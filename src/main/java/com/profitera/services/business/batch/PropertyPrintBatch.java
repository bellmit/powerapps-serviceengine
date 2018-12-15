package com.profitera.services.business.batch;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.descriptor.business.TransferObject;

public class PropertyPrintBatch implements IBatchProcess {
  Log log = LogFactory.getLog(PropertyPrintBatch.class);
  public TransferObject invoke(String identifier, Date effectiveDate, Map arguments) {
    log.info(identifier + ": " + effectiveDate);
    for (Iterator i = arguments.entrySet().iterator(); i.hasNext();) {
      Map.Entry element = (Map.Entry) i.next();
      log.info(identifier + ": " + element.getKey() + " -> " + element.getValue());  
    }
    return new TransferObject();
  }

}
