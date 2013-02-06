package com.mediasmiths.stdEvents.reporting.csv;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

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
import com.mediasmiths.stdEvents.report.entity.TranscoderLoad;

public class TranscoderLoadRpt
{
	public static final transient Logger logger = Logger.getLogger(TranscoderLoadRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeTranscoderLoad(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<TranscoderLoad> transcodes = getTranscoderList(events, startDate, endDate);
		
		ICsvBeanWriter beanWriter = null;
		try {
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "transcoderLoadCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "mediaID", "transcodeFinish", "duration", "sourceFormat", "result"};
			final CellProcessor[] processors = getTranscoderProcessor();
			beanWriter.writeHeader(header);
			
			TranscoderLoad submitted = new TranscoderLoad("Transcodes Submitted", Integer.toString(queryApi.getByNamespace("http://www.foxtel.com.au/ip/tc").size()));
			TranscoderLoad passed = new TranscoderLoad("Transcodes Passed", Integer.toString(queryApi.getByEventName("Transcoded").size()));
			TranscoderLoad failed = new TranscoderLoad("Transcodes Failed", Integer.toString(queryApi.getByEventName("TCFailed").size()));
			transcodes.add(submitted);
			transcodes.add(passed);
			transcodes.add(failed);
			
			for (TranscoderLoad tc : transcodes)
			{
				beanWriter.write(tc, header, processors);
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
	
	public List<TranscoderLoad> getTranscoderList(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<TranscoderLoad> transcoders = new ArrayList<TranscoderLoad>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			TranscoderLoad tc = new TranscoderLoad();
			tc.setDateRange(startDate + " - " + endDate);
			if (payload.contains("PackageID"))
				tc.setMediaID(payload.substring(payload.indexOf("PackageID")+10, payload.indexOf("</PackageID")));
			tc.setTranscodeFinish(new Date(event.getTime()).toString());
			
			List<EventEntity> materials = queryApi.getByNamespace("http://www.foxtel.com.au/ip/content");
			for (EventEntity material : materials)
			{
				String str = material.getPayload();
				String matTitle = str.substring(str.indexOf("materialId") +11, str.indexOf("</materialId"));
				if (payload.contains("PackageID")) {
					String curTitle = payload.substring(payload.indexOf("PackageID")+10, payload.indexOf("</PackageID"));
					if (curTitle.equals(matTitle)) {
						if (str.contains("Duration"))
							tc.setDuration(str.substring(str.indexOf("Duration") +9, str.indexOf("</Duration")));
						if (str.contains("Format"))
							tc.setSourceFormat(str.substring(str.indexOf("Format")+7, str.indexOf("</Format")));
					}
				}	
			}
			tc.setResult(event.getEventName());
			transcoders.add(tc);	
		}
		return transcoders;
	}
	
	public CellProcessor[] getTranscoderProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] {
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
