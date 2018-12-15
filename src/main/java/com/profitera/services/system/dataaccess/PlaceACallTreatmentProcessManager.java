/*
 * Created on Jan 5, 2006
 *
 */
package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.meta.ITreatmentProcess;

/**
 * @author weionnho
 *  
 */
public class PlaceACallTreatmentProcessManager extends
		DefaultTreatmentProcessManager {
	public Long createTreatmentProcess(Map plan, Map process, Long accountId,
			Date date, Long typeId, String user, ITransaction t,
			IReadWriteDataProvider p) throws AbortTransactionException,
			SQLException {

		Object obj = process.get("SCHEDULED_DATE");
		if (obj != null) {
			Date scheduleDate = (Date) obj;
			process.put(ITreatmentProcess.EXPECTED_START_DATE, scheduleDate);
		} 
		
		return super.createTreatmentProcess(plan,process,accountId,date,typeId,user,t,p);
		
	}

}
