package com.profitera.services.system.info;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

/**
 * @author avitus
 *
 */
public class AccountInfoService extends AbstractInfoService {
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

	public IRunnableTransaction updateAccountInformation(Map info, Long accountId, String user, Date d, IReadWriteDataProvider p) throws SQLException {
		return super.updateInformation(info, accountId, user, d, p);
	}
	
	public IRunnableTransaction updateMultipleAccountInformation(List accountList, String user, Date d, IReadWriteDataProvider p) throws SQLException {
		return super.updateMultipleInformation(accountList, user, d, p);
	}
}
