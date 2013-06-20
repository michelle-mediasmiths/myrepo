package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.ComplianceLoggingMarker;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
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

public class ComplianceRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(ComplianceRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPIImpl queryApi;
	
	public void writeCompliance(List<EventEntity> events, DateTime startDate, DateTime endDate, String reportName)
	{
		List<ComplianceLoggingMarker> comps = getReportList(events, startDate, endDate);
		
		int total = 0;
		
		for (ComplianceLoggingMarker clm : comps)
		{
			total ++;
		}
		
		comps.add(addStats("No of Titles", Integer.toString(total)));
		
		createCsv(comps, reportName);
	}
	
	public List<ComplianceLoggingMarker> getReportList(List<EventEntity> events, DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>getReportList");
		
		List<ComplianceLoggingMarker> clms = new ArrayList<ComplianceLoggingMarker>();
				
		String startF = startDate.toString(dateFormatter);
		String endF = endDate.toString(dateFormatter);
		
		for (EventEntity event : events) 
		{
			ComplianceLoggingMarker clm = (ComplianceLoggingMarker) unmarshallEvent(event);
			
			clm.setDateRange(new StringBuilder().append(startF).append(" - ").append(endF).toString());
			
			clm.setMaterialId(clm.getTitleField());
			clm.setTaskStart(new DateTime(event.getTime()).toString());
			
			Title title = queryApi.getTitleById(clm.getMaterialId());
			if (title != null)
			{
				logger.info("Title found: " + title.getTitleId());
				clm.setTitle(title.getTitle());
				clm.setChannels(title.getChannels().toString());
			}
			clms.add(clm);
		}
		
		logger.debug("<<<getReportList");
		return clms;
	}
	
	private void createCsv(List<ComplianceLoggingMarker> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "title", "materialId", "channels", "taskStatus", "taskStart", "taskFinish", "externalCompliance"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (ComplianceLoggingMarker title : titles)
			{
				beanWriter.write(title, header, processors);
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
	
	private CellProcessor[] getProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] {
				new Optional(),
				new Optional(), 
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional()
		};
		return processors;
	}
	
	private ComplianceLoggingMarker addStats(String name, String value)
	{
		ComplianceLoggingMarker clm = new ComplianceLoggingMarker();
		clm.setTitle(name);
		clm.setMaterialId(value);
		return clm;
	}
}
