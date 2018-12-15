package com.profitera.services.system.report;

import java.io.IOException;
import java.io.InputStream;

public interface IReportOutputGenerator {
	
	public final static String OUTPUT_PATH = "reportOutput.outputPath";
	public final static String BASE_FILE_NAME = "reportOutput.baseFileName";
	public final static String FILE_NAME_PREFIX = "reportOutput.fileNamePrefix";
	public final static String FILE_NAME_SUFFIX = "reportOutput.fileNameSuffix";
	public final static String FILE_NAME_DATE_FORMAT = "reportOutput.fileNameDateFormat";
	
	public final static String ID = "ID";
	public final static String NAME = "NAME";
	public final static String DATE = "DATE";
	
	public final static String DEFAULT_DATE_FORMAT = "yyyyMMddhhmmss";
	
	public void outputReport(Long id, String name, InputStream fis) throws IOException;
}
