package com.profitera.services.business.treatment;

import java.util.Map;

import oracle.toplink.sessions.Session;

import com.profitera.descriptor.business.treatment.admin.DefaultTreatmentGraph;
import com.profitera.descriptor.db.reference.TreatmentStageRef;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

/**
 * 
 *
 * @author jamison
 */
public interface TreatmentGraphLoader {
	public DefaultTreatmentGraph loadGraph(Map stage, IReadWriteDataProvider provider);
}
