package com.mediasmiths.stdEvents.jobs;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DiskUsageJob implements Job 
{
	@Inject
	@Named("diskUsageLoc")
	public String DISK_USAGE_LOC;
	
	private static final transient Logger logger = Logger.getLogger(DiskUsageJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{
		    ICsvBeanReader beanReader = null;
		    String filename = DISK_USAGE_LOC;
		    DiskUsageBean diskUsage = null;
		    try{
			    try {
			    	logger.info("Reading Disk Usage CSV : " + filename);
			    	beanReader = new CsvBeanReader(new FileReader(filename), CsvPreference.STANDARD_PREFERENCE);
			            
			        // the header elements are used to map the values to the bean (names must match)
			        final String[] header = beanReader.getHeader(true);
			        final CellProcessor[] processors = getProcessors();
			            
			        while( (diskUsage = beanReader.read(DiskUsageBean.class, header, processors)) != null ) {
			        	System.out.println(String.format("lineNo=%s, rowNo=%s, customer=%s", beanReader.getLineNumber(), beanReader.getRowNumber(), diskUsage));
			        }
			            
			    }
			    catch(FileNotFoundException e)
			    {
			    	logger.warn("File Not Found Exception while attempting to read " + filename);
			    }
			    finally {
			    	if( beanReader != null ) {
			    		beanReader.close();
			        }
			    }
			}
		    catch(IOException ioe)
		    {
		    	logger.warn("File IO Exception while attempting to read " + filename);
		    }
		    //return diskUsage;
	}
	
	private static CellProcessor[] getProcessors() 
	{  
		final CellProcessor[] processors = new CellProcessor[] { 
            new NotNull(), // channel
            new NotNull(), // HR Size
            new NotNull(), // TSM Size
            new NotNull(), // LR Size
            new NotNull(), // Others Size
            new NotNull(), // Total Size
	    };
	        
		return processors;
	}

}
