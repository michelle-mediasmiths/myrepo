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
import com.mediasmiths.stdEvents.report.entity.ExportRT;

public class ExportRpt
{
	public static final transient Logger logger = Logger.getLogger(ExportRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeExport(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<ExportRT> exports = getExportList(events, startDate , endDate);
		
		ICsvBeanWriter beanWriter = null;
		
		try{
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "extendedPublishingAndFileExportCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "title", "materialID", "channel", "taskStatus", "exportType", "duration"};
			final CellProcessor[] processors = getExportProcessor();
			beanWriter.writeHeader(header);
			
			ExportRT compliance = new ExportRT("Number of Titles Exported Compliance", null);
			ExportRT captioning = new ExportRT("Number of Titles Exported Captioning", null);
			ExportRT publicity = new ExportRT("Number of Titles Exported Publicity", null);
			exports.add(compliance);
			exports.add(captioning);
			exports.add(publicity);
			
			for (ExportRT export : exports)
			{
				beanWriter.write(export, header, processors);
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
	
	public List<ExportRT> getExportList(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<ExportRT> exports = new ArrayList<ExportRT>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			ExportRT export = new ExportRT();
			export.setDateRange(startDate + " - " + endDate);

			
			exports.add(export);
		}		
		return exports;
	}
	
	public CellProcessor[] getExportProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] {
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

