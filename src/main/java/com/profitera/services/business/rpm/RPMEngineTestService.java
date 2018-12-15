package com.profitera.services.business.rpm;

import com.profitera.deployment.rmi.RPMEngineTestServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.business.BusinessService;
import com.profitera.services.system.rpm.RuleTest;

public class RPMEngineTestService extends BusinessService implements RPMEngineTestServiceIntf {

	public RPMEngineTestService() {
	}

	/**
	 * Returns a vector with 3 elements, a hashtable array containing the descriptors
	 * (same as calling fetchAccountStates()), a 2 dim Long array containing the
	 * rules foired for each descriptor, and another Hashtable array containing
	 * the descriptors after the changes made by the rules have take effect.
	 *
	 * For thread-safety the implementation is in RuleTest, at each invokation we
	 * crate a Ruletest instance and make it do the work, see that class for the real goods. 
	 * @see com.profitera.deployment.rmi.RPMEngineTestServiceIntf#testRules(int, java.lang.Long[])
	 */
	public TransferObject testRules(int accountCount, Long[] ruleIds) {
		RuleTest rt = new RuleTest();
		return rt.testRules(accountCount,ruleIds);
	}
	
	/**
	 * This method will return a transfer object containing an array of hashtables,
	 * the keys are the names of the methods used by the rules and the data is a 
	 * Vector of Strings that represents the data passed in followed by the resulting value.
	 * If the method is called more than once the params are just appended to the end of 
	 * the vector. The descriptors/dictionaries should never have polymophic methods anyway.
	 * The client side can use the dictionary to pull these values and display 
	 * them properly.
	 * <br/>
	 * For thread-safety the implementation is in RuleTest, at each invokation we
	 * crate a Ruletest instance and make it do the work, see that class for the real goods. 
	 * 
	 * @see com.profitera.deployment.rmi.RPMEngineTestServiceIntf#fetchAccountStates(int, java.lang.Long[])
	 * @see com.profitera.services.system.rpm.RuleTest
	 */
	public TransferObject fetchAccountStates(int accountCount, Long[] ruleIds) {
		RuleTest rt = new RuleTest();
		return rt.fetchAccountStates(accountCount,ruleIds);
	}
}