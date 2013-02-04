package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import com.mediasmiths.stdEvents.report.entity.Compliance;

public class ComplianceRpt
{
	public static final transient Logger logger = Logger.getLogger(ComplianceRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeCompliance(List<EventEntity> events)
	{
		List<Compliance> comps = getComplianceList(events);
		
		ICsvBeanWriter beanWriter = null;
		try{
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "complianceEditCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "title", "materialID", "channel", "taskStatus", "taskStart", "taskFinish", "externalCompliance"};
			final CellProcessor[] processors = getComplianceProcessor();
			beanWriter.writeHeader(header);
			
			Compliance total = new Compliance("Number of Titles", null);
			Compliance time = new Compliance("Average Completion Time", null);
			comps.add(total);
			comps.add(time);
			
			for (Compliance comp : comps)
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
	
	public List<Compliance> getComplianceList(List<EventEntity> events)
	{
		List<Compliance> comps = new ArrayList<Compliance>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			Compliance comp = new Compliance();
			
			//GET FIELDS FOR REPORT TYPE
			
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
