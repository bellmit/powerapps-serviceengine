package com.profitera.rpm.treatment;

import java.util.Date;

import com.profitera.descriptor.business.treatment.admin.*;
import com.profitera.descriptor.rpm.Treatable;

/**
 * 
 *
 * @author jamison
 */
public interface TreatmentPlanner {
	public abstract void treat(Treatable t, TreatmentGraph actionGraph, TreatmentProducer p, Date date);
}