package com.profitera.services.business.login;

import java.util.Date;

import com.profitera.deployment.rmi.LoginServiceIntf;
import com.profitera.ibatis.SQLMapFileRenderer;
import com.profitera.services.business.login.impl.ILoginSessionStore;
import com.profitera.services.business.login.impl.PersistentLoginSessionStore;

public class FailoverLoginService extends MapLoginService implements LoginServiceIntf {
  @Override
  protected ILoginSessionStore buildSessionStore() {
    return new PersistentLoginSessionStore(getPrivateProvider()){
      @Override
      public void handleSessionTimeout(long session, String userId, Date lastRequestTime) {
        triggerServerTimeoutLogoutEvent(session);
      }};
  }

  protected String getAdditionalSQL(SQLMapFileRenderer renderer) {
    return PersistentLoginSessionStore.getSQL(renderer);
  }
}
