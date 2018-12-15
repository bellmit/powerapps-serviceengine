package com.profitera.rpm;
import java.util.Date;
/**
 * 
 *
 * @author jamison
 */
public interface TreatmentAssigner {
	public abstract void assignTreatments(String startId, String endId, Date d,
			boolean useDelqDetermination, String rootID);
}