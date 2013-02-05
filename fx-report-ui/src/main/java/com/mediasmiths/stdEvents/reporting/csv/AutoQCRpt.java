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
import com.mediasmiths.stdEvents.report.entity.AutoQC;

public class AutoQCRpt
{
	public static final transient Logger logger = Logger.getLogger(CsvImpl.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeAutoQc(List<EventEntity> passed, Date startDate, Date endDate)
	{
		List<AutoQC> autoQcs = getQCList(passed, startDate, endDate);
		ICsvBeanWriter beanWriter = null;
		try {
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "autoQcCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "title", "materialID", "channels", "contentType", "operator", "taskStatus", "qcStatus", "taskStart", "manualOverride", "failureParameter", "titleLength"};
			final CellProcessor[] processors = getAutoQCProcessor();
			beanWriter.writeHeader(header);
			
			AutoQC totalPass = new AutoQC("Total OC'd", Integer.toString(queryApi.getLength(queryApi.getTotalQCd())));
			AutoQC failed = new AutoQC("Failed QC", Integer.toString(queryApi.getLength(queryApi.getFailedQc())));
			AutoQC overridden = new AutoQC("Operator Overridden", Integer.toString(queryApi.getLength(queryApi.getOperatorOverridden())));
			autoQcs.add(totalPass);
			autoQcs.add(failed);
			autoQcs.add(overridden);
			
			AutoQC avConc = new AutoQC("Average Concurrant Titles", null);
			AutoQC maxConc = new AutoQC("Max Concurrant Titles", null);
			autoQcs.add(avConc);
			autoQcs.add(maxConc);
			
			for (AutoQC autoQc : autoQcs)
			{
				beanWriter.write(autoQc, header, processors);
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
	
	public List<AutoQC> getQCList(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<AutoQC> titleList = new ArrayList<AutoQC>();
		for (EventEntity event : events)
		{
			AutoQC autoQc = new AutoQC();
			autoQc.setDateRange(startDate.toString() + " - " + endDate.toString());
			String payload = event.getPayload();
			if (payload.contains("AssetID"))
				autoQc.setMaterialID(payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID")));
			if (payload.contains("Title"))
				autoQc.setTitle(payload.substring(payload.indexOf("Title")+6, payload.indexOf("</Title")));
			if (payload.contains("QCStatus")) {
				String qcStatus = payload.substring(payload.indexOf("QCStatus")+9, payload.indexOf("</QCStatus"));
				autoQc.setQcStatus(qcStatus);
				
				if (qcStatus.equals("QCPass"))
					autoQc.setTaskStatus("Completed");
				else if (qcStatus.equals("QCNotDone"))
					autoQc.setTaskStatus("Processing");
				else if (qcStatus.equals("QCFail"))
					autoQc.setTaskStatus("Completed/ Failed");
				else if (qcStatus.equals("QCFail(Overridden)"))
					autoQc.setTaskStatus("Completed/Failed");
				
			}
			
			autoQc.setTaskStart(new Date(event.getTime()).toString());
			
			List<EventEntity> channelEvents = queryApi.getByEventName("CreateOrUpdateTitle");
			for (EventEntity channelEvent : channelEvents)
			{
				String str = channelEvent.getPayload();
				String channelTitle = str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9)));
				if (payload.contains("AssetID")) {
					String curTitle =  payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID"));
					if (curTitle.equals(channelTitle)) {
						if (str.contains("channelName"))
							autoQc.setChannels(str.substring(str.indexOf("channelName")+13, str.indexOf('"',(str.indexOf("channelName")+13)))); 
					}
				}
			}
			
			List<EventEntity> lengthEvents = queryApi.getByNamespace("http://www.foxtel.com.au/ip/content");
			for (EventEntity lengthEvent : lengthEvents)
			{
				String str = lengthEvent.getPayload();
				String lengthTitle = str.substring(str.indexOf("materialId") +11, str.indexOf("</materialId"));
				if (payload.contains("AssetID")) {
					String curTitle = payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID"));
					if (curTitle.equals(lengthTitle)) {
						if (str.contains("Duration")) 
							autoQc.setTitleLength(str.substring(str.indexOf("Duration") +9, str.indexOf("</Duration")));
						if (lengthEvent.getEventName().equals("UnmatchedContentAvailable"))
							autoQc.setContentType("Unmatched");
						else
							autoQc.setContentType("Programme");
					}
				}
			}
			titleList.add(autoQc);
		}	
		return titleList;
	}
	
	public CellProcessor[] getAutoQCProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] {
				new Optional(),
				new Optional(), 
				new Optional(),
				new Optional(),
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
