package com.profitera.services.business.customer;

import java.util.List;
import java.util.Map;

import com.profitera.deployment.rmi.CollateralInformationServiceIntf;
import com.profitera.descriptor.business.TransferObject;

public class CollateralInformationService extends AbstractInformationService implements CollateralInformationServiceIntf{

	private static final String GET_COLLATERAL_INFORMATION = "getCollateralInformation";
	private static final String INSERT_COLLATERAL_INFORMATION = "insertCollateralInformation";
	private static final String UPDATE_COLLATERAL_INFORMATION = "updateCollateralInformation";
	private static final String INSERT_COLLATERAL_INFORMATION_HISTORY = "insertCollateralInformationHistory";
	private static final String KEY_NAME = "COLLATERAL_ID";
	
	public TransferObject updateCollateralInformation(Map info, Long collateralId, String user) {
		return super.updateInformation(info, collateralId, user);
	}

	public TransferObject updateMultipleCollateralInformation(List collateralList, String user) {
		return super.updateMultipleInformation(collateralList, user);
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
