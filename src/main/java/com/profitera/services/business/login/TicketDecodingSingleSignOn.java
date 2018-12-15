package com.profitera.services.business.login;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class TicketDecodingSingleSignOn extends AbstractSingleSignOn implements ISingleSignOnImplementation {
  @Override
  public ISingleSignOnSession getAuthenticatedUser(String ticket, IReadWriteDataProvider loginDataProvider)
      throws SingleSignOnCommunicationException {
    String name = ticket.substring(0, ticket.indexOf(":"));
    MapLoginService l = getLoginService();
    Map<String, Object> user;
    try {
      user = l.getUser(name);
      if (l.isUserPassword(user, name, ticket.substring(ticket.indexOf(":") + 1))) {
        Long roleId = getLocalRoleForUser(name);
        if (roleId != null) {
          return new DefaultSingleSignOnSession(name, roleId, getLocalAccessRightsForRole(roleId));
        }
      }
    } catch (SQLException e) {
      throw new SingleSignOnCommunicationException(e);
    }
    return null;
  }


  @Override
  public void acknowledgeLogin(ISingleSignOnSession ssoSession) {
    // Do nothing
  }

  @Override
  public void logoff(String userName, Object sessionAttachedData) throws SingleSignOnCommunicationException {
    //Nothing to do here
  }

  @Override
  public List<Map<String, Object>> getBulkUserInformation(Date from, Date to, String organization, String branch, String user,
      Object sessionAttachedData) {
    // TODO: Should query the local DB I guess but actually nothing "changed"
    return new ArrayList<>();
  }

}
