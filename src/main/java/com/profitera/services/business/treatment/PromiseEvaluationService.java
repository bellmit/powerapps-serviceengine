/*
 * Created on Sep 15, 2003
 */
package com.profitera.services.business.treatment;

import java.util.Date;
import java.util.HashMap;

import com.profitera.deployment.rmi.PromiseEvaluationServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.batch.PromiseEvaluationBatch;

/**
 * This implementation should be considered "legacy" - the guts
 * are moved to PromiseEvaluationBatch and this entire service
 * and interface will be phased out eventually.
 * @author jamison
 */
public class PromiseEvaluationService extends ProviderDrivenService
    implements PromiseEvaluationServiceIntf {
  private static final String PROMISEEVALUATION_COMMITSIZE = "promiseevaluation." + "commitsize";
  public TransferObject evaluateAllPromises(Date evalDate) {
    HashMap arguments = new HashMap();
    int cSize = ServiceEngine.getIntProp(PROMISEEVALUATION_COMMITSIZE, -1);
    if (cSize != -1) {
      arguments.put("commitsize", cSize + "");
    }
    return new PromiseEvaluationBatch().invoke("PROMISE_EVAL", evalDate, arguments);
  }
}
