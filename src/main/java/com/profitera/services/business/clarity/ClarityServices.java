package com.profitera.services.business.clarity;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Vector;

import com.profitera.deployment.rmi.ClarityServicesIntf;
import com.profitera.descriptor.business.ServiceException;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.descriptor.business.mtmf.MTMFMemoBusinessBean;
import com.profitera.descriptor.db.reference.ClarityMessagesRef;
import com.profitera.server.ServiceEngine;
import com.profitera.services.business.BusinessService;
import com.profitera.services.business.reference.ReferenceService;
import com.profitera.services.system.clarity.ClarityInteractor;
import com.profitera.services.system.lookup.LookupManager;

public class ClarityServices extends BusinessService
		implements
			ClarityServicesIntf {
	private ClarityInteractor clarity = (ClarityInteractor) LookupManager.getInstance().getLookupItem(
					LookupManager.SYSTEM, "ClarityInteractor");
	private ReferenceService refService = null;
	
	private final boolean serviceEnabled = ServiceEngine.getProp(
			"Clarity-Enabled", "false").equalsIgnoreCase("true");
	private final boolean blockCardEnabled = ServiceEngine.getProp(
			"Clarity-Block-Card-Enabled", "false").equalsIgnoreCase("true");
	private final boolean waiverEnabled = ServiceEngine.getProp("Clarity-Waiver-Enabled", "false").equalsIgnoreCase("true");

	public ClarityServices() {
	}

	public TransferObject getMemo(String accNo, String custID, char memoType,
			boolean getNext, MTMFMemoBusinessBean bean) {

		if (!serviceEnabled)
			return new TransferObject(null, TransferObject.ERROR,
					"Clarity service is not enabled");

		if (clarity == null)
         return new TransferObject(null, TransferObject.ERROR,
					"Clarity Interactor has failed or broken");
			//clarity = (ClarityInteractor) LookupManager.getInstance().getLookupItem(
			//		LookupManager.SYSTEM, "ClarityInteractor");

		int fetchSize = ServiceEngine.getIntProp("MTMF-Fetch-Size", 5);

		Vector mtmfs = new Vector();

		for (int i = 0; i < fetchSize; i++) {
			try {
				MTMFMemoBusinessBean tmpBean = null;
				if (i == 0 && !getNext)
					tmpBean = clarity.fetchNotesFromMTMF(accNo, custID, memoType, false,
							bean);
				else
					tmpBean = clarity.fetchNotesFromMTMF(accNo, custID, memoType, true,
							bean);

				if (tmpBean != null && clarity.isSuccessful(tmpBean.getErrorCode())) {
					mtmfs.add(tmpBean);
					if (tmpBean.getEndOfRecInd() == 'N')
						break;
					else {
						bean.setEndOfRecInd(tmpBean.getEndOfRecInd());
						bean.setNextRecordKey(tmpBean.getNextRecordKey());
						bean.setDataElement(tmpBean.getDataElement());
						bean.setEchoBackErrorMessage(tmpBean.getEchoBackErrorMessage());
					}
				} else {
					return new TransferObject(mtmfs, TransferObject.ERROR,
							(tmpBean != null ? getErrorMessage(tmpBean.getErrorCode()) : "Unknown Error!"));
				}
			} catch (IOException e) {
				e.printStackTrace();
				return new TransferObject(null, TransferObject.EXCEPTION,
						"An unexpected communication error has happened, unable to contact host!");
			}
		}
		return new TransferObject(mtmfs);
	}

	public TransferObject addMemo(String accNo, String custId, char type,
			MTMFMemoBusinessBean bean) {
		String[] notes = bean.getContent();
		int flag = TransferObject.SUCCESS;
		String message = null;
		if (clarity == null) {
         return new TransferObject(null, TransferObject.ERROR,
					"Clarity Interactor has failed or broken");
			//clarity = (ClarityInteractor) LookupManager.getInstance().getLookupItem(
			//		LookupManager.SYSTEM, "ClarityInteractor");
		}
		if (!serviceEnabled)
			return new TransferObject(null, TransferObject.ERROR,
					"Clarity service is not enabled");
		try {
			message = clarity.addNotesToMTMF(accNo, custId, bean.getCreator(), type,
					bean.getCategoryCode(), bean.getDescription(), notes[0]);
		} catch (IOException e) {
			return new TransferObject(TransferObject.ERROR, e.getMessage());
		}
		if (clarity.isSuccessful(message))
			return new TransferObject("Successful");
		else
			return new TransferObject(TransferObject.ERROR, getErrorMessage(message));
	}

	public TransferObject blockCard(String accNo, String customerId,
			String userId, String blockCode) throws RemoteException, IOException {

		if (!serviceEnabled || !blockCardEnabled) {
			return new TransferObject("Online service disabled!");
		}
		
		if (blockCode == null || blockCode.trim().length() == 0)
			return new TransferObject(TransferObject.ERROR, "Missing or invalid block code!");

		if (clarity == null)
         return new TransferObject(null, TransferObject.ERROR,
					"Clarity Interactor has failed or broken");

		String message = clarity.blockCard(accNo, customerId, userId, blockCode);

		if (clarity.isSuccessful(message))
			return new TransferObject("Successful");
		else
			return new TransferObject(TransferObject.ERROR, getErrorMessage(message));

	}

	public TransferObject waiveCharges(String accNo, String customerId,
			String userId, Double amount, String waiveCode) throws ServiceException, IOException {

		if (!serviceEnabled || !waiverEnabled) {
			return new TransferObject("Online service disabled!");
		}
		
		if (amount == null)
			return new TransferObject(TransferObject.ERROR, "Invalid waiver amount!");
		
		if (clarity == null)
         return new TransferObject(null, TransferObject.ERROR,
					"Clarity Interactor has failed or broken");
               
		String message = clarity.waiverOfCharges(accNo, customerId, userId, amount, waiveCode);

		if (clarity.isSuccessful(message))
			return new TransferObject("Successful");
		else
			return new TransferObject(TransferObject.ERROR, getErrorMessage(message));	
	}

	private String getErrorMessage(String errorCode) {
		ClarityMessagesRef msgRef = getReferenceService().getClarityMessagesRefByCode(errorCode);
		if (msgRef != null)
			return msgRef.getDescription();
		else
			return "Unknown error, please contact administrator!\nError Code: " + errorCode;
	}
	private ReferenceService getReferenceService() {
		if (refService == null)
			refService = (ReferenceService) LookupManager.getInstance()
					.getLookupItem(LookupManager.BUSINESS, "ReferenceService");
		return refService;
	}
}