package com.profitera.services.business.login;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.profitera.descriptor.business.admin.AccessRightsBean;
import com.profitera.descriptor.business.login.UserRoleBean;
import com.profitera.services.system.lookup.LookupManager;

public abstract class AbstractSingleSignOn implements ISingleSignOnImplementation{
  private Properties properties;
  public AbstractSingleSignOn() {
    super();
  }
  @Override
  public void setProperties(Properties p) {
    this.properties = p;
  }
  protected Properties getProperties() {
    return this.properties;
  }
  protected Map<Long, String> getSimpleAccessRightsMap(List<AccessRightsBean> roleRights) {
    Map<Long, String> accessRights = new HashMap<>();
    for (AccessRightsBean accessRightsBean : roleRights) {
      accessRights.put(accessRightsBean.getId().longValue(), accessRightsBean.getCode());
    }
    return accessRights;
  }
  private AccessRightsBean buildAccessRightsBean(Entry<Long, String> entry) {
    AccessRightsBean b = new AccessRightsBean();
    b.setId(entry.getKey().doubleValue());
    b.setCode(entry.getValue());
    b.setDesc(entry.getValue());
    b.setSortPriority(0);
    return b;
  }

  @Override
  public List<AccessRightsBean> getAccessRights(Object sessionAttachedData) throws SingleSignOnCommunicationException {
    List<AccessRightsBean> beans = new ArrayList<>();
    ISingleSignOnSession d = (ISingleSignOnSession) sessionAttachedData;
    Map<Long, String> accessRights = d.getAccessRights();
    Set<Entry<Long,String>> entrySet = accessRights.entrySet();
    for (Entry<Long, String> entry : entrySet) {
      beans.add(buildAccessRightsBean(entry));
    }
    return beans;
  }
  protected MapLoginService getLoginService() {
    LookupManager lm = LookupManager.getInstance();
    MapLoginService l = (MapLoginService) lm.getLookupItem(LookupManager.BUSINESS, "LoginService");
    return l;
  }
  protected Long getLocalRoleForUser(String name) throws SQLException {
    UserRoleBean[] roles = getLoginService().getRoles(name);
    if (roles.length == 0) {
      return null; // No role, no go.
    }
    return roles[0].getRoleId().longValue();
  }
  protected Map<Long, String> getLocalAccessRightsForRole(long roleId) throws SQLException {
    List<AccessRightsBean> roleRights = getLoginService().getAccessRights(roleId);
    Map<Long, String> accessRights = getSimpleAccessRightsMap(roleRights);
    return accessRights;
  }

}