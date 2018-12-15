package com.profitera.services.business.http.impl;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.profitera.event.license.ILicenseProvider;
import com.profitera.services.system.license.LicenseInterceptor;
import com.profitera.services.system.license.LicenseVerifier;
import com.profitera.util.security.BouncyCastleLicenseGenerator;
import com.profitera.util.security.BouncyCastleLicenseGenerator.NamedUserType;

final class DefaultLicenseProvider implements ILicenseProvider {
  @Override
  public Map<String, Object> getLicenseInformation() throws ParseException {
    LicenseVerifier verifier = LicenseInterceptor.buildVerifier();
    Map<String, Object> info = new HashMap<String, Object>();
    addInfo("NAMED_USER_LIMIT", verifier.getLicenseInternalNamedUsers(), info);
    NamedUserType[] values = BouncyCastleLicenseGenerator.NamedUserType.values();
    for (int i = 0; i < values.length; i++) {
      addInfo("NAMED_USER_LIMIT_" + values[i].getTypeId(), verifier.getLicenseNamedUsers(values[i]), info);  
    }
    addInfo("CONCURRENT_USER_LIMIT", verifier.getLicenseConcurrentUsers(), info);
    addInfo("ACCOUNT_LIMIT", verifier.getLicenseAccountCount(), info);
    addInfo("LOGICAL_PROCESSOR_LIMIT", verifier.getLicenseLogicalProcessorCount(), info);
    addInfo("HIGH_AVAILABILITY", verifier.isHighAvailabilityLicense(), info);
    addInfo("EXPIRATION_DATE", verifier.getExpirationDate(), info);
    addInfo("EXPIRATION_WARNING_DAY_COUNT", verifier.getLicenseExpirationWarningDayCount(), info);
    return info;
  }

  private void addInfo(String key, Object restriction,
      Map<String, Object> info) {
    if (restriction != null) {
      info.put(key, restriction);
    }
  }
}