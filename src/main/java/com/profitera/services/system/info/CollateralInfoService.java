package com.profitera.services.system.info;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

public class CollateralInfoService extends AbstractInfoService {

	private static final String GET_COLLATERAL_INFORMATION = "getCollateralInformation";
	private static final String INSERT_COLLATERAL_INFORMATION = "insertCollateralInformation";
	private static final String UPDATE_COLLATERAL_INFORMATION = "updateCollateralInformation";
	private static final String INSERT_COLLATERAL_INFORMATION_HISTORY = "insertCollateralInformationHistory";
	private static final String KEY_NAME = "COLLATERAL_ID";
	
	public IRunnableTransaction updateCollateralInformation(Map info, Long collateralId, String user, Date d, IReadWriteDataProvider p) throws SQLException {
		return super.updateInformation(info, collateralId, user, d, p);
	}

	public IRunnableTransaction updateMultipleCollateralInformation(List collateralList, String user, Date d, IReadWriteDataProvider p) throws SQLException {
		return super.updateMultipleInformation(collateralList, user, d, p);
	}

	protected String getInformationHistoryStatementName() {
		return INSERT_COLLATERAL_INFORMATION_HISTORY;
	}

	protected String getInformationInsertStatementName() {
		return INSERT_COLLATERAL_INFORMATION;
	}

	protected String getInformationKeyName() {
		return KEY_NAME;
	}

	protected String getInformationSelectionQueryName() {
		return GET_COLLATERAL_INFORMATION;
	}

	protected String getInformationUpdateStatementName() {
		return UPDATE_COLLATERAL_INFORMATION;
	}

}
