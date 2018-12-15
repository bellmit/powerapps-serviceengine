/*
 * Created on Jan 2, 2006
 *
 */
package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.client.RemoteProgressThread;
import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.TemplateServiceIntf;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.services.system.document.IDocumentService;
import com.profitera.services.system.lookup.LookupManager;

/**
 * @author weionnho
 * 
 */
public class LetterTreatmentProcessManager extends
		DefaultTreatmentProcessManager {

	private TemplateServiceIntf templateService;
	public Long createTreatmentProcess(Map plan, Map process, Long accountId,
			Date date, Long typeId, String user, ITransaction t,
			IReadWriteDataProvider p) throws AbortTransactionException,
			SQLException {
		return super.createTreatmentProcess(plan, process, accountId, date,
				typeId, user, t, p);

	}

	protected void createExtendedProcessInformation(Long accountId, Map process,
			Long typeId, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
		// For manual process creation, run the template.
		// For System processes the notification service will
		// do our generation for us/it will be done as an update.
		Object manual = process.get(ITreatmentProcess.MANUAL);
		if (manual == null || ((Boolean) manual).equals(Boolean.TRUE)) {
			insertDocument(process, t, p);
		}
		super.createExtendedProcessInformation(accountId, process, typeId, date, user, t, p);
	}

	protected void updateExtendedProcessInformation(Long accountId, Map process,
			Long typeId, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
		updateDocument(process, t, p);

		super.updateExtendedProcessInformation(accountId, process, typeId, date, user, t, p);
	}

	private TemplateServiceIntf getTemplateService() {
		if (templateService == null) {
		  templateService = (TemplateServiceIntf) LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, RemoteProgressThread.TEMPLATE_SERVICE);
		}
		return templateService;
	}

	private IDocumentService getDocumentService() {
		final IDocumentService docService = (IDocumentService) LookupManager
				.getInstance().getLookupItem(LookupManager.SYSTEM,
						"DocumentService");
		return docService;
	}

	protected void preprocessTreatmentProcessInformationForUpdate(Map process, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
		if (process.get("TEMPLATE_ID") == null) {
			getLog().warn(
					"Missing Template ID when trying to update document under "
							+ this.getClass().getName());
			return;
		}
		
		String data = (String) getTemplateService().generateDocument(
				(Long) process.get("TEMPLATE_ID"), process).getBeanHolder();
		
		process.put("DOCUMENT_DATA",data);
	}
	
	private void updateDocument(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
		String data = (String) process.get("DOCUMENT_DATA");
		
		if(data==null){
			getLog().warn("No data is generated using template ID '" + process.get("TEMPLATE_ID") + "'");
			return;
		}
		if (process.get("DOCUMENT_ID") == null) {
			IDocumentTransaction dt = getDocumentService().createDocument(4,
					"Letter generated using template ID "+process.get("TEMPLATE_ID"), data, p);
			dt.execute(t);
			process.put("DOCUMENT_ID", dt.getId());
			p.insert("insertDocumentTreatmentProcess", process, t);
		} else {
			Long docId = (Long) process.get("DOCUMENT_ID");
			IDocumentTransaction dt = getDocumentService().updateDocument(
					docId, 4, "", data, p);
			dt.execute(t);
		}
	}

	protected void preprocessTreatmentProcessInformationForInsert(Map process,
			Long typeId, ITransaction t, IReadWriteDataProvider p)
			throws SQLException, AbortTransactionException {
		Object manual = process.get(ITreatmentProcess.MANUAL);
		if (manual == null || ((Boolean) manual).equals(Boolean.TRUE)) {
			if (process.get("TEMPLATE_ID") == null) {
				getLog().error(
						"Missing Template ID when trying to update document under "
								+ this.getClass().getName());
	
				return;
			}
			
			String data = (String) getTemplateService().generateDocument(
					(Long) process.get("TEMPLATE_ID"), process).getBeanHolder();
			
			process.put("DOCUMENT_DATA",data);
		}
	}

	private void insertDocument(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException,
			AbortTransactionException {
		String data = (String) process.get("DOCUMENT_DATA");
		
		if(data==null){
			getLog().warn("No data is generated using template ID '" + process.get("TEMPLATE_ID") + "'");
			return;
		}
		
		IDocumentTransaction dt = getDocumentService().createDocument(4, "Letter generated using template ID "+process.get("TEMPLATE_ID"),
				data, p);
		dt.execute(t);
		process.put("DOCUMENT_ID", dt.getId());
		p.insert("insertDocumentTreatmentProcess", process, t);

	}

}
