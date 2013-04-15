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
import com.mediasmiths.foxtel.ip.common.events.ManualQANotification;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.foxtel.ip.common.events.report.ManualQA;
import com.mediasmiths.foxtel.ip.common.events.report.OrderStatus;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.ManualQART;
import com.mediasmiths.stdEvents.reporting.rest.ReportUIImpl;

public class ManualQARpt
{
	public static final transient Logger logger = Logger.getLogger(ManualQARpt.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	private ReportUIImpl report = new ReportUIImpl();
	
	private List<String> channels = new ArrayList<String>();
	
	private int total=0;
	private int escalated=0;
	private int failed=0;
	private int reordered=0;
	private int hr=0;
	
	public void writeManualQA(List<EventEntity> events, Date startDate, Date endDate, String reportName)
	{
		List<ManualQA> manualQAs = getReportList(events, startDate, endDate);
		//setStats(manualQAs);
		
		manualQAs.add(addStats("Total QA'd", Integer.toString(total)));
		manualQAs.add(addStats("Total Escalated", Integer.toString(escalated)));
		manualQAs.add(addStats("Total Failed QA", Integer.toString(failed)));
		manualQAs.add(addStats("Total Needs Reordered", Integer.toString(reordered)));
		manualQAs.add(addStats("Total QA'd HR", Integer.toString(hr)));
		
		createCsv(manualQAs, reportName);
	}
	
	private Object unmarshall(EventEntity event)
	{
		Object title = null;
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);

		try
		{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
			logger.info("Deserialising payload");
			title = JAXB_SERIALISER.deserialise(payload);
			logger.info("Object created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return title;
	}
	
	public List<ManualQA> getReportList(List<EventEntity> events, Date startDate, Date endDate)
	{
		logger.info("Creating manualQA list");
		List<ManualQA> manualQAList = new ArrayList<ManualQA>();
		
		List<Acquisition> acqs = report.acqs;
		
		for (EventEntity event : events)
		{
			ManualQANotification notification = (ManualQANotification) unmarshall(event);
			ManualQA manualQA = new ManualQA();
			
			manualQA.setDateRange(startDate + " - " + endDate);
			manualQA.setTitle(notification.getTitle());
			manualQA.setMaterialID(notification.getMaterialID());
			manualQA.setChannels(notification.getChannels());
			manualQA.setAggregatorID(notification.getAggregatorID());
			manualQA.setTaskStatus(notification.getTaskStatus());
			manualQA.setPreviewStatus(notification.getPreviewStatus());
			manualQA.setEscalated(notification.getEscalated());
			manualQA.setReordered(notification.getReordered());
			
			for (Acquisition acq : acqs) {
				if (acq.getMaterialID().equals(manualQA.getMaterialID()))
					manualQA.setTitleLength(acq.getTitleLength());
			}
			
			manualQAList.add(manualQA);
		}		
		return manualQAList;
	}
				
	private void createCsv(List<ManualQA> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "title", "materialID", "channels", "operator", "aggregatorID", "taskStatus", "previewStatus", "hrPreview", "hrPreviewRequested", "escalated", "timeEscalated", "titleLength", "reordered"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (ManualQA title : titles)
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
	
	private void setStats(List<ManualQA> events)
	{
		for (ManualQA event : events)
		{
			if (event.getTaskStatus().contains("FINISHED"))
				total ++;
			if (!(event.getEscalated()==null))
				escalated ++;
			if (event.getTaskStatus().contains("FAILED"))
				failed ++;
			if (event.getReordered().equals("1"))
				reordered ++;
			if (event.getHrPreview().equals("1"))
				hr ++;
		}
	}
	
	private ManualQA addStats(String name, String value)
	{
		ManualQA qa = new ManualQA();
		qa.setTitle(name);
		qa.setMaterialID(value);
		return qa;
	}
}
	
//	ICsvBeanWriter beanWriter = null;
//	try {
//		beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
//		final String[] header = {"dateRange", "title", "materialID", "channel", "operator", "aggregatorID", "taskStatus", "previewStatus", "hrPreview", "hrPreviewRequested", "escalated", "timeEscalated", "titleLength", "reordered"};
//		final CellProcessor[] processors = getManualQAProcessor();
//		beanWriter.writeHeader(header);
//
//		int escalated = 0;
//		int reordered = 0;
//		int requiring = 0;
//		
//		for (ManualQART manualQA : manualQAs)
//		{
//			if (manualQA.getEscalated().equals("1"))
//				escalated++;
//			if ((manualQA.getReordered() != null) && (manualQA.getReordered().equals("1")))
//				reordered++;
//			if ((manualQA.getReordered() != null) && (manualQA.getPreviewStatus().contains("FIX")))
//				requiring++;
//		}
//		
//		int totalTime = 0;
//		for (ManualQART manualQA : manualQAs)
//		{
//			String time = manualQA.getTimeEscalated();
//			int hours = Integer.parseInt(time.substring(3, 5)) * 3600;
//			int mins = Integer.parseInt(time.substring(6, 8)) * 60;
//			int secs = Integer.parseInt(time.substring(9, 11)) + hours + mins;
//			totalTime += secs;
//		}
//		totalTime = totalTime / escalated;
//		String timeEscalated = formatIntoHHMMSS(totalTime);
//		logger.info("totalTime: " + totalTime + "timeEscalated: " + timeEscalated);
//		
//		ManualQART passed = new ManualQART("Total QA'd", Integer.toString(manualQAs.size()));
//		ManualQART noEscalated = new ManualQART("Number Escalated", Integer.toString(escalated));
//		ManualQART avEscalated = new ManualQART("Average Time Escalated", timeEscalated);
//		ManualQART noReordered = new ManualQART ("Number Reordered", Integer.toString(reordered));
//		ManualQART noRequiring = new ManualQART("Requiring Fix/ Stitch", Integer.toString(requiring));
//		manualQAs.add(passed);
//		manualQAs.add(noEscalated);
//		manualQAs.add(avEscalated);
//		manualQAs.add(noReordered);
//		manualQAs.add(noRequiring);
//
//		for (ManualQART manualQA : manualQAs) 
//		{
//			beanWriter.write(manualQA, header, processors);
//		}
//	}
//	catch (IOException e)
//	{
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	finally {
//		if (beanWriter != null)
//		{
//			try
//			{
//				beanWriter.close();
//			}
//			catch(IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	private String formatIntoHHMMSS(int secsIn)
//	{
//		int hours = secsIn / 3600,
//		remainder = secsIn % 3600,
//		minutes = remainder / 60,
//		seconds = remainder % 60;
//
//		return ( (hours < 10 ? "0" : "") + hours
//		+ ":" + (minutes < 10 ? "0" : "") + minutes
//		+ ":" + (seconds< 10 ? "0" : "") + seconds );
//	}
//	
//	if (payload.contains("materialID"))
//		manualQA.setMaterialID(payload.substring(payload.indexOf("materialID")+11, payload.indexOf("</materialID")));
//	if (payload.contains("title"))
//		manualQA.setTitle(payload.substring(payload.indexOf("title")+6, payload.indexOf("</title")));
//	if (payload.contains("channels"))
//		manualQA.setChannel(payload.substring(payload.indexOf("channels")+9, payload.indexOf("</channels")));
//	logger.info("channels: " + manualQA.getChannel());
//	if (payload.contains("operator"))
//		manualQA.setOperator(payload.substring(payload.indexOf("operator")+9, payload.indexOf("</operator")));
//	logger.info("operator: " + manualQA.getOperator());
//	if (payload.contains("aggregatorID"))
//		manualQA.setAggregatorID(payload.substring(payload.indexOf("aggregatorID")+13, payload.indexOf("</aggregatorID")));
//	if (payload.contains("taskStatus"))
//		manualQA.setTaskStatus(payload.substring(payload.indexOf("taskStatus")+11, payload.indexOf("</taskStatus")));
//	if (payload.contains("hrPreview"))
//		manualQA.setHrPreview(payload.substring(payload.indexOf("hrPreview")+10, payload.indexOf("</hrPreview")));
//	if (payload.contains("hrPreviewRequired"))
//		manualQA.setHrPreviewRequested(payload.substring(payload.indexOf("hrPreviewRequired")+18, payload.indexOf("</hrPreviewRequired")));
//	if (payload.contains("timeEscalated"))
//		manualQA.setTimeEscalated(payload.substring(payload.indexOf("timeEscalated")+14, payload.indexOf("</timeEscalated")));
//	
//	if (manualQA.getTimeEscalated() != null)
//		manualQA.setEscalated("1");
//	
//	List<EventEntity> previews = queryApi.getByNamespace("http://www.foxtel.com.au/ip/preview");
//	for (EventEntity preview : previews)
//	{
//		String str = preview.getPayload();
//		if (payload.contains("materialID"))
//		{
//			String curTitle = payload.substring(payload.indexOf("materialID")+11, payload.indexOf("</materialID"));
//			if (str.contains("MasterID"))
//			{
//				String previewTitle = str.substring(str.indexOf("MasterID")+9, str.indexOf("</MasterID"));
//				if (curTitle.equals(previewTitle))
//					manualQA.setPreviewStatus(str.substring(str.indexOf("PreviewStatus")+14, str.indexOf("</PreviewStatus")));
//			}
//		}
//	}
//	
//	List<EventEntity> lengthEvents = queryApi.getByNamespace("http://www.foxtel.com.au/ip/content");
//	for (EventEntity lengthEvent : lengthEvents)
//	{
//		String str = lengthEvent.getPayload();
//		logger.info("lengthEvent payload: " + str);
//		if (str.contains("materialId"))
//		{
//			//may need changed to titleID
//			String lengthTitle = str.substring(str.indexOf("materialId") +11, str.indexOf("</materialId"));
//			if (payload.contains("materialID")) {
//				String curTitle = payload.substring(payload.indexOf("materialID")+11, payload.indexOf("</materialID"));
//				if (curTitle.equals(lengthTitle)) {
//					if (str.contains("Duration")) 
//						manualQA.setTitleLength(str.substring(str.indexOf("Duration") +9, str.indexOf("</Duration")));
//				}
//			}
//		}
//	}
//	
//	if (manualQA.getPreviewStatus().contains("REORDER"))
//		manualQA.setReordered("1");
//	
//	manualQAList.add(manualQA);
//}
//	public void extractChannels(String channel)
//	{
//		if (channel.contains(","))
//		{
//			String[] channelArray = channel.split(",");
//			logger.info("Channel array: " + channelArray);
//			for (int i=0; i<channelArray.length; i++) {
//				if (!channels.contains(channelArray[i]))
//					channels.add(channelArray[i]);
//				else {
//					if (!channels.contains(channel)) {
//						logger.info("Adding channel: " + channel);
//						channels.add(channel);
//					}
//				}
//			}
//		}
//	}

	
	
//	if (payload.contains("assetID"))
//		manualQA.setMaterialID(payload.substring(payload.indexOf("assetID") +8, payload.indexOf("</assetID")));
//		if (payload.contains("title"))
//			manualQA.setTitle(payload.substring(payload.indexOf("title")+6, payload.indexOf("</title")));
//		
//		List<EventEntity> aggregatorEvents = queryApi.getByEventName("AddOrUpdateMaterial");
//		for (EventEntity aggregatorEvent : aggregatorEvents)
//		{
//			String str = aggregatorEvent.getPayload();
//			String aggregatorTitle = str.substring(str.indexOf("materialID")+12, str.indexOf('"', (str.indexOf("materialID")+12)));
//			if (payload.contains("assetID")) {
//				String curTitle =  payload.substring(payload.indexOf("assetID") +8, payload.indexOf("</assetID"));
//				if (curTitle.equals(aggregatorTitle))
//					manualQA.setAggregatorID(str.substring(str.indexOf("aggregatorID")+14, str.indexOf('"',(str.indexOf("aggregatorID")+14))));
//			}
//		}
//				
//		List<EventEntity> lengthEvents = queryApi.getByNamespace("http://www.foxtel.com.au/ip/content");
//		for (EventEntity lengthEvent : lengthEvents)
//		{
//			String str = lengthEvent.getPayload();
//			String lengthTitle = str.substring(str.indexOf("materialId") +11, str.indexOf("</materialId"));
//			logger.info("Length: " + lengthTitle);
//			if (payload.contains("AssetID")) {
//				String curTitle = payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID"));
//				logger.info("Current: " + curTitle);
//				if (curTitle.equals(lengthTitle)) {
//					if (str.contains("Duration"))
//						manualQA.setTitleLength(str.substring(str.indexOf("Duration") +9, str.indexOf("</Duration")));
//				}
//			}
//		}
//		
//		List<EventEntity> creates = queryApi.getByEventName("CreateOrUpdateTitle");
//		for (EventEntity create : creates)
//		{
//			String str = create.getPayload();
//			if (str.contains("titleID")) {
//				String title = str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9)));
//				logger.info("titleID: " + title);
//				if (str.contains("channelName")) {
//					String curTitle = payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID"));
//					logger.info("AssetID: " + curTitle);
//					if (curTitle.equals(title))
//						manualQA.setChannel(str.substring(str.indexOf("channelName")+13, str.indexOf('"',(str.indexOf("channelName")+13))));
//				}	
//			}
//		}

