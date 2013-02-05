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
import com.mediasmiths.stdEvents.report.entity.ManualQA;

public class ManualQARpt
{
	public static final transient Logger logger = Logger.getLogger(CsvImpl.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeManualQA(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<ManualQA> manualQAs = getManualQAList(events, startDate, endDate);

		ICsvBeanWriter beanWriter = null;
		try {
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "manualQAcsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "title", "assetID", "operator", "aggregatorID", "qaStatus", "duration", "channel"};
			final CellProcessor[] processors = getManualQAProcessor();
			beanWriter.writeHeader(header);

			ManualQA failed = new ManualQA("Total failed", Integer.toString(queryApi.getLength(queryApi.getTotalFailedQA())));
			ManualQA passed = new ManualQA("Total QA'd", null);
			ManualQA noEscalated = new ManualQA("Number Escalated", null);
			ManualQA avEscalated = new ManualQA("Average Time Escalated", null);
			ManualQA noReordered = new ManualQA ("Number Reordered", null);
			ManualQA requiring = new ManualQA("Requiring Fix/ Stitch", null);
			manualQAs.add(failed);
			manualQAs.add(passed);
			manualQAs.add(noEscalated);
			manualQAs.add(avEscalated);
			manualQAs.add(noReordered);
			manualQAs.add(requiring);

			for (ManualQA manualQA : manualQAs) 
			{
				beanWriter.write(manualQA, header, processors);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
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
	
	public List<ManualQA> getManualQAList(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<ManualQA> manualQAList = new ArrayList<ManualQA>();
		for (EventEntity event : events)
		{
			ManualQA manualQA = new ManualQA();
			manualQA.setDateRange(startDate.toString() + " - " + endDate.toString());
			String payload = event.getPayload();
			logger.info(payload);
			if (payload.contains("AssetID"))
				manualQA.setAssetID(payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID")));
			if (payload.contains("Title"))
				manualQA.setTitle(payload.substring(payload.indexOf("Title")+6, payload.indexOf("</Title")));
			
			List<EventEntity> aggregatorEvents = queryApi.getByEventName("AddOrUpdateMaterial");
			for (EventEntity aggregatorEvent : aggregatorEvents)
			{
				String str = aggregatorEvent.getPayload();
				String aggregatorTitle = str.substring(str.indexOf("materialID")+12, str.indexOf('"', (str.indexOf("materialID")+12)));
				if (payload.contains("AssetID")) {
					String curTitle =  payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID"));
					if (curTitle.equals(aggregatorTitle))
						manualQA.setAggregatorID(str.substring(str.indexOf("aggregatorID")+14, str.indexOf('"',(str.indexOf("aggregatorID")+14))));
				}
			}
			
			if (payload.contains("QCStatus")) 
				manualQA.setQaStatus(payload.substring(payload.indexOf("QCStatus")+9, payload.indexOf("</QCStatus")));
			
			List<EventEntity> lengthEvents = queryApi.getByNamespace("http://www.foxtel.com.au/ip/content");
			for (EventEntity lengthEvent : lengthEvents)
			{
				String str = lengthEvent.getPayload();
				String lengthTitle = str.substring(str.indexOf("materialId") +11, str.indexOf("</materialId"));
				logger.info("Length: " + lengthTitle);
				if (payload.contains("AssetID")) {
					String curTitle = payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID"));
					logger.info("Current: " + curTitle);
					if (curTitle.equals(lengthTitle)) {
						if (str.contains("Duration"))
							manualQA.setDuration(str.substring(str.indexOf("Duration") +9, str.indexOf("</Duration")));
					}
				}
			}
			
			List<EventEntity> creates = queryApi.getByEventName("CreateOrUpdateTitle");
			for (EventEntity create : creates)
			{
				String str = create.getPayload();
				if (str.contains("titleID")) {
					String title = str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9)));
					logger.info("titleID: " + title);
					if (str.contains("channelName")) {
						String curTitle = payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID"));
						logger.info("AssetID: " + curTitle);
						if (curTitle.equals(title))
							manualQA.setChannel(str.substring(str.indexOf("channelName")+13, str.indexOf('"',(str.indexOf("channelName")+13))));
					}	
				}
			}
			
			manualQAList.add(manualQA);
		}
		return manualQAList;
	}
	
	public CellProcessor[] getManualQAProcessor()
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
