package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Element;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.util.xml.XMLConfigUtil;

public class DefaultTreatmentProcessManager extends AbstractTreatmentProcessManager implements
		ITreatmentProcessDataManager {

  protected void createExtendedProcessInformation(Long accountId, Map process, Long typeId, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
    if (typeId.longValue() == RPMDataManager.LEGAL_ACTION_TREATMENT_PROCESS) {
			insertLegalAction(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.OUTSOURCE_AGENCY_TREATMENT_PROCESS) {
			insertOutsourceAgency(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.PLACE_A_CALL_TREATMENT_PROCESS) {
			insertCall(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.NOTES_TREATMENT_PROCESS) {
			insertNote(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.APPOINTMENT_TREATMENT_PROCESS) {
			insertAppointment(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.LETTER_TREATMENT_PROCESS) {
			insertLetter(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.SMS_TREATMENT_PROCESS) {
			insertSMS(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.PAYMENT_PLAN_TREATMENT_PROCESS) {
			insertPaymentPlan(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.PAYOFF_INQUIRY_TREATMENT_PROCESS) {
			insertPayoffInquiry(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.TPS_TREATMENT_PROCESS) {
			insertTPS(process, t, p);
		} else {
			throw new AbortTransactionException(
					"Unable to handle treatment process type " + typeId);
		}
  }
  
	protected void insertAppointment(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.insert("insertAppointmentTreatmentProcess", process, t);
	}

	protected void insertNote(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		// Do nothing..
	}

	protected void insertSMS(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.insert("insertSMSMessageTreatmentProcess", process, t);
	}

	protected void insertPaymentPlan(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.insert("insertPaymentPlanTreatmentProcess", process, t);
		List installments = (List) process.get("INSTALLMENTS");
		if (installments == null || installments.size() == 0){
			throw new IllegalArgumentException("Required field " + "INSTALLMENTS" + " could not be found, Payment Plan editor probably misconfigured");
		}
		for (int i = 0; i < installments.size(); i++) {
			((Map) installments.get(i)).putAll(process);
			p.insert("insertPaymentInstallmentTreatmentProcess", installments.get(i), t);
		}
	}
	
	protected void insertPayoffInquiry(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.insert("insertPayoffInquiryTreatmentProcess", process, t);
	}
	
	protected void insertTPS(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.insert("insertTPSTreatmentProcess", process, t);
	}

	protected void insertCall(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.insert("insertCallTreatmentProcess", process, t);
	}

	protected void insertLetter(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.insert("insertLetterTreatmentProcess", process, t);
	}

	protected void insertOutsourceAgency(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.insert("insertOutsourceAgencyTreatmentProcess", process, t);
	}

	protected void insertLegalAction(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.insert("insertLegalActionTreatmentProcess", process, t);
	}

	protected Long getProcessStatus(Map process, IReadWriteDataProvider p) {
		Long status = (Long) process.get(ITreatmentProcess.PROCESS_STATUS_ID);
		if (status == null) {
			status = IN_PROGRESS;
		}
		return status;
	}

  protected void updateExtendedProcessInformation(Long accountId, Map process, Long typeId, Date date, String user, ITransaction t, IReadWriteDataProvider p) throws SQLException, AbortTransactionException {
		if (typeId.longValue() == RPMDataManager.LEGAL_ACTION_TREATMENT_PROCESS) {
			updateLegalAction(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.OUTSOURCE_AGENCY_TREATMENT_PROCESS) {
			updateOutsourceAgency(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.PAYMENT_PLAN_TREATMENT_PROCESS) {
			updatePaymentPlan(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.PLACE_A_CALL_TREATMENT_PROCESS) {
			updateCall(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.APPOINTMENT_TREATMENT_PROCESS) {
			updateAppointment(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.LETTER_TREATMENT_PROCESS) {
			updateLetter(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.SMS_TREATMENT_PROCESS) {
			updateSMS(process, t, p);
		} else if (typeId.longValue() == RPMDataManager.PAYOFF_INQUIRY_TREATMENT_PROCESS) {
			updatePayoffInquiry(process, t, p);
		}else if (typeId.longValue() == RPMDataManager.TPS_TREATMENT_PROCESS) {
			updateTPS(process, t, p);
		}else {
			throw new AbortTransactionException(
					"Unable to handle treatment process type " + typeId);
		}
  }

	protected void updateLegalAction(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		//We do not use any of the fields in that table, so there will be
		// nothing to update.
		//p.update("updateLegalActionTreatmentProcess", process, t);
	}

	protected void updateOutsourceAgency(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		//We do not use any of the fields in that table, so there will be
		// nothing to update.
		//p.update("updateOutsourceAgencyTreatmentProcess", process, t);
	}

	protected void updateSMS(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.update("updateSMSMessageTreatmentProcess", process, t);
	}

	protected void updateLetter(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.update("updateLetterTreatmentProcess", process, t);
	}

	protected void updateAppointment(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.update("updateAppointmentTreatmentProcess", process, t);
	}

	protected void updateCall(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.update("updateCallTreatmentProcess", process, t);
	}

	private void updatePaymentPlan(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.update("updatePaymentPlan", process, t);
		List installments = (List) process.get("INSTALLMENTS");
		// Null installments is assumed to mean you did NOT want to update them.
		if (installments == null)
			return;
		
		//TODO: It is assumed this is never used to add or delete installments
		for (Iterator i = installments.iterator(); i.hasNext();) {
			Map installment = (Map) i.next();
			p.update("updatePaymentPlanInstallment", installment, t);
		}
	}
	protected void updatePayoffInquiry(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.update("updatePayoffInquiry", process, t);
	}
	
	protected void updateTPS(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.update("updateTPS", process, t);
	}

	protected void configure(Object id, Properties p) {
		// Ignore the config right now, we don't use it.

	}

  public void configure(Object id, Element e) {
    configure(id, XMLConfigUtil.getProperties(e));
  }
}
