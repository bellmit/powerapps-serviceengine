/*
 * Created on Nov 13, 2003
 */
package com.profitera.descriptor.rpm;

import oracle.toplink.sessions.Session;

import com.profitera.descriptor.db.account.Customer;
import com.profitera.rpm.RPM;

/**
 * @author Jamison Masse
 */
public class CustomerProfile extends BaseDescriptor{
	private Customer customer;
	
	private static final String[] propertyNames =
		{ "getTotalSpentSince", "getAverageLineOfCreditSince", "getTransactionCount", "getAverageBalanceCarriedForwardSince", "getOldestAccountAge", "getMaxAccountProfileScore", "getDueAmount"};

	public CustomerProfile(Session tlSession){
		super(RPM.CUSTOMER_QUERY_FILE);
		session = tlSession;
	}
	
	public void setCustomer(Customer c){
		customer = c;
		setId(c.getCustomerId());
	}
	
	private Number getValueByQuery(String propName, Object[] args, Number defaultValue) {
		return getValueByQuery(propName, args, customer.getCustomerId(), defaultValue, session);
	}
	
	public double getTotalSpentSince(int cycles){
		return ((Number)getValueByQuery("getTotalSpentSince", new Object[]{new Integer(cycles)}, new Double(0))).intValue();
	}
	
	public double getAverageLineOfCreditSince(int cycles){
		return ((Number)getValueByQuery("getAverageLineOfCreditSince", new Object[]{new Integer(cycles)}, new Double(0))).intValue();
	}
	
	public int getTransactionCount(int cycles){
		return ((Number)getValueByQuery("getTransactionCount", new Object[]{new Integer(cycles)}, new Double(0))).intValue();
	}
	public double getAverageBalanceCarriedForwardSince(int cycles){
		return ((Number)getValueByQuery("getAverageBalanceCarriedForwardSince", new Object[]{new Integer(cycles)}, new Double(0))).intValue();
	}
	

	public int getOldestAccountAge() {
		return ((Number)getValueByQuery("getOldestAccountAge", new Object[]{}, new Double(0))).intValue();
	}
	
	public double getMaxAccountProfileScore() {
		return ((Number)getValueByQuery("getMaxAccountProfileScore", new Object[]{}, new Double(0))).intValue();
	}
	
	public double getDueAmount(int cycles) {
		return ((Number)getValueByQuery("getDueAmount", new Object[]{new Integer(cycles)}, new Double(0))).intValue();
	}

	/**
	 * @see com.profitera.descriptor.rpm.BaseDescriptor#getSupportedProperties()
	 */
	protected String[] getSupportedProperties() {
		return propertyNames;
	}

}
