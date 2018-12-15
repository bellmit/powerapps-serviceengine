package com.profitera.services.business.login;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.profitera.descriptor.business.admin.AccessRightsBean;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public interface ISingleSignOnImplementation {
  interface ISingleSignOnSession {
    public String getUserName();
    public long getRole();
    public Map<Long, String> getAccessRights();
  }
  void setProperties(Properties p);
  ISingleSignOnSession getAuthenticatedUser(String ticket, IReadWriteDataProvider loginDataProvider) throws SingleSignOnCommunicationException;
  List<AccessRightsBean> getAccessRights(Object sessionAttachedData) throws SingleSignOnCommunicationException;
  void acknowledgeLogin(ISingleSignOnSession ssoSession);
  void logoff(String userName, Object sessionAttachedData) throws SingleSignOnCommunicationException;
  List<Map<String, Object>> getBulkUserInformation(Date from, Date to, String organization, String branch, String user,
      Object sessionAttachedData) throws SingleSignOnCommunicationException, ParserConfigurationException, SAXException, IOException;
}
