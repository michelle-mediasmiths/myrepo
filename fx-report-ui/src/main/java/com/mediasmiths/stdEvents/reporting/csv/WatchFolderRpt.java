package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.WatchFolder;

public class WatchFolderRpt
{
	public static final transient Logger logger = Logger.getLogger(WatchFolderRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeWatchFolder(List<EventEntity> events, Date startDate, Date endDate, String reportName)
	{
		List<WatchFolder> watched = getWatchFolderList(events, startDate, endDate);
		
		ICsvBeanWriter beanWriter = null;
		try { 
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "filename", "timeDiscovered", "timeProcessed", "aggregator"};
			final CellProcessor[] processors = getWatchFolderProcessor();
			beanWriter.writeHeader(header);
			
			WatchFolder average = new WatchFolder("Average Time", null);
			watched.add(average);
			
			for (WatchFolder watch : watched) {
				beanWriter.write(watch, header, processors);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally {
			if (beanWriter != null)
			{
				try
				{
					beanWriter.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public List<WatchFolder> getWatchFolderList(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<WatchFolder> watched = new ArrayList<WatchFolder>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			WatchFolder watch = new WatchFolder();
			
			//GET FIELDS FOR REPORT TYPE
			
			watched.add(watch);
		}
		
		return watched;
	}
	
	public CellProcessor[] getWatchFolderProcessor() 
	{
		final CellProcessor[] processors = new CellProcessor[] {
				new Optional(),
				new Optional(), 
				new Optional(),
				new Optional(),
				new Optional()
		};
		return processors;
	}
}
