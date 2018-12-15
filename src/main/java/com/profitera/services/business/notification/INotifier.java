/*
 * Created on Jan 4, 2005
 */
package com.profitera.services.business.notification;

import com.profitera.descriptor.db.treatment.TreatmentProcess;

/**
 * @author cyprodevil
 */
public interface INotifier {

	public static final String CONTENT_DATE_FORMAT = "_CONTENT_DATE_FORMAT";
	public static final String SENDER_CLASS = "_SENDER_CLASS";
  public static final String BATCH_SIZE = "_BATCH_SIZE";

	/**
	 * @param treatment process to be notified about
	 * @return treatment processes that failed to be notified about
	 * @throws NotificationFailure if unexpected problem occured causing all process to fail
	 */
	public TreatmentProcess[] notify(TreatmentProcess[] processes) throws NotificationFailure;
	public TreatmentProcess[] prepareForDeparture(TreatmentProcess[] processes);
	public String getNotifierCode();
	public void setNotifierCode(String code);
	public void setPropertyProvider(INotifierPropertyProvider pp);
    public int getBatchSize();
}
