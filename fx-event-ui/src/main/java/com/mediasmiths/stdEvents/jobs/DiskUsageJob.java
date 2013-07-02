package com.mediasmiths.stdEvents.jobs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ITokenizer;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.Util;

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
		        
		        
		        
		        ICsvListReader listReader = new CsvListReader(new BufferedReader(new InputStreamReader(new URL(filename).openStream())), CsvPreference.STANDARD_PREFERENCE);
	            List<String> row = null;
	            
	            //Discard first 2 rows
	            listReader.read();
	            listReader.read();
	            
	            while ((row = listReader.read()) != null) {

	                if (listReader.length() != header.length) {
	                    // skip row with invalid number of columns
	                	logger.info("Skipping invalid row, length : " + listReader.length()  + ", header length : " + header.length);
	                	continue;
	                }

	                // safe to create map now
	                Map<String, String> rowMap = new HashMap<String, String>();
	                Util.filterListToMap(rowMap, header, row);
	                logger.info("Row map : " + rowMap);
	                
	                DiskUsageEvent diskUsageEvent = new DiskUsageEvent();
	                diskUsageEvent.setChannel(rowMap.get(header[0]));
	                diskUsageEvent.setHrSize(rowMap.get(header[1]));
	                diskUsageEvent.setTsmSize(rowMap.get(header[2]));
	                diskUsageEvent.setLrSize(rowMap.get(header[3]));
	                diskUsageEvent.setOthersSize(rowMap.get(header[4]));
	                diskUsageEvent.setTotalSize(rowMap.get(header[5]));
	                events.saveEvent(SYSTEM_NAMESPACE, EventNames.DISK_USAGE_EVENT, diskUsageEvent);

	            }
	            listReader.close();
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
