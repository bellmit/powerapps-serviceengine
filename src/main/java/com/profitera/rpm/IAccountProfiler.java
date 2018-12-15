package com.profitera.rpm;

import com.profitera.descriptor.db.account.Account;
import com.profitera.descriptor.rpm.UnsupportedAttributeException;
import com.profitera.rpm.expression.InvalidExpressionException;

public interface IAccountProfiler {

  public abstract void buildCacheTables() throws AgentFailureException, NoSuchAgentException,
      InvalidExpressionException, RuleEngineException;

  public abstract void dropCacheTables() throws AgentFailureException, NoSuchAgentException,
      InvalidExpressionException, RuleEngineException;

  public abstract void profileAccounts(boolean useCache, int startingAccountId, int endingAccountId)
      throws NoSuchAgentException, InvalidExpressionException, RuleEngineException;

  public abstract void profileAccount(Account a, RuleFiredListener l) throws NoSuchAgentException,
      InvalidExpressionException, RuleEngineException, UnsupportedAttributeException;

}