package com.profitera.services.system.license;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.crypto.InvalidCipherTextException;

import com.profitera.dataaccess.ISqlMapProvider;
import com.profitera.dataaccess.SqlMapProvider;
import com.profitera.datasource.IDataSourceConfiguration;
import com.profitera.datasource.IDataSourceConfigurationSet;
import com.profitera.descriptor.business.admin.UserBusinessBean;
import com.profitera.ibatis.SQLMapFileRenderer;
import com.profitera.server.LicenseException;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.login.FailoverLoginService;
import com.profitera.services.business.login.LoginService;
import com.profitera.services.system.dataaccess.ProtocolLoadedSqlMapProvider;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.security.BouncyCastleLicenseGenerator;
import com.profitera.util.security.BouncyCastleLicenseGenerator.NamedUserType;

public class LicenseVerifier {
  private static final String COUNT = "COUNT";
  private static final String IS_CURRENT = "IS_CURRENT";
  private static final String GET_CONTRACT_AGENCIES = "getContractAgenciesByContractIsCurrent";
  private static final String NAMED_USER_COUNT_STATEMENT = "getNoOfNamedUsers";
  private Log log = LogFactory.getLog(LicenseVerifier.class);
  private Map<String, String> licenseValues = new HashMap<String, String>();
  private String mLicenseExpiredMsg = null;
  private boolean mLicenseExpired = false;
  private String mViolationCode = null;
  private Object[] mViolationData = new Object[0];
  private boolean mLicenseWarning = false;
  private String mWarningCode = null;
  private Object[] mWarningData = new Object[0];
  private SqlMapProvider mPrivateProvider = null;
  

  public LicenseVerifier(String location) throws IOException {
    BouncyCastleLicenseGenerator lg = new BouncyCastleLicenseGenerator();
    Throwable t = null;
    try {
      licenseValues = lg.degenerate(location);
      printLicenseInfo(licenseValues);
      return;
    } catch (InvalidKeyException e1) {
      t = e1;
    } catch (NoSuchAlgorithmException e1) {
      t = e1;
    } catch (NoSuchPaddingException e1) {
      t = e1;
    } catch (IllegalBlockSizeException e1) {
      t = e1;
    } catch (BadPaddingException e1) {
      t = e1;
    } catch (InvalidCipherTextException e1){
      t = e1;
    }
    log.fatal("Unable to read license key", t);
    setViolated(LicenseException.INVALID_LICENSE_KEY, 
        new Object[]{location}, getUnreadableMessage(location));
  }
  
  private void setViolated(String code, Object[] data, String message){
    mLicenseExpired = true;
    mViolationCode = code;
    mViolationData = data == null ? new Object[0] : data;
    mLicenseExpiredMsg = new MessageFormat(message).format(data);
  }
  
  private void setNotViolated(){
    mLicenseExpired = false;
    mViolationCode = null;
    mViolationData = new Object[0];
    mLicenseExpiredMsg = null;
  }

  private String getUnreadableMessage(String location) {
    return "Unable to read license key at " + location;
  }

  private SqlMapProvider getPrivateProvider() {
    if (mPrivateProvider == null) {
      SQLMapFileRenderer renderer = new SQLMapFileRenderer();
      String rName = "licenseManagement";
      StringBuilder sb = new StringBuilder(renderer.renderHeader(rName));
      sb.append(getNoOfAccounts(renderer) + getNoOfNamedUsers(renderer));
      sb.append(getNoOfConcurrentUsers(renderer) + getIsActiveUser(renderer));
      sb.append(getContractAgencies(renderer) + renderer.renderFooter());
      IDataSourceConfigurationSet configSet = ServiceEngine.getDataSourceConfigurations();
      IDataSourceConfiguration source = configSet.getDefaultDataSource();
      mPrivateProvider = new ProtocolLoadedSqlMapProvider(rName, sb.toString(), source, "License");
    }
    return mPrivateProvider;
  }

  private String getNoOfAccounts(SQLMapFileRenderer r) {
    return r.renderSelect("getNoOfAccounts", Long.class,
        "select count(*) from ptraccount");
  }

  private String getNoOfNamedUsers(SQLMapFileRenderer r) {
    return r.renderSelect(NAMED_USER_COUNT_STATEMENT, Long.class, Long.class,
        "select count(*) from ptruser where active_status = '"
            + UserBusinessBean.ACTIVE_STATUS + "' and USER_TYPE_ID = #VALUE#");
  }

