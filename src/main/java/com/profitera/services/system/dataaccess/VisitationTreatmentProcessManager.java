/**
 * 
 */
package com.profitera.services.system.dataaccess;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Element;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.descriptor.business.meta.ITreatmentProcess;
import com.profitera.services.system.lookup.LookupManager;
import com.profitera.util.xml.XMLConfigUtil;

/**
 * @author cmlow
 *
 */
public class VisitationTreatmentProcessManager extends
		DefaultTreatmentProcessManager {
	private Long successfulStatusId;	
	private Long pendingStatusId;
	private Long cancelledStatusId;

	/* (non-Javadoc)
	 * @see com.profitera.services.system.dataaccess.ITreatmentProcessDataManager#configure(java.lang.Object, org.w3c.dom.Element)
	 */
	public void configure(Object id, Element e) {
		configure(id, XMLConfigUtil.getProperties(e));
	}
	
	protected void configure(Object id, Properties p) {
		String s = (String) p.get("SUCCESSFUL_STATUS_ID");
		if (s != null){
			successfulStatusId = Long.valueOf(s);
		}
		String s1 = (String) p.get("PENDING_TYPE_STATUS_ID");
		if (s1 != null){
			pendingStatusId = Long.valueOf(s1);
		}
		String s2 = (String) p.get("CANCELLED_TYPE_STATUS_ID");
		if (s2 != null){
			cancelledStatusId = Long.valueOf(s2);
		}
	}
	
	public void processPostUpdate(Long accountId, Map process, Date date, String user, ITransaction t, IReadWriteDataProvider p)throws SQLException, AbortTransactionException {
		if (process.get(ITreatmentProcess.PROCESS_STATUS_ID) != null && process.get(ITreatmentProcess.PROCESS_STATUS_ID).equals(successfulStatusId)){
			ListQueryServiceIntf service = (ListQueryServiceIntf) LookupManager.getInstance().getLookupItem(LookupManager.BUSINESS, "ListQueryService");
			// retrieve pending site visit action for cancellation
			Map args = new HashMap();
			args.put("ACCOUNT_ID", accountId);
			args.put("STATUS", pendingStatusId);
			for (Iterator i = p.query(IReadOnlyDataProvider.LIST_RESULTS, "getVisitationByTypeStatusId", args); i.hasNext();) {
				Map tp = (Map)i.next();
				if (!tp.get(ITreatmentProcess.TREATMENT_PROCESS_ID).equals(process.get(ITreatmentProcess.TREATMENT_PROCESS_ID))){
					List l = (List) service.getQueryList("getCompleteTreatmentProcess", tp).getBeanHolder();
					if (l != null && l.size() > 0){
						Map m = (Map)l.get(0);
						m.put(ITreatmentProcess.PROCESS_TYPE_STATUS_ID, cancelledStatusId);
						m.put(ITreatmentProcess.PROCESS_STATUS_ID, ITreatmentProcess.CANCELLED_STATUS);
						m.put(ITreatmentProcess.USER_ID, user);
						updateTreatmentProcess(accountId, m, date, user, t, p);
					}
				}
			}
		}
	}
	
	protected void insertAppointment(Map process, ITransaction t,
			IReadWriteDataProvider p) throws SQLException {
		p.insert("insertAppointmentTreatmentProcess", process, t);
		p.insert("insertVisitationLog", process, t);
	}
	
}
