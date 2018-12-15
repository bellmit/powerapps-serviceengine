package com.profitera.rpm;

import java.util.List;

/**
 * @author jamison
 */
public interface RuleFiredListener {
    public void clearLog();
	public List getFiredRuleIds();
	public void ruleFired(Long id, String name);
}
