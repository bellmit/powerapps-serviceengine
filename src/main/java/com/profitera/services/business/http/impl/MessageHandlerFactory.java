package com.profitera.services.business.http.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.profitera.auth.IAuthorizationProvider;
import com.profitera.cti.ICTIProvider;
import com.profitera.dataaccess.ISqlMapProvider;
import com.profitera.datasource.IDataSourceConfigurationSet;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.admin.AccessRightsBean;
import com.profitera.document.IDocumentProvider;
import com.profitera.event.license.ILicenseProvider;
import com.profitera.financial.IFinancialProvider;
import com.profitera.properties.IPropertyReader;
import com.profitera.server.ServiceEngine;
import com.profitera.server.impl.DefaultSqlMapProviderSet;
import com.profitera.services.business.http.DefaultMessageHandler;
import com.profitera.services.business.login.MapLoginService;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.loan.impl.DefaultFinancialProvider;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.services.system.mail.MailService;
import com.profitera.silverlake.ISilverLakeMessagingProvider;

public class MessageHandlerFactory {
  private IFinancialProvider getFinancialProvider() {
    return new DefaultFinancialProvider();
  }
  private ICTIProvider getCTIProvider() {
    return new DefaultCtiProvider();
  }
  private IDocumentService getDocumentService() {
    LookupManager lm = LookupManager.getInstance();
    return (IDocumentService) lm.getLookupItem(LookupManager.SYSTEM, "DocumentService");
  }

  private IReadWriteDataProvider getReadWriteProvider() {
    LookupManager l = LookupManager.getInstance();
    return (IReadWriteDataProvider) l.getLookupItem(LookupManager.SYSTEM, "ReadWriteProvider");
  }

  
  public DefaultMessageHandler build(final MapLoginService login) {
    final IDocumentService d = getDocumentService();

    IAuthorizationProvider auth = new IAuthorizationProvider() {
      public String getUserId(Long session) {
        if (session == null) {
          return null;
        }
        return login.getSessionUser(session);
      }

      public Set<Long> getSessionAuthorizations(long session) {
        Long roleId = getRoleId(session);
        if (roleId == null) {
          return Collections.emptySet();
        }
        Double id = new Double(roleId.longValue());
        TransferObject roleRights = login.getRoleRights(id.longValue());
        @SuppressWarnings("unchecked")
        List<AccessRightsBean> rights = (List<AccessRightsBean>) roleRights.getBeanHolder();
        Set<Long> s = new HashSet<Long>();
        for (AccessRightsBean a : rights) {
          long aid = a.getAccessRightsId().longValue();
          if (!s.contains(aid)) {
            s.add(aid);
          }
        }
        return s;
      }

      @Override
      public Long getRoleId(Long session) {
        //TODO: Simplify to check single access right?
        TransferObject sessionRole = login.getSessionRole(session);
        return (Long) sessionRole.getBeanHolder();
      }

      @Override
      public Object getSessionAttachedData(long session) {
        return login.getSessionAttachedData(session);
      }
    };
    IDocumentProvider doc = new DefaultDocumentProvider(d);
    IPropertyReader serverProps = new IPropertyReader() {
      public String getProperty(String name) {
        return getProperties().getProperty(name);
      }
      @Override
      public Properties getProperties() {
        return ServiceEngine.getConfig(true);
      }
    };
    ILicenseProvider license = new DefaultLicenseProvider();
    ISqlMapProvider defaultProvider = (ISqlMapProvider) getReadWriteProvider();
    IDataSourceConfigurationSet configurations = ServiceEngine.getDataSourceConfigurations();
    DefaultSqlMapProviderSet set = new DefaultSqlMapProviderSet(defaultProvider, configurations);
    ISilverLakeMessagingProvider silverlakeProvider = new DefaultSilverLakeMessagingProvider();
    return new DefaultMessageHandler(set, doc, auth, serverProps, new MailService(), getCTIProvider(),
        getFinancialProvider(), license, silverlakeProvider);
    
  }
}
