/**
 * To query and store the collector receivables daily statistics into
 * table PTRCOLLECTOR_DAILY_ACTIVITIES.
 * Recommended to be run daily at day end
 * Frequency - Daily
 */
package com.profitera.services.business.batch;

import java.sql.SQLException;
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
public class CollectorDailyActivitiesBatch extends AbstractProviderBatch {
	private static String GET_COLLECTOR_DAILY_ACTIVITIES_BY_USER = "getCollectorDailyActivitiesByUser";
	private static String INSERT_COLLECTOR_DAILY_ACTIVITIES = "insertCollectorDailyActivities";
	private static final Log log = LogFactory.getLog(CollectorDailyActivitiesBatch.class);

	private TransferObject updateCollectorDailyActivitiesHistory(Date effectiveDate){
		final Date effDate = effectiveDate;
		log.info("Collector Daily Activities Update for day " + effDate);
		Map args = new HashMap();
		args.put("TODAY_DATE", effDate);
		
		ListQueryServiceIntf lqs = getListQueryService();
		TransferObject to = lqs.getQueryList(GET_COLLECTOR_DAILY_ACTIVITIES_BY_USER, args);
		if (to.isFailed())
			return to;
		
		final List res = (List) to.getBeanHolder();
		if (res != null && res.size() > 0){
			// save into table PTRCOLLECTOR_DAILY_ACTIVITIES 
			final IReadWriteDataProvider p = getReadWriteProvider();
	        try {
	        	p.execute(new IRunnableTransaction(){
	        		public void execute(ITransaction t) throws SQLException, AbortTransactionException {
	        			for (Iterator iter = res.iterator(); iter.hasNext();) {
	        				Map m = (Map)iter.next();
	        				m.put("DATA_DATE", effDate);
	        				p.insert(INSERT_COLLECTOR_DAILY_ACTIVITIES, m, t);
	        			}
	        		}});
	        } catch (SQLException e) {
	        	log.fatal("Failed to execute query: " + INSERT_COLLECTOR_DAILY_ACTIVITIES, e);
	        	return new TransferObject(TransferObject.ERROR, "QUERY_FAILED - " + INSERT_COLLECTOR_DAILY_ACTIVITIES);
	        } catch (AbortTransactionException e) {
	        	log.fatal("Failed to execute query: " + INSERT_COLLECTOR_DAILY_ACTIVITIES, e);
	        	return new TransferObject(TransferObject.ERROR, "QUERY_FAILED - " + INSERT_COLLECTOR_DAILY_ACTIVITIES);
	        }
		}
		return new TransferObject();
	}

	/* (non-Javadoc)
	 * @see com.profitera.services.business.batch.IBatchProcess#invoke(java.lang.String, java.util.Date, java.util.Map)
	 */
	public TransferObject invoke() {
		return updateCollectorDailyActivitiesHistory(getEffectiveDate());
	}

	protected String getBatchDocumentation() {
		return "Batch process to query and store the collector receivables daily statistics";
	}

	protected String getBatchSummary() {
		return "Batch process to query and store the collector receivables daily statistics into table PTRCOLLECTOR_DAILY_ACTIVITIES. - Recommended to be run daily at day end. - Frequency: Daily";
	}
}
