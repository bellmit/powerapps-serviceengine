package com.profitera.services.business.login;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import com.profitera.datasource.IDataSourceConfigurationSet;
import com.profitera.deployment.rmi.LoginServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.admin.PasswordManagerBean;
import com.profitera.log.DefaultLogProvider;
import com.profitera.log.ILogProvider;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.ProviderDrivenService;
import com.profitera.services.business.login.impl.ILoginSessionStore;
import com.profitera.services.business.login.impl.LoginServiceLogClient;

public abstract class LoginService extends ProviderDrivenService implements
    LoginServiceIntf {
  private static final String LOGIN_SESSIONTIMEOUT = "login.sessiontimeout";
  private ILoginSessionStore sessions;
  private ILogProvider logger;

  protected ILogProvider getLog() {
    if (logger == null) {
      logger = new DefaultLogProvider();
      logger.register(new LoginServiceLogClient());
    }
    return logger;
  }

  @Override
  public void setDataSourceConfigurations(IDataSourceConfigurationSet s) {
    super.setDataSourceConfigurations(s);
    getPasswordConstraints();
  }

  protected abstract ILoginSessionStore buildSessionStore();

  protected ILoginSessionStore getStore() {
    if (sessions == null) {
      sessions = buildSessionStore();
      String prop = ServiceEngine.getProp(LOGIN_SESSIONTIMEOUT);
      if (prop != null) {
        try {
          sessions.setSessionTimeout(1000L * 60L * Integer.parseInt(prop));
        } catch (NumberFormatException e) {
          getLog().emit(LoginServiceLogClient.SERVER_SESSION_TIMEOUT_BAD, LOGIN_SESSIONTIMEOUT, prop);
        }
      }
    }
    return sessions;
  }

  public abstract void audit(String userId, String module, String action,
      String hostname, String remarks, int status, Timestamp startTime);
  public abstract String getSessionUser(Long session);

  public TransferObject getAllPasswordSetting() {
    PasswordManagerBean bean = new PasswordManagerBean();
    TransferObject obj = getPasswordConstraints();
    @SuppressWarnings("unchecked")
    Map<Object, Object> config = (Map<Object, Object>) obj.getBeanHolder();
    bean.setPasswordSettings(config);
    return new TransferObject(bean);
  }

  public void updateLastSessionActive(Long session, Date date) {
    getStore().updateSessionLastActive(session, date);
  }
}