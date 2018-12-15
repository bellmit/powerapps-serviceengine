/*
 * Created on Aug 22, 2003
 */
package com.profitera.rpm;

import java.util.List;
import java.util.Vector;


/**
 * @author jamison
 *
 */
public class RulesFiredIdLogger implements RuleFiredListener {
	List ids = new Vector();

	public void clearLog() {
		ids = new Vector();
	}

	/**
	 * The vector is full of Long objects!
	 * @return
	 */
	public List getFiredRuleIds() {
		return ids;
	}

	/**
     * @see com.profitera.rpm.RuleFiredListener#ruleFired(java.lang.Long, java.lang.String)
     */
    public void ruleFired(Long id, String name) {
        ids.add(id);
    }

}
