package com.profitera.services.system.report;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReportOutputGeneratorFactory {
	
	public final static String REPORT_OUTPUT_GENERATOR = "reportOutput.reportOutputGenerator";

	public final static String FILENET = "FILENET";
	public final static String NULL = "NULL";
	
	private final Map generators = new HashMap();

	private static ReportOutputGeneratorFactory factory = null;
	private static final Log log = LogFactory.getLog(ReportOutputGeneratorFactory.class);
	
	private ReportOutputGeneratorFactory() throws ClassNotFoundException, Exception {
		registerReportOutputGenerator("FILENET", "com.profitera.services.system.report.FileNetReportOutputGenerator");
		registerReportOutputGenerator("NULL", "com.profitera.services.system.report.NullReportOutputGenerator");
	};
	
	public static ReportOutputGeneratorFactory getInstance(){
		if (factory == null)
			try {
				factory = new ReportOutputGeneratorFactory();
			} catch (ClassNotFoundException e) {
				log.error("Unable to instantiate ReportOutputGeneratorFactory", e);
			} catch (Exception e) {
				log.error("Unable to instantiate ReportOutputGeneratorFactory", e);
			}
		return factory;
	}
	
	public IReportOutputGenerator getReportOutputGenerator(String generatorName){
		if (generatorName == null || generatorName.trim().length() == 0)
			return (IReportOutputGenerator)generators.get("NULL");
		if (generators.containsKey(generatorName))
			return (IReportOutputGenerator)generators.get(generatorName);
		throw new RuntimeException("Report generator " + generatorName + " is not registered.");
	}

	public void registerReportOutputGenerator(String name, String className) throws ClassNotFoundException, Exception{
		Class clazz = Class.forName(className);
		registerReportOutputGenerator(name, clazz);
	}
	
	public void registerReportOutputGenerator(String name, Class clazz) throws Exception{
		try {
			Object o = clazz.newInstance();
			if (! (o instanceof IReportOutputGenerator))
				throw new Exception("Report output generator " + clazz.getName() + " is not an instance of " + IReportGenerationService.class.getName());
			generators.put(name, o);
		} catch (InstantiationException e) {
			throw new Exception("Unable to instantiate " + clazz.getName() + " for registering report output generator " + name, e);
		} catch (IllegalAccessException e) {
			throw new Exception("Illegal access exception while instantiating " + clazz.getName() + " for registering report output generator " + name, e);
		}
	}
}