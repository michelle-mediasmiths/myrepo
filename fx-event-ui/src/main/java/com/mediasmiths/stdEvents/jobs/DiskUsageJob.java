package com.mediasmiths.stdEvents.jobs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.exception.SuperCsvConstraintViolationException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.DiskUsageEvent;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.foxtel.ip.event.EventService;

public class DiskUsageJob implements Job 
{
	private static String diskUsageLoc;
	private static EventService events;
	
	private static final transient Logger logger = Logger.getLogger(DiskUsageJob.class);
	private static final String SYSTEM_NAMESPACE = "http://www.foxtel.com.au/ip/system";
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{
	    ICsvBeanReader beanReader = null;
	    String filename = diskUsageLoc;
	    DiskUsageEvent diskUsage = null;
	    try{
		    try {
		    	logger.info("Reading Disk Usage CSV : " + filename);
		    	BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(filename).openStream()));
		    	beanReader = new CsvBeanReader(reader, CsvPreference.STANDARD_PREFERENCE);
		            
		        // the header elements are used to map the values to the bean (names must match)
		    	beanReader.getHeader(false);
		        final String[] header = {"channel", "hrSize", "tsmSize", "lrSize", "othersSize", "totalSize"};
		        final CellProcessor[] processors = getProcessors();
		        
		        String headerDesc = "";
		        for (String col: header)
		        {
		        	headerDesc +=  col + ", ";
		        }
		        logger.info("CSV header is : " + headerDesc);
		        	        
		        boolean endOfFile = false;
		        while (!endOfFile)
		        {
		        	try
		        	{
				        diskUsage = beanReader.read(DiskUsageEvent.class, header, processors);
				        if (diskUsage != null)
				        {
				        	logger.info(String.format("lineNo=%s, rowNo=%s, customer=%s", beanReader.getLineNumber(), beanReader.getRowNumber(), diskUsage));
				        	events.saveEvent(SYSTEM_NAMESPACE, EventNames.DISK_USAGE_EVENT, diskUsage);
				        }
				        else {
				        	logger.warn("Null value read on line: " + beanReader.getRowNumber());
				        }
			        } 
		        	catch (SuperCsvConstraintViolationException ex) {
			            logger.warn("NON CORRECT VALUE ENCOUNTERD ON ROW "+beanReader.getRowNumber(), ex);
			            endOfFile = false;
			       } catch (SuperCsvCellProcessorException ex){
			           logger.warn("PARSER EXCEPTION ON ROW "+beanReader.getRowNumber(), ex);
			           endOfFile = false;
			       } catch (org.supercsv.exception.SuperCsvException ex){
			            logger.warn("ERROR ON ROW "+beanReader.getRowNumber(), ex);
			            endOfFile = true;
			       }
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
	}
	
	public static void setEvents(EventService events)
	{
		DiskUsageJob.events = events;
	}
	
	public static void setLocation(String location)
	{
		DiskUsageJob.diskUsageLoc = location;
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
