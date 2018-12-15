package com.profitera.services.business.login;

import java.util.Collections;
import java.util.Map;

import com.profitera.services.business.login.ISingleSignOnImplementation.ISingleSignOnSession;

public class DefaultSingleSignOnSession implements ISingleSignOnSession {
  private final String userName;
  private final long roleId;
  private final Map<Long, String> accessRights;

  public DefaultSingleSignOnSession(String userName, long roleId, Map<Long, String> accessRights) {
    this.userName = userName;
    this.roleId = roleId;
    this.accessRights = Collections.unmodifiableMap(accessRights);
  }

  @Override
  public String getUserName() {
    return userName;
  }

  @Override
  public long getRole() {
    return roleId;
  }

  @Override
  public Map<Long, String> getAccessRights() {
    return accessRights;
  }

}