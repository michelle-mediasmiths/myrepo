package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.FilePickupDetails;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WatchFolderRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(WatchFolderRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPIImpl queryApi;
	
	public void writeWatchFolder(List<EventEntity> events, DateTime startDate, DateTime endDate, String reportName)
	{
		List<FilePickupDetails> files = getReportList(events, startDate, endDate);
		
		createCsv(files, reportName);	
	}
	
	public List<FilePickupDetails> getReportList(List<EventEntity> events, DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>getReportList");
		
		List<FilePickupDetails> files = new ArrayList<FilePickupDetails>();
		
		String startF = startDate.toString(dateFormatter);
		String endF = endDate.toString(dateFormatter);
		
		for (EventEntity event : events) 
		{
			FilePickupDetails file = (FilePickupDetails) unmarshallEvent(event);
			
			file.setDateRange(new StringBuilder().append(startF).append(" - ").append(endF).toString());
			file.setTimeDiscovered(event.getTime());
			
			files.add(file);
		}
		
		logger.debug("<<<getReportList");
		return files;
	}
	
	private void createCsv (List<FilePickupDetails> files, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try { 
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "filename", "timeDiscovered", "timeProcessed", "aggregator"};
			final CellProcessor[] processors = getWatchFolderProcessor();
			beanWriter.writeHeader(header);
			
			for (FilePickupDetails file : files) {
				beanWriter.write(file, header, processors);
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
