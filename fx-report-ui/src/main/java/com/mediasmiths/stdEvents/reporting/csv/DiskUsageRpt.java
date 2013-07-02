package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import com.mediasmiths.foxtel.ip.common.events.DiskUsageEvent;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiskUsageRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(DiskUsageRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPIImpl queryApi;
	
	public void writeDiskUsage(List<EventEntity> events, DateTime startDate, DateTime endDate, String reportName)
	{
		List<DiskUsageEvent> reports = getReportList(events, startDate, endDate);		
		createCsv(reports, reportName);	
	}
	
	public List<DiskUsageEvent> getReportList(List<EventEntity> events, DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>getReportList");
		Map<String, DiskUsageEvent> reportMap = new HashMap <String, DiskUsageEvent> ();
		for (EventEntity event: events)
		{
			DiskUsageEvent report = (DiskUsageEvent) unmarshallReport(event);
			
			if (report != null)
			{
				DiskUsageEvent cumulativeUsage = reportMap.get(report.getChannel());
				if (cumulativeUsage == null)
				{
					reportMap.put(report.getChannel(), report);
				}
				else {
					cumulativeUsage.setHrSize(add(cumulativeUsage.getHrSize(), report.getHrSize()));
					cumulativeUsage.setTsmSize(add(cumulativeUsage.getHrSize(), report.getHrSize()));
					cumulativeUsage.setLrSize(add(cumulativeUsage.getHrSize(), report.getHrSize()));
					cumulativeUsage.setOthersSize(add(cumulativeUsage.getHrSize(), report.getHrSize()));
					cumulativeUsage.setTotalSize(add(cumulativeUsage.getHrSize(), report.getHrSize()));
					reportMap.put(report.getChannel(), cumulativeUsage);
				}
			}
			else {
				logger.warn("Null report after unmarshalling event : " + event.toString());
			}
		}
		logger.debug("<<<getReportList");

		return new ArrayList<DiskUsageEvent>(reportMap.values());
	}
	
	private String add(String cumulative, String newSize)
	{
		String x = "10.5GB";
		CharSequence trimmedTotal = cumulative.subSequence(0, cumulative.length() - 2);
		double runningTotal = Double.parseDouble(trimmedTotal.toString());
		
		CharSequence trimmedNew = newSize.subSequence(0, newSize.length() - 2);
		double newValue = Double.parseDouble(trimmedTotal.toString());
		
		if (newSize.substring(newSize.length() - 2).toLowerCase().equals("kb"))
		{
			newValue = newValue / 1024;
		}
		else if (newSize.substring(newSize.length() - 2).toLowerCase().equals("mb"))
		{
			newValue = newValue / (1024 * 1024);
		}
		
		runningTotal += newValue;
		cumulative = runningTotal + "GB";
		
		return cumulative;
	}
	
	private void createCsv (List<DiskUsageEvent> enteries, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try { 
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "channel", "hrSize", "tsmSize", "lrSize", "othersSize", "totalSize"};
			final CellProcessor[] processors = getProcessors();
			beanWriter.writeHeader(header);
			
			for (DiskUsageEvent entry : enteries) {
				beanWriter.write(entry, header, processors);
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
