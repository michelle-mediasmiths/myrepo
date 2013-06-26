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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiskUsageRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(DiskUsageRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	@Named("diskUsageLoc")
	public String DISK_USAGE_LOC;
	
	@Inject
	private QueryAPIImpl queryApi;
	
	public void writeDiskUsage(DateTime startDate, DateTime endDate, String reportName)
	{
		List<DiskUsageBean> reports = getReportList(startDate, endDate);
		
		createCsv(reports, reportName);	
	}
	
	public List<DiskUsageBean> getReportList(DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>getReportList");
		List<DiskUsageBean> reports = new ArrayList <DiskUsageBean> ();
		for (DateTime dateToFetch = startDate; dateToFetch.isBefore(endDate); dateToFetch.plusDays(1))
		{
			DiskUsageBean report = readDiskUsageBeans(dateToFetch);
			if (report != null)
			{
				reports.add(report);
			}
			else {
				logger.warn("No disk usage report used for date " + dateToFetch.toString());
			}
		}
		logger.debug("<<<getReportList");
		return reports;
	}
	
	private void createCsv (List<DiskUsageBean> enteries, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try { 
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "channel", "hrSize", "tsmSize", "lrSize", "othersSize", "totalSize"};
			final CellProcessor[] processors = getProcessors();
			beanWriter.writeHeader(header);
			
			for (DiskUsageBean entry : enteries) {
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
