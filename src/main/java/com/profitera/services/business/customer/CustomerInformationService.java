package com.profitera.services.business.customer;

import java.util.Map;

import com.profitera.deployment.rmi.CustomerInformationServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.meta.ICustomer;

public class CustomerInformationService extends AbstractInformationService implements CustomerInformationServiceIntf {
	private static final String GET_CUSTOMER_INFORMATION = "getCustomerInformation";
	private static final String INSERT_CUSTOMER_INFORMATION = "insertCustomerInformation";
	private static final String UPDATE_CUSTOMER_INFORMATION = "updateCustomerInformation";
	private static final String INSERT_CUSTOMER_INFORMATION_LOG = "insertCustomerInformationHistory";

	protected String getInformationHistoryStatementName() {
		return INSERT_CUSTOMER_INFORMATION_LOG;
	}

	protected String getInformationInsertStatementName() {
		return INSERT_CUSTOMER_INFORMATION;
	}

	protected String getInformationSelectionQueryName() {
		return GET_CUSTOMER_INFORMATION;
	}

	protected String getInformationKeyName() {
		return ICustomer.CUSTOMER_ID;
	}

	protected String getInformationUpdateStatementName() {
		return UPDATE_CUSTOMER_INFORMATION;
	}

	public TransferObject updateCustomerInformation(Map info, String customerId, String user) {
		return super.updateInformation(info, customerId, user);
	}

}