  private String getNoOfConcurrentUsers(SQLMapFileRenderer r) {
    return r.renderSelect("getNoOfConcurrentUsers", Long.class,
        "select count(*) from ptruser where logon_status = '"
            + UserBusinessBean.LOGIN_ACTIVE + "'");
  }
  
  private String getIsActiveUser(SQLMapFileRenderer r) {
    return r.renderSelect("isActiveUser", Integer.class, String.class,
        "select count(*) from ptruser where active_status = '"
            + UserBusinessBean.ACTIVE_STATUS + "' and USER_ID = #value#");
  }
  private String getContractAgencies(SQLMapFileRenderer r) {
    StringBuilder b = new StringBuilder(r.renderResultMap(GET_CONTRACT_AGENCIES + "-rmap", HashMap.class, 
        new String[]{IS_CURRENT, COUNT}, new Class[]{Boolean.class, Long.class}));
    b.append(r.renderSelect(GET_CONTRACT_AGENCIES, GET_CONTRACT_AGENCIES + "-rmap",
        "select is_current, count(distinct AGENCY_ID) from PTRAGENCY_CONTRACT_VERSION group by is_current"));
    return b.toString();
  }
  private void printLicenseInfo(Map<String, String> licenseValues) {
    Object exp = licenseValues.get(BouncyCastleLicenseGenerator.EXPIRY_DATE);
    Object cUser = licenseValues.get(BouncyCastleLicenseGenerator.CONCURRENT_USER);
    Object accounts = licenseValues.get(BouncyCastleLicenseGenerator.NO_OF_ACCOUNTS);
    Object processors = licenseValues.get(BouncyCastleLicenseGenerator.PROCESSOR_COUNT);
    Object highAvailability = licenseValues.get(BouncyCastleLicenseGenerator.HIGH_AVAILABILITY);
    Object contracts = licenseValues.get(BouncyCastleLicenseGenerator.AGENCY_COUNT);
    log("License Expiry date: ", exp);
    log("License Concurrent users: ", cUser);
    NamedUserType[] values = BouncyCastleLicenseGenerator.NamedUserType.values();
    for (int i = 0; i < values.length; i++) {
      Object nUser = licenseValues.get(values[i].getTag());
      log("License Named users(Type " + values[i].getTypeId() + "): ", nUser);
    }
    log("License Accounts: ", accounts);
    log("License Logical Processors: ", processors);
    log("License High Availability: ", highAvailability);
    log("License Active Contracted Agencies: ", contracts);
  }
  
  private void log(String text, Object data){
    if (data == null) {
      return;
    }
    log.info(text + data);
  }

  public void verify() {
    try {
      validateLicense(new Date());
      if (mLicenseExpired) {
        printLicenseInfo(licenseValues);
        log.fatal("License violation detected: " + mLicenseExpiredMsg);
        log
            .fatal("Application will send errors for all attempted client access");
      }
    } catch (SQLException e) {
      log.fatal("Unable to query license information from database, "
              + "system will assume that license has been violated and " +
              		"disable user access.", e);
      setViolated(LicenseException.ERROR_VERIFYING_LICENSE, null, 
          "Unable to retrieve license information from database, system will disable user access.");
    } catch (ParseException e) {
      log.fatal("Unable to parse date information in license file, " +
      		"please obtain new license file.  User access to application is disabled.",
              e);
      setViolated(LicenseException.ERROR_VERIFYING_LICENSE, null, 
          "License file corrupt, user access disabled.");
    }
  }

