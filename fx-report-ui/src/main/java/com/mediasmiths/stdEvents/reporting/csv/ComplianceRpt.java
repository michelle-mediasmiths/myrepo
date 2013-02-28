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
import com.mediasmiths.stdEvents.report.entity.ComplianceLoggingRT;

public class ComplianceRpt
{
	public static final transient Logger logger = Logger.getLogger(ComplianceRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeCompliance(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<ComplianceLoggingRT> comps = getComplianceList(events, startDate, endDate);
		
		ICsvBeanWriter beanWriter = null;
		try{
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "complianceEditCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "title", "materialID", "channel", "taskStatus", "taskStart", "taskFinish", "externalCompliance"};
			final CellProcessor[] processors = getComplianceProcessor();
			beanWriter.writeHeader(header);
			
			ComplianceLoggingRT total = new ComplianceLoggingRT("Number of Titles", null);
			ComplianceLoggingRT time = new ComplianceLoggingRT("Average Completion Time", null);
			comps.add(total);
			comps.add(time);
			
			for (ComplianceLoggingRT comp : comps)
			{
				beanWriter.write(comp, header, processors);
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
	
	public List<ComplianceLoggingRT> getComplianceList(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<ComplianceLoggingRT> comps = new ArrayList<ComplianceLoggingRT>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			ComplianceLoggingRT comp = new ComplianceLoggingRT();
			
			comp.setDateRange(startDate + " - " + endDate);
			if (payload.contains("title"))
				comp.setTitle(payload.substring(payload.indexOf("title")+6, payload.indexOf("</title")));
			if (payload.contains("materialID"))
				comp.setMaterialID(payload.substring(payload.indexOf("materialID")+11, payload.indexOf("</materialID")));
			if (payload.contains("channel"))
				comp.setChannel(payload.substring(payload.indexOf("channel")+8, payload.indexOf("</channel")));
			if (payload.contains("taskStatus"))
				comp.setTaskStatus(payload.substring(payload.indexOf("taskStatus")+11, payload.indexOf("</taskStatus")));
			if (payload.contains("taskStart"))
				comp.setTaskStart(payload.substring(payload.indexOf("taskStart")+10, payload.indexOf("</taskStart")));
			if (payload.contains("taskFinish"))
				comp.setTaskFinish(payload.substring(payload.indexOf("taskFinish")+11, payload.indexOf("</taskFinish")));
			if (payload.contains("externalCompliance"))
				comp.setExternalCompliance(payload.substring(payload.indexOf("externalCompliance")+19, payload.indexOf("</externalCompliance")));
			
			comps.add(comp);
		}
		return comps;
	}
	
	public CellProcessor[] getComplianceProcessor()
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
}
