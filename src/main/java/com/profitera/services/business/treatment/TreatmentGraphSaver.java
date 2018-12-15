package com.profitera.services.business.treatment;

import com.profitera.dataaccess.ITransaction;
import com.profitera.descriptor.business.treatment.admin.TreatmentGraph;
import com.profitera.descriptor.db.reference.TreatmentStageRef;
import com.profitera.services.system.dataaccess.IReadWriteDataProvider;

/**
 * 
 *
 * @author jamison
 */
public interface TreatmentGraphSaver {
	public void saveTreatmentGraph(TreatmentGraph graph, TreatmentStageRef stage, IReadWriteDataProvider p, ITransaction t);
}
