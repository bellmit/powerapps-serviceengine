package com.profitera.services.business.customer;

import java.util.List;
import java.util.Map;

import com.profitera.deployment.rmi.AccountInformationServiceIntf;
import com.profitera.descriptor.business.TransferObject;

/**
 * @author avitus
 *
 */
public class AccountInformationService extends AbstractInformationService implements AccountInformationServiceIntf {
	private static final String GET_ACCOUNT_INFORMATION = "getAccountInformation";
	private static final String INSERT_ACCOUNT_INFORMATION = "insertAccountInformation";
	private static final String UPDATE_ACCOUNT_INFORMATION = "updateAccountInformation";
	private static final String INSERT_ACCOUNT_INFORMATION_HISTORY = "insertAccountInformationHistory";
	private static final String KEY_NAME = "ACCOUNT_ID";

	protected String getInformationHistoryStatementName() {
		return INSERT_ACCOUNT_INFORMATION_HISTORY;
	}

	protected String getInformationInsertStatementName() {
		return INSERT_ACCOUNT_INFORMATION;
	}

	protected String getInformationKeyName() {
		return KEY_NAME;
	}

	protected String getInformationSelectionQueryName() {
		return GET_ACCOUNT_INFORMATION;
	}

	protected String getInformationUpdateStatementName() {
		return UPDATE_ACCOUNT_INFORMATION;
	}

	public TransferObject updateAccountInformation(Map info, Long accountId, String user) {
		return super.updateInformation(info, accountId, user);
	}
	
	public TransferObject updateMultipleAccountInformation(List accountList, String user) {
		return super.updateMultipleInformation(accountList, user);
	}
}
