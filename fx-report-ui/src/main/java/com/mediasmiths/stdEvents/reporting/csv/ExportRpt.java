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
import com.mediasmiths.stdEvents.report.entity.Export;

public class ExportRpt
{
	public static final transient Logger logger = Logger.getLogger(ExportRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeExport(List<EventEntity> events)
	{
		List<Export> exports = getExportList(events);
		
		ICsvBeanWriter beanWriter = null;
		
		try{
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "extendedPublishingAndFileExportCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "title", "materialID", "channel", "taskStatus", "exportType", "duration"};
			final CellProcessor[] processors = getExportProcessor();
			beanWriter.writeHeader(header);
			
			Export compliance = new Export("Number of Titles Exported Compliance", null);
			Export captioning = new Export("Number of Titles Exported Captioning", null);
			Export publicity = new Export("Number of Titles Exported Publicity", null);
			exports.add(compliance);
			exports.add(captioning);
			exports.add(publicity);
			
			for (Export export : exports)
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
	
	public List<Export> getExportList(List<EventEntity> events)
	{
		List<Export> exports = new ArrayList<Export>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			Export export = new Export();
			//GET FIELDS FOR REPORT TYPE
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