  private void validateLicense(Date currentDate) 
    throws ParseException, SQLException {
    mLicenseExpiredMsg = "";
    setNotViolated();
    clearWarning();
    if (getExpirationDate() != null) {
      Date fExpiryDate = getExpirationDate();
      if (fExpiryDate.before(currentDate)) {
        setViolated(LicenseException.LICENSE_DATE_EXPIRED, new Object[]{currentDate, fExpiryDate}, 
            LicenseException.EXPIRED_MESSAGE);
      } else {
        Integer count = getLicenseExpirationWarningDayCount();
        if (count != null) {
          long warningDate = fExpiryDate.getTime() - (count * 1000*60*60*24);
          if (warningDate <= currentDate.getTime()) {
            setWarning(LicenseException.LICENSE_DATE_EXPIRED_WARNING, 
                new Object[]{currentDate, fExpiryDate});
          }
        }
      }
    }
    if (getLicenseLogicalProcessorCount() != null) {
      int count = getLicenseLogicalProcessorCount().intValue();
      int actualProcessors = Runtime.getRuntime().availableProcessors();
      if (actualProcessors > count) {
        setViolated(LicenseException.LICENSE_PROCESSOR_COUNT_EXCEEDED, 
            new Object[]{new Integer(actualProcessors), new Integer(count)}, 
            LicenseException.PROCESSORS_MESSAGE);
      }
    }
    if (getLicenseAccountCount() != null) {
      long fNoOfAccounts = getLicenseAccountCount();
      Long noOfAccounts = (Long) getPrivateProvider().queryObject("getNoOfAccounts", null);
      if (noOfAccounts.longValue() > fNoOfAccounts) {
        setViolated(LicenseException.LICENSE_ACCOUNT_COUNT_EXCEEDED, 
            new Object[]{noOfAccounts, fNoOfAccounts}, 
            LicenseException.ACCOUNTS_MESSAGE);
      }
    }
    if (getLicenseConcurrentUsers() != null) {
      Long noOfConcurrentUsers = (Long) getPrivateProvider().
        queryObject("getNoOfConcurrentUsers", null);
      long fNoOfConcurrentUser = getLicenseConcurrentUsers();
      if (noOfConcurrentUsers.longValue() > fNoOfConcurrentUser) {
        setViolated(LicenseException.LICENSE_CONCURRENT_USER_COUNT_EXCEEDED, 
            new Object[]{noOfConcurrentUsers, fNoOfConcurrentUser}, 
            LicenseException.CONCURRENT_MESSAGE);
      }
    }
    if (getLicenseAgenciesWithContracts() != null) {
      Long countFromDb = (Long) getAgenciesWithActiveContractsFromDatabase();
      long licenseCount = getLicenseAgenciesWithContracts();
      if (countFromDb.longValue() > licenseCount) {
        setViolated(LicenseException.LICENSE_ACTIVE_CONTRACT_AGENCY_EXCEEDED, 
            new Object[]{countFromDb, licenseCount}, 
            LicenseException.AGENCY_MESSAGE);
      }
    }
    NamedUserType[] values = NamedUserType.values();
    for (int i = 0; i < values.length; i++) {
      Long fNamedUser = getLicenseNamedUsers(values[i]);
      if (fNamedUser != null) {
        Long actualNoOfNamedUsers = getNamedUserCountFromDatabase(values[i]);
        if (actualNoOfNamedUsers.longValue() > fNamedUser.longValue()) {
          setViolated(LicenseException.LICENSE_NAMED_USER_COUNT_EXCEEDED, 
              new Object[]{actualNoOfNamedUsers, fNamedUser}, 
              LicenseException.NAMED_MESSAGE);
        }
      }
    }
    boolean isHa = isHighAvailabilityLicense();
    if (!isHa){
      LoginService l = (LoginService) LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, "LoginService");
      if (l instanceof FailoverLoginService) {
        setViolated(LicenseException.LICENSE_FAIL_OVER_VIOLATED, null, 
        "High-availability sessions enabled but not permitted by license");
      }
    }
    String[] keys = new String[] { BouncyCastleLicenseGenerator.EXPIRY_DATE,
        BouncyCastleLicenseGenerator.CONCURRENT_USER, BouncyCastleLicenseGenerator.NO_OF_ACCOUNTS, BouncyCastleLicenseGenerator.AGENCY_COUNT};
    boolean hasOne = false;
    for (int i = 0; i < keys.length; i++) {
      if (licenseValues.containsKey(keys[i]) && licenseValues.get(keys[i]) != null) {
        hasOne = true;
      }
    }
    for (int i = 0; i < NamedUserType.values().length; i++) {
      String key = NamedUserType.values()[i].getTag();
      if (licenseValues.containsKey(key) && licenseValues.get(key) != null) {
        hasOne = true;
      }
    }
    if (hasOne == false) {
      setViolated(LicenseException.LICENSE_HAS_NO_RESTRICTION, null, 
      "No license conditions present, license is not valid.");
    }
  }

  public boolean isHighAvailabilityLicense() {
    String ha = (String) licenseValues.get(BouncyCastleLicenseGenerator.HIGH_AVAILABILITY);
    if (ha == null || ha.equals("No")) {
      return false;
    }
    return true;
  }

  public Long getLicenseLogicalProcessorCount() {
    return getCountIfPresent(BouncyCastleLicenseGenerator.PROCESSOR_COUNT);
  }

  public Long getLicenseAccountCount() {
    return getCountIfPresent(BouncyCastleLicenseGenerator.NO_OF_ACCOUNTS);
  }
  public Integer getLicenseExpirationWarningDayCount() {
    Object warningString = licenseValues.get(BouncyCastleLicenseGenerator.EXPIRY_WARNING_DAY_COUNT);
    Integer count = warningString == null ? null : new Integer(warningString.toString());
    return count;
  }

  public Date getExpirationDate() throws ParseException {
    String fExpiryDateStr = (String) licenseValues
        .get(BouncyCastleLicenseGenerator.EXPIRY_DATE);
    if (fExpiryDateStr == null) {
      return null;
    }
    Date fExpiryDate = new SimpleDateFormat(BouncyCastleLicenseGenerator.LICENSE_DATE_FORMAT).parse(fExpiryDateStr);
    return fExpiryDate;
  }

  private void setWarning(String code, Object[] warningData) {
    mWarningCode = code;
    mWarningData = warningData;
    mLicenseWarning = true;
  }

  private void clearWarning() {
    mWarningCode = null;
    mWarningData = new Object[]{};
    mLicenseWarning = false;
  }

  public Long getLicenseInternalNamedUsers() {
    return getLicenseNamedUsers(NamedUserType.Internal1);
  }

  public Long getLicenseNamedUsers(NamedUserType userType) {
    String key = userType.getTag();
    String fNoOfAccountsStr = (String) licenseValues.get(key);
    if (fNoOfAccountsStr == null){
      return null;
    }
    return Long.parseLong(fNoOfAccountsStr);
  }

  public boolean isViolated() {
    return mLicenseExpired;
  }

  private Long getNamedUserCountFromDatabase(NamedUserType userType) throws SQLException {
    return (Long) getPrivateProvider().queryObject(NAMED_USER_COUNT_STATEMENT, userType.getTypeId());
  }
  private Long getAgenciesWithActiveContractsFromDatabase() throws SQLException {
    Iterator<?> i = getPrivateProvider().query(ISqlMapProvider.LIST, GET_CONTRACT_AGENCIES, null);
    @SuppressWarnings("unchecked")
    Iterator<Map<String, Object>> query = (Iterator<Map<String, Object>>) i;
    Long count = null;
    while (query.hasNext()) {
      Map<String, Object> m = query.next();
      Boolean b = (Boolean) m.get(IS_CURRENT);
      if (b.booleanValue()) {
        count = (Long) m.get(COUNT);
      }
    }
    return count;
  }

  
  public Long getCurrentNamedUserCount(NamedUserType userType) {
    try {
      Long noOfNamedUsers;
      noOfNamedUsers = getNamedUserCountFromDatabase(userType);
      return noOfNamedUsers;
    } catch (SQLException e) {
      log.error("Failed to number of active named users", e);
      return new Long(0);
    }
  }

  public boolean isActiveUser(String userId) {
    try {
      Integer i = (Integer) getPrivateProvider().queryObject("isActiveUser", userId);
      return !(i == null || i.intValue() == 0);
    } catch (SQLException e){
      log.error("Failed to verify active status of user", e);
      return false;
    }
  }

  public String getViolationCode() {
    return mViolationCode;
  }

  public Object[] getViolationData() {
    return mViolationData;
  }

  public Object[] getWarningData() {
    return mWarningData;
  }

  public String getWarningCode() {
    return mWarningCode;
  }

  public boolean isWarning() {
    return mLicenseWarning;
  }

  public Long getLicenseConcurrentUsers() {
   return getCountIfPresent(BouncyCastleLicenseGenerator.CONCURRENT_USER);
  }
  public Long getLicenseAgenciesWithContracts() {
    return getCountIfPresent(BouncyCastleLicenseGenerator.AGENCY_COUNT);
  }
  private Long getCountIfPresent(String prop) {
    String countStr = (String) licenseValues.get(prop);
    if (countStr == null) {
      return null;
    }
    return Long.parseLong(countStr);
  }

  public Long getCurrentInternalNamedUserCount() {
    try {
      return getNamedUserCountFromDatabase(NamedUserType.Internal1);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }
  

}
