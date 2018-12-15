package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.services.business.template.TemplateService;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.lookup.LookupManager;

public class RepossessionTreatmentProcessDataManager extends
		DefaultTreatmentProcessManager {

	private TemplateService templateService;

	// This makes sure that the validity based on subtype code is parsed
	// and the number of days valid is taken into consideration for
	// setting expected end date.
	// THIS IS HIGHLY DEPENDANT ON THE SUBTYPE_CODE OF RO/RA carrying a syntax of
	// <code>_<no of days>, e.g.
	// RO_1, RO_2, RO_3
	protected void preprocessTreatmentProcessInformationForInsert(Map process,
			Long typeId, ITransaction t, IReadWriteDataProvider p)
			throws SQLException, AbortTransactionException {
		preprocessTreatmentProcessInformationForUpdate(process, t, p);
	}

	protected void preprocessTreatmentProcessInformationForUpdate(Map process,
			ITransaction t, IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
		Long processSubtypeId = (Long) process
				.get(ITreatmentProcess.PROCESS_SUBTYPE_ID);
		Date createdDate = (Date) process.get(ITreatmentProcess.CREATED_DATE);
		String processSubtypeCode = (String) p.queryObject(
				"getProcessSubtypeCodeById", processSubtypeId);
		int daysValid = getValidDays(processSubtypeCode);
		Calendar validDaysCal = Calendar.getInstance();
		validDaysCal.setTime(createdDate);
		validDaysCal.add(Calendar.DATE, daysValid);
		process.put(ITreatmentProcess.EXPECTED_END_DATE, new Date(validDaysCal
				.getTimeInMillis()));
	}

	protected void createExtendedProcessInformation(Long accountId, Map process,
			Long typeId, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
		insertDocument(process, t, p);
		
		if (process.get("PROCESS_STATUS_ID") != null
				&& process.get("PROCESS_STATUS_ID").equals(new Long(15004))) {
			Calendar cal = Calendar.getInstance();
			process.put("REPOSESSION_DATE", cal.getTime());
		} else {
			process.put("REPOSESSION_DATE", null);
		}
		p.insert("insertRepossessionOrderTreatmentProcess", process, t);
	}

	protected void updateExtendedProcessInformation(Long accountId, Map process,
			Long typeId, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
		updateDocument(process, t, p);
	
		if (process.get("PROCESS_STATUS_ID") != null
				&& process.get("PROCESS_STATUS_ID").equals(new Long(15004))) {
			Calendar cal = Calendar.getInstance();
			process.put("REPOSESSION_DATE", cal.getTime());
		} else {
			process.put("REPOSESSION_DATE", null);
		}
		p.insert("updateRepossessionOrderTreatmentProcess", process, t);
	}

	private TemplateService getTemplateService() {
		if (templateService == null) {
			templateService = new TemplateService();
		}
		return templateService;
	}

	private IDocumentService getDocumentService() {
		final IDocumentService docService = (IDocumentService) LookupManager
				.getInstance().getLookupItem(LookupManager.SYSTEM, "DocumentService");
		return docService;
	}

	private void insertDocument(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
		if (process.get("TEMPLATE_ID") == null){
			getLog().error("Missing Template ID when trying to create document under " + RepossessionTreatmentProcessDataManager.class.getName());
			return;
		}
		String data = (String) getTemplateService().generateDocument(
				(Long) process.get("TEMPLATE_ID"), process).getBeanHolder();
		IDocumentTransaction dt = getDocumentService().createDocument(4, "Repossession document generated using template ID "+process.get("TEMPLATE_ID"), data,
				p);
		dt.execute(t);
		process.put("DOCUMENT_ID", dt.getId());
		p.insert("insertDocumentTreatmentProcess", process, t);
	}
	
	private void updateDocument(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
		if (process.get("TEMPLATE_ID") == null){
			getLog().error("Missing Template ID when trying to update document under " + RepossessionTreatmentProcessDataManager.class.getName());
			return;
		}
		if (process.get("DOCUMENT_ID") == null){
			getLog().error("Missing Document ID when trying to update document under " + RepossessionTreatmentProcessDataManager.class.getName());
			return;
		}
		String data = (String) getTemplateService().generateDocument(
				(Long) process.get("TEMPLATE_ID"), process).getBeanHolder();
		IDocumentTransaction dt = getDocumentService().updateDocument((Long) process.get("DOCUMENT_ID"), 4, "", data,
				p);
		dt.execute(t);
	}

	private int getValidDays(String processSubtypeCode) {
		String[] broken = processSubtypeCode.split("_");
		String daysStr = broken[broken.length - 1];
		try {
			return Integer.parseInt(daysStr);
		} catch (Exception e) {
			getLog().error("Unable to parse valid days for RO from RO subtype code "
					+ daysStr);
			return 0;
		}
	}
}