package com.profitera.services.business.batch.promiseevaluation.impl;

import java.util.Date;
import java.util.Map;

public interface IPromiseGraceCalculator {
  public Date getGraceDate(Map promiseData);
}
