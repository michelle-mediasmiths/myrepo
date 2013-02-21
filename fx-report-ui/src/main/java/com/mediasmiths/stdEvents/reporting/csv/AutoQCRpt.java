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
			final String[] header = {"dateRange", "title", "materialID", "channels", "contentType", "operator", "taskStatus", "qcStatus", "taskStart", "taskFinish", "warningTime", "manualOverride", "failureParameter", "titleLength"};
			final CellProcessor[] processors = getAutoQCProcessor();
			beanWriter.writeHeader(header);
			
			AutoQC totalPass = new AutoQC("Total OC'd", Integer.toString(autoQcs.size()));
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
			if (payload.contains("materialID"))
				autoQc.setMaterialID(payload.substring(payload.indexOf("materialID") +11, payload.indexOf("</materialID")));
			if (payload.contains("title"))
				autoQc.setTitle(payload.substring(payload.indexOf("title")+6, payload.indexOf("</title")));
			if (payload.contains("channels")) 
				autoQc.setChannels(payload.substring(payload.indexOf("channels")+9, payload.indexOf("</channels")));
			if (payload.contains("contentType"))
				autoQc.setContentType(payload.substring(payload.indexOf("contentType")+12, payload.indexOf("</contentType")));
			if (payload.contains("operator"))
				autoQc.setOperator(payload.substring(payload.indexOf("operator")+9, payload.indexOf("</operator")));
			if (payload.contains("taskStatus"))
				autoQc.setTaskStatus(payload.substring(payload.indexOf("taskStatus")+11, payload.indexOf("</taskStatus")));
			if (payload.contains("qcStatus"))
				autoQc.setQcStatus(payload.substring(payload.indexOf("qcStatus")+9, payload.indexOf("</qcStatus")));
			autoQc.setTaskStart(new Date(event.getTime()).toString());
			if (payload.contains("taskFinish"))
				autoQc.setTaskFinish(payload.substring(payload.indexOf("taskFinish")+11, payload.indexOf("</taskFinish")));
			if (payload.contains("warningTime"))
				autoQc.setWarningTime(payload.substring(payload.indexOf("warningTime")+12, payload.indexOf("</warningTime")));
			if ((autoQc.getQcStatus() != null) && (autoQc.getQcStatus().contains("Overridden")))
				autoQc.setManualOverride("1");
			if (payload.contains("failureParameter"))
				autoQc.setFailureParameter(payload.substring(payload.indexOf("failureParameter")+17, payload.indexOf("</failureParameter")));
			if (payload.contains("titleLength"))
				autoQc.setTitleLength(payload.substring(payload.indexOf("titleLength")+12, payload.indexOf("</titleLength")));

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
				new Optional(),
				new Optional(),
				new Optional()
		};
		return processors;
	}
	
//	if (qcStatus.equals("QCPass"))
//	autoQc.setTaskStatus("Completed");
//else if (qcStatus.equals("QCNotDone"))
//	autoQc.setTaskStatus("Processing");
//else if (qcStatus.equals("QCFail"))
//	autoQc.setTaskStatus("Completed/ Failed");
//else if (qcStatus.equals("QCFail(Overridden)"))
//	autoQc.setTaskStatus("Completed/Failed");

//}
//
//autoQc.setTaskStart(new Date(event.getTime()).toString());
//
//List<EventEntity> channelEvents = queryApi.getByEventName("CreateOrUpdateTitle");
//for (EventEntity channelEvent : channelEvents)
//{
//String str = channelEvent.getPayload();
//String channelTitle = str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9)));
//if (payload.contains("AssetID")) {
//	String curTitle =  payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID"));
//	if (curTitle.equals(channelTitle)) {
//		if (str.contains("channelName"))
//			autoQc.setChannels(str.substring(str.indexOf("channelName")+13, str.indexOf('"',(str.indexOf("channelName")+13)))); 
//	}
//}
//}
//
//List<EventEntity> lengthEvents = queryApi.getByNamespace("http://www.foxtel.com.au/ip/content");
//for (EventEntity lengthEvent : lengthEvents)
//{
//String str = lengthEvent.getPayload();
//String lengthTitle = str.substring(str.indexOf("materialId") +11, str.indexOf("</materialId"));
//if (payload.contains("AssetID")) {
//	String curTitle = payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID"));
//	if (curTitle.equals(lengthTitle)) {
//		if (str.contains("Duration")) 
//			autoQc.setTitleLength(str.substring(str.indexOf("Duration") +9, str.indexOf("</Duration")));
//		if (lengthEvent.getEventName().equals("UnmatchedContentAvailable"))
//			autoQc.setContentType("Unmatched");
//		else
//			autoQc.setContentType("Programme");
//	}
//}
//}
	
	
}
