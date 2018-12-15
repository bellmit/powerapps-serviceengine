package com.profitera.services.business.login.impl;

import java.util.HashMap;
import java.util.Map;

import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.license.LicenseInterceptor;
import com.profitera.services.system.license.LicenseVerifier;

public class LicenseWarningTracker {
  private static final long GRACE_PERIOD = 1000 * 60 * 5;
  private Map warnedUsers = new HashMap();
  private long lastVerified = 0;
  private LicenseVerifier verifier;
  private final long verifyPeriod;
  
  public LicenseWarningTracker(long verifyPeriod){
    this.verifyPeriod = verifyPeriod;
    
  }

  public TransferObject getLicenseWarning(String userName) {
    TransferObject licenseWarning = getLicenseWarning();
    if (licenseWarning != null && !isUserLicenseWarned(userName)){
      setUserLicenseWarned(userName);
      return licenseWarning;
    }
    return null;
  }
  
  
  private void setUserLicenseWarned(String userName) {
    warnedUsers.put(userName, new Long(System.currentTimeMillis()));
  }
  
  private boolean isUserLicenseWarned(String userName) {
    Long warnTime = (Long) warnedUsers.get(userName);
    // Warned within the last 5 minutes is considered already warned
    long warnThreshold = System.currentTimeMillis() - GRACE_PERIOD;
    if (warnTime == null || warnTime.longValue() < warnThreshold){
      return false;
    }
    return true;
  }
  private TransferObject getLicenseWarning() {
    LicenseVerifier verifier = getVerifier();
    if (verifier.isWarning()){
    return new TransferObject(verifier.getWarningData(), TransferObject.ERROR, verifier.getWarningCode());
    } else {
      return null;
    }
  }


  private synchronized LicenseVerifier getVerifier() {
    if (verifier == null || lastVerified + verifyPeriod < System.currentTimeMillis()){
      lastVerified = System.currentTimeMillis();
      verifier = LicenseInterceptor.buildVerifier();
      verifier.verify();  
    }
    return verifier;
  }

}
