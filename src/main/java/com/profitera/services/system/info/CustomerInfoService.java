package com.profitera.services.system.info;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.descriptor.business.meta.ICustomer;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class CustomerInfoService extends AbstractInfoService {

  private static final String GET_CUSTOMER_INFORMATION = "getCustomerInformation";
  private static final String INSERT_CUSTOMER_INFORMATION = "insertCustomerInformation";
  private static final String UPDATE_CUSTOMER_INFORMATION = "updateCustomerInformation";
  private static final String INSERT_CUSTOMER_INFORMATION_LOG = "insertCustomerInformationHistory";

  protected String getInformationHistoryStatementName() {
    return INSERT_CUSTOMER_INFORMATION_LOG;
  }

  protected String getInformationInsertStatementName() {
    return INSERT_CUSTOMER_INFORMATION;
  }

  protected String getInformationSelectionQueryName() {
    return GET_CUSTOMER_INFORMATION;
  }

  protected String getInformationKeyName() {
    return ICustomer.CUSTOMER_ID;
  }

  protected String getInformationUpdateStatementName() {
    return UPDATE_CUSTOMER_INFORMATION;
  }

  public IRunnableTransaction updateCustomerInformation(Map info, String customerId, String user, Date d, IReadWriteDataProvider p) throws SQLException {
    return super.updateInformation(info, customerId, user, d, p);
  }

}
