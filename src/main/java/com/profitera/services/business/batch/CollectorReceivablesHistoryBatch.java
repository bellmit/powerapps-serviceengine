/**
 * To query and store the previous month collector receivables statistics into
 * table PTRCOLLECTOR_RECEIVABLES_HISTORY.
 * Recommended to be run on 1st of the month morning before work list generation
 * Frequency - Monthly
 */
package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.profitera.dataaccess.AbortTransactionException;
import com.profitera.dataaccess.IRunnableTransaction;
import com.profitera.dataaccess.ITransaction;
import com.profitera.deployment.rmi.ListQueryServiceIntf;
import com.profitera.descriptor.business.TransferObject;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

/**
 * @author cmlow
 *
 */
public class CollectorReceivablesHistoryBatch extends AbstractProviderBatch {
	private static String GET_COLLECTOR_MTD_RECEIVABLES_BY_USER = "getCollectorMonthToDateReceivablesByUser";
	private static String INSERT_COLLECTOR_RECEIVABLES_HISTORY = "insertCollectorReceivablesHistory";
	private static final Log log = LogFactory.getLog(CollectorReceivablesHistoryBatch.class);

	private TransferObject updateCollectorReceivablesHistory(Date effectiveDate){
		// need to set an argument called TODAY_DATE with the value end of previous month		
		Date evalDate = getLastDayOfPreviousMonth(effectiveDate);
		log.info("Collector Receivables History Update for month ending " + evalDate);
		Map args = new HashMap();
		args.put("TODAY_DATE", evalDate);
		
		final Date histDate = getFirstDayOfMonth(evalDate);
		
		ListQueryServiceIntf lqs = getListQueryService();
		TransferObject to = lqs.getQueryList(GET_COLLECTOR_MTD_RECEIVABLES_BY_USER, args);
		if (to.isFailed())
			return to;
		
		final List res = (List) to.getBeanHolder();
		if (res != null && res.size() > 0){
			// save into table PTRCOLLECTOR_RECEIVABLES_HISTORY 
			final IReadWriteDataProvider p = getReadWriteProvider();
	        try {
	        	p.execute(new IRunnableTransaction(){
	        		public void execute(ITransaction t) throws SQLException, AbortTransactionException {
	        			for (Iterator iter = res.iterator(); iter.hasNext();) {
	        				Map m = (Map)iter.next();
	        				m.put("HISTORY_DATE", histDate);
	        				p.insert(INSERT_COLLECTOR_RECEIVABLES_HISTORY, m, t);
	        			}
	        		}});
	        } catch (SQLException e) {
	        	log.fatal("Failed to execute query: " + INSERT_COLLECTOR_RECEIVABLES_HISTORY, e);
	        	return new TransferObject(TransferObject.ERROR, "QUERY_FAILED - " + INSERT_COLLECTOR_RECEIVABLES_HISTORY);
	        } catch (AbortTransactionException e) {
	        	log.fatal("Failed to execute query: " + INSERT_COLLECTOR_RECEIVABLES_HISTORY, e);
	        	return new TransferObject(TransferObject.ERROR, "QUERY_FAILED - " + INSERT_COLLECTOR_RECEIVABLES_HISTORY);
	        }
		}
		return new TransferObject();
	}
	
	private Date getLastDayOfPreviousMonth(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.DATE, -1);
		return c.getTime();
	}

	private Date getFirstDayOfMonth(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTime();
	}

	/* (non-Javadoc)
	 * @see com.profitera.services.business.batch.IBatchProcess#invoke(java.lang.String, java.util.Date, java.util.Map)
	 */
	public TransferObject invoke() {
		return updateCollectorReceivablesHistory(getEffectiveDate());
	}

	protected String getBatchDocumentation() {
		return "Batch program to store collector's previous month receivable statistics";
	}

	protected String getBatchSummary() {
		return "To query and store the previous month collector receivables statistics into table PTRCOLLECTOR_RECEIVABLES_HISTORY. Recommended to be run on 1st of the month morning before work list generation Frequency - Monthly";
	}
}
