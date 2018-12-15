package com.profitera.rpm;

import java.util.Map;

import com.profitera.descriptor.rpm.Descriptor;
import com.profitera.descriptor.rpm.UnsupportedAttributeException;
import com.profitera.rpm.expression.Expression;
import com.profitera.rpm.expression.InvalidExpressionException;

/**
 * @author jamison
 */
public interface Rule {
    public abstract void configureRule(String agentCode, String ruleExpressionText, boolean log)
            throws InvalidExpressionException, RuleEngineException;

    public abstract void configureRule(String agentCode, String ruleExpressionText,
            String overrideName, boolean log) throws InvalidExpressionException,
            RuleEngineException;

    /**
     * @return The agent code of this rule.
     */
    public abstract String getAgentCode();

    public abstract String[] getDeclarations();

    public abstract Expression getCondition();

    public abstract Expression getAction();

    public abstract Map getUsedAttributesValues(Descriptor descriptor)
            throws InvalidExpressionException, UnsupportedAttributeException, RuleEngineException,
            UnsupportedOperationException;
}