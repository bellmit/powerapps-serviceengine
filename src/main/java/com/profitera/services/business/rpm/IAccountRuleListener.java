package com.profitera.services.business.rpm;

import com.profitera.rpm.RuleFiredListener;

public interface IAccountRuleListener extends RuleFiredListener {
  public void setAccountId(Long accountId);
}
