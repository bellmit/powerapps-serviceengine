package com.profitera.services.business.rpm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AccountRuleLogger implements IAccountRuleListener {
  private static final Log LOG = LogFactory.getLog(IAccountRuleListener.class);
  private Long accountId;
  private List fired = new ArrayList();
  public void setAccountId(Long accountId) {
    this.accountId = accountId;
  }

  public void clearLog() {
    fired.clear();
  }

  public List getFiredRuleIds() {
    return fired;
  }

  public void ruleFired(Long id, String name) {
    fired.add(id);
    getLog().info("Fired: " + id + " for " + accountId + " (" + name + ")");
  }
  
  protected Log getLog(){
    return LOG;
  }

}
