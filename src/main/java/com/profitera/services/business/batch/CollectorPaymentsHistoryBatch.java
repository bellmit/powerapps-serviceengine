/**
 * To query and store the daily collector payments statistics into
 * table PTRCOLLECTOR_PAYMENTS_HISTORY and PTRCOLLECTOR_PAYMENTS_MTD_HISTORY.
 * Recommended to be run daily morning after payments are loaded and before work list generation
 * Frequency - Daily
 */
package com.profitera.services.business.batch;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public class CollectorPaymentsHistoryBatch extends AbstractProviderBatch {
	private static String GET_COLLECTOR_DAILY_PAYMENTS_BY_USER = "getCollectorDailyPaymentsHistoryByUser";
	private static String INSERT_COLLECTOR_PAYMENTS_HISTORY = "insertCollectorPaymentsHistory";
	private static String GET_COLLECTOR_MTD_PAYMENTS_BY_USER = "getCollectorMonthToDatePaymentsHistoryByUser";
	private static String INSERT_COLLECTOR_PAYMENTS_MTD_HISTORY = "insertCollectorPaymentsMTDHistory";

	private TransferObject updateCollectorPaymentsHistory(Date effectiveDate){
		// need to set an argument called PROCESS_DATE with the value of yesterday
		getLog().info("Collector Payments History Update for today " + effectiveDate);
		final Date evalDate = getYesterday(effectiveDate);
		Map args = new HashMap();
		args.put("TODAY_DATE", effectiveDate);
		args.put("PROCESS_DATE", evalDate);
		
		ListQueryServiceIntf lqs = getListQueryService();
		TransferObject to = lqs.getQueryList(GET_COLLECTOR_DAILY_PAYMENTS_BY_USER, args);
		if (to.isFailed())
			return to;
		
		
		final List res = (List) to.getBeanHolder();
		if (res != null && res.size() > 0){
			// save into table PTRCOLLECTOR_PAYMENTS_HISTORY 
			final IReadWriteDataProvider p = getReadWriteProvider();
	        try {
	        	p.execute(new IRunnableTransaction(){
	        		public void execute(ITransaction t) throws SQLException, AbortTransactionException {
	        			for (Iterator iter = res.iterator(); iter.hasNext();) {
	        				Map m = (Map)iter.next();
	        				m.put("HISTORY_DATE", evalDate);
	        				p.insert(INSERT_COLLECTOR_PAYMENTS_HISTORY, m, t);
	        			}
	        		}});
	        } catch (SQLException e) {
	        	getLog().fatal("Failed to execute query: " + INSERT_COLLECTOR_PAYMENTS_HISTORY, e);
	        	return new TransferObject(TransferObject.ERROR, "QUERY_FAILED - " + INSERT_COLLECTOR_PAYMENTS_HISTORY);
	        } catch (AbortTransactionException e) {
	        	getLog().fatal("Failed to execute query: " + INSERT_COLLECTOR_PAYMENTS_HISTORY, e);
	        	return new TransferObject(TransferObject.ERROR, "QUERY_FAILED - " + INSERT_COLLECTOR_PAYMENTS_HISTORY);
	        }
		}
		
		return new TransferObject();
	}

	private TransferObject updateCollectorPaymentsMTDHistory(Date effectiveDate){
		// need to set an argument called PROCESS_DATE with the value of yesterday
		getLog().info("Collector Payments MTD History Update for today " + effectiveDate);
		final Date evalDate = getYesterday(effectiveDate);
		Map args = new HashMap();
		args.put("TODAY_DATE", effectiveDate);
		args.put("PROCESS_DATE", evalDate);
		
		ListQueryServiceIntf lqs = getListQueryService();
		TransferObject to = lqs.getQueryList(GET_COLLECTOR_MTD_PAYMENTS_BY_USER, args);
		if (to.isFailed())
			return to;
		
		
		final List res = (List) to.getBeanHolder();
		if (res != null && res.size() > 0){
			// save into table PTRCOLLECTOR_PAYMENTS_MTD_HISTORY 
			final IReadWriteDataProvider p = getReadWriteProvider();
	        try {
	        	p.execute(new IRunnableTransaction(){
	        		public void execute(ITransaction t) throws SQLException, AbortTransactionException {
	        			for (Iterator iter = res.iterator(); iter.hasNext();) {
	        				Map m = (Map)iter.next();
	        				m.put("HISTORY_DATE", evalDate);
	        				p.insert(INSERT_COLLECTOR_PAYMENTS_MTD_HISTORY, m, t);
	        			}
	        		}});
	        } catch (SQLException e) {
	        	getLog().fatal("Failed to execute query: " + INSERT_COLLECTOR_PAYMENTS_MTD_HISTORY, e);
	        	return new TransferObject(TransferObject.ERROR, "QUERY_FAILED - " + INSERT_COLLECTOR_PAYMENTS_MTD_HISTORY);
	        } catch (AbortTransactionException e) {
	        	getLog().fatal("Failed to execute query: " + INSERT_COLLECTOR_PAYMENTS_HISTORY, e);
	        	return new TransferObject(TransferObject.ERROR, "QUERY_FAILED - " + INSERT_COLLECTOR_PAYMENTS_MTD_HISTORY);
	        }
		}
		
		return new TransferObject();
	}
	
	private Date getYesterday(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DATE, -1);
		return c.getTime();
	}
	
	/* (non-Javadoc)
	 * @see com.profitera.services.business.batch.IBatchProcess#invoke(java.lang.String, java.util.Date, java.util.Map)
	 */
	public TransferObject invoke() {
	    TransferObject to = updateCollectorPaymentsHistory(getEffectiveDate());
	    if (to.isFailed()){
	      return to;
	    }
		return updateCollectorPaymentsMTDHistory(getEffectiveDate());
	}

	protected String getBatchDocumentation() {
		return "Batch program to store collector payment statistics";
	}

	protected String getBatchSummary() {
		return "To query and store the daily collector payments statistics into  table PTRCOLLECTOR_PAYMENTS_HISTORY and PTRCOLLECTOR_PAYMENTS_MTD_HISTORY.  Recommended to be run daily morning after payments are loaded and before work list generation Frequency - Daily";
	}

}
