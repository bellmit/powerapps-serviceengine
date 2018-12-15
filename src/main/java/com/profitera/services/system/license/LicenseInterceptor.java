package com.profitera.services.system.license;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.profitera.server.LicenseException;
import com.profitera.server.ServiceEngine;
import com.profitera.util.interceptor.Interceptor;

public class LicenseInterceptor implements Interceptor {
  private static final String MODULE = "licensemanager";
  private static final String LOCATION = MODULE + "." + "location";
  private static final int DEFAULT_ALERT_FREQUENCY = 60 * 45;
  //
  private Timer timer = new Timer();
  private LicenseVerifier verifier;

  public LicenseInterceptor() {
    long alertFrequency = ServiceEngine.getIntProp(MODULE + ".frequency", DEFAULT_ALERT_FREQUENCY);
    if (alertFrequency > DEFAULT_ALERT_FREQUENCY) {
      alertFrequency = DEFAULT_ALERT_FREQUENCY;
    }
    verifier = buildVerifier();
    TimerTask tt = new TimerTask() {
      public void run() {
        verifier.verify();
      };
    };
    long frequency = alertFrequency * 1000;
    timer.schedule(tt, frequency, frequency);
  }

  public static LicenseVerifier buildVerifier() {
    String location = ServiceEngine.getConfig(false).getProperty(LOCATION);
    try {
      return new LicenseVerifier(location);
    } catch (IOException e) {
      throw new RuntimeException("Unable to read license files specified by license path server property '" + LOCATION + "'", e);
    }
  }

  @Override
  public void beforeInvoke(Object target, Method m, Object[] args, Map<String, Object> context) {
    if (verifier.isViolated()) {
      String code = verifier.getViolationCode();
      Object[] data = verifier.getViolationData();
      throw new LicenseException(code, data, "Your software license has been violated");
    }
  }
  @Override
  public void afterInvoke(Object target, Method m, Object[] args, Map<String, Object> context, Object result) {
  }

}
