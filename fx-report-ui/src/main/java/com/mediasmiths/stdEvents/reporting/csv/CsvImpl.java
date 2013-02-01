package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.AcquisitionDelivery;
import com.mediasmiths.stdEvents.report.entity.AutoQC;
import com.mediasmiths.stdEvents.report.entity.ManualQA;
import com.mediasmiths.stdEvents.report.entity.OrderStatus;
import com.mediasmiths.stdEvents.report.entity.Purge;

public class CsvImpl implements CsvAPI
{
	public static final transient Logger logger = Logger.getLogger(CsvImpl.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	
//	public void writeLateOrderStatus(List<EventEntity> outstanding, List<EventEntity> unmatched)
//	{
//		List<OrderStatus> outstandingList = getTitleList(outstanding, "Outstanding");
//		List<OrderStatus> unmatchedList = getMaterialList(unmatched, "Unmatched");
//		List<OrderStatus> titles = new ArrayList<OrderStatus>();
//		titles.addAll(outstandingList);
//		titles.addAll(unmatchedList);
//		
//		createOrderStatusCsv(titles, "lateOrderStatusCsv");
//	}
	
	public void writeAcquisitionDelivery(List<EventEntity> materials)
	{
		List<AcquisitionDelivery> titles = getAcquisitionDeliveryList(materials);
		
		ICsvBeanWriter beanWriter = null;
		try{
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "acquisitionDeliveryCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "materialID", "channels", "aggregatorID", "tape", "file", "format", "fileSize", "duration"};
			final CellProcessor[] processors = getAcquisitionProcessor();
			beanWriter.writeHeader(header);
			
			AcquisitionDelivery noFile = new AcquisitionDelivery("No Delivered By File", Integer.toString(queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File"))));
			AcquisitionDelivery noTape = new AcquisitionDelivery("No Delivered By Tape", Integer.toString(queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File"))));
			titles.add(noFile);
			titles.add(noTape);
			
			int total = (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"))) + (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File")));
			int perByTape = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape")) / total;
			int perByFile = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File")) / total;
			perByTape = perByTape * 100;
			perByFile = perByFile * 100;
			
			AcquisitionDelivery perFile = new AcquisitionDelivery("% By File", Integer.toString(perByFile));
			AcquisitionDelivery perTape = new AcquisitionDelivery("% By Tape", Integer.toString(perByTape));
			titles.add(perFile);
			titles.add(perTape);
			
			for (AcquisitionDelivery title : titles)
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
	
	public void writeManualQA(List<EventEntity> events)
	{
		List<ManualQA> manualQAs = getManualQAList(events);
		
		ICsvBeanWriter beanWriter = null;
		try {
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "manualQAcsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "title", "assetID", "operator", "aggregatorID", "qaStatus", "duration"};
			final CellProcessor[] processors = getManualQAProcessor();
			beanWriter.writeHeader(header);
			
			ManualQA failed = new ManualQA("Total failed", Integer.toString(queryApi.getLength(queryApi.getTotalFailedQA())));
			manualQAs.add(failed);
			
			for (ManualQA manualQA : manualQAs) 
			{
				beanWriter.write(manualQA, header, processors);
			}
		} catch (IOException e) {
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
	
	public void writeAutoQc(List<EventEntity> passed)
	{
		List<AutoQC> autoQcs = getQCList(passed);
		
		ICsvBeanWriter beanWriter = null;
		try {
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "autoQcCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "title", "materialID", "channels", "content", "operator", "taskStatus", "qcStatus", "taskStart", "manualOverride", "failureParameter", "titleLength"};
			final CellProcessor[] processors = getAutoQCProcessor();
			beanWriter.writeHeader(header);
			
			AutoQC totalPass = new AutoQC("Total OC'd", Integer.toString(queryApi.getLength(queryApi.getTotalQCd())));
			AutoQC failed = new AutoQC("Failed QC", Integer.toString(queryApi.getLength(queryApi.getFailedQc())));
			AutoQC overridden = new AutoQC("Operator Overridden", Integer.toString(queryApi.getLength(queryApi.getOperatorOverridden())));
			autoQcs.add(totalPass);
			autoQcs.add(failed);
			autoQcs.add(overridden);
			
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
	
//	public void writeFileTapeIngest(List<EventEntity> completed, List<EventEntity> failed, List<EventEntity> unmatched)
//	{
//		logger.info("Completed: " + completed);
//		List<OrderStatus> completedTitles = getMaterialList(completed, "Completed");
//		List<OrderStatus> failedTitles = getMaterialList(failed, "Failed");
//		List<OrderStatus> unmatchedTitles = getMaterialList(unmatched, "Unmatched");
//		logger.info("Completed: " + completedTitles);
//		List<OrderStatus> titles = new ArrayList<OrderStatus>();
//		titles.addAll(completedTitles);
//		titles.addAll(failedTitles);
//		titles.addAll(unmatchedTitles);
//		
//		createOrderStatusCsv(titles, "fileTapeIngestCsv");
//	}

	public void writePurgeTitles(List<EventEntity> events)
	{
		List<Purge> purged = getPurgeList(events);
		logger.info("Events: " + events);
		logger.info("Purged: " + purged);
		ICsvBeanWriter beanWriter = null;
		try {
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "purgedContentCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "channel", "mediaID", "purgeStatus", "protectedStatus", "extendedStatus", "size"};
			final CellProcessor[] processors = getPurgeProcessor();
			beanWriter.writeHeader(header);
			
			Purge expiring = new Purge("Expiring", Integer.toString(queryApi.getLength(queryApi.getExpiring())));
			Purge purgeProtected = new Purge("Total Protected", Integer.toString(queryApi.getLength(queryApi.getPurgeProtected())));
			Purge purgePosponed = new Purge("Total Postponed", Integer.toString(queryApi.getLength(queryApi.getPurgePosponed())));
			Purge total = new Purge("Total Purged", Integer.toString(queryApi.getLength(queryApi.getTotalPurged())));
			purged.add(expiring);
			purged.add(purgeProtected);
			purged.add(purgePosponed);
			purged.add(total);
			
			for (Purge purge : purged)
			{
				beanWriter.write(purge, header, processors);
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
	
	
	
	public List<ManualQA> getManualQAList(List<EventEntity> events)
	{
		List<ManualQA> manualQAList = new ArrayList<ManualQA>();
		for (EventEntity event : events)
		{
			ManualQA manualQA = new ManualQA();
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
			
			manualQAList.add(manualQA);
		}
		return manualQAList;
	}
	public List<AutoQC> getQCList(List<EventEntity> events)
	{
		List<AutoQC> titleList = new ArrayList<AutoQC>();
		for (EventEntity event : events)
		{
			AutoQC autoQc = new AutoQC();
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
			
			autoQc.setTaskStart(Long.toString(event.getTime()));
			
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
						if (payload.contains("Duration"))
							autoQc.setTitleLength(str.substring(str.indexOf("Duration") +9, str.indexOf("</Duration")));
					}
				}
				titleList.add(autoQc);
			}
		}	
		return titleList;
	}
	
	public List<Purge> getPurgeList(List<EventEntity> events)
	{
		List<Purge> purgeList = new ArrayList<Purge>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			Purge purge = new Purge();
			if (payload.contains("titleID"))
				purge.setMediaID(payload.substring(payload.indexOf("titleID")+9, payload.indexOf('"', (payload.indexOf("titleID")+9))));
			if (payload.contains("PurgeStatus"))
				purge.setPurgeStatus(payload.substring(payload.indexOf("PurgeStatus")+12, payload.indexOf("</PurgeStatus")));
			if (payload.contains("ProtectedStatus"))
				purge.setProtectedStatus(payload.substring(payload.indexOf("ProtectedStatus")+16, payload.indexOf("</ProtectedStatus")));
			if (payload.contains("ExtendedStatus"))
				purge.setExtendedStatus(payload.substring(payload.indexOf("ExtendedStatus")+15, payload.indexOf("</ExtendedStatus")));
			purgeList.add(purge);
			
			List<EventEntity> channelEvents = queryApi.getByEventName("CreateOrUpdateTitle");
			for (EventEntity channelEvent : channelEvents)
			{
				String str = channelEvent.getPayload();
				String channelTitle = str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9)));
				if (payload.contains("titleID")) {
					String curTitle =  payload.substring(payload.indexOf("titleID")+9, payload.indexOf('"', (payload.indexOf("titleID")+9)));
					if (curTitle.equals(channelTitle)) {
						if (str.contains("channelName"))
							purge.setChannel(str.substring(str.indexOf("channelName")+13, str.indexOf('"',(str.indexOf("channelName")+13)))); 
					}
				}
			}
			purgeList.add(purge);
		}
		
		return purgeList;
	}
	
	public List<AcquisitionDelivery> getAcquisitionDeliveryList(List<EventEntity> events)
	{
		List<AcquisitionDelivery> titleList = new ArrayList<AcquisitionDelivery>();
		for(EventEntity event : events)
		{
			String payload = event.getPayload();
			AcquisitionDelivery title = new AcquisitionDelivery();
			if (payload.contains("materialId"))
			{
				String materialId = payload.substring(payload.indexOf("materialId") +11, payload.indexOf("</materialId"));
				title.setMaterialID(materialId);
			}
			
			List<EventEntity> channelEvents = queryApi.getByEventName("CreateOrUpdateTitle");
			for (EventEntity channelEvent : channelEvents)
			{
				String str = channelEvent.getPayload();
				String channelTitle = str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9)));
				logger.info("Channel: " + channelTitle);
				if (payload.contains("materialId")) {
					String curTitle =  payload.substring(payload.indexOf("materialId") +11, payload.indexOf("</materialId"));
					logger.info("Current: " + curTitle);
					if (curTitle.equals(channelTitle)) 
						title.setChannels(str.substring(str.indexOf("channelName")+13, str.indexOf('"',(str.indexOf("channelName")+13)))); 
				}
			}
			
			List<EventEntity> aggregatorEvents = queryApi.getByEventName("AddOrUpdateMaterial");
			for (EventEntity aggregatorEvent : aggregatorEvents)
			{
				String str = aggregatorEvent.getPayload();
				String aggregatorTitle = str.substring(str.indexOf("materialID")+12, str.indexOf('"', (str.indexOf("materialID")+12)));
				logger.info("agg:" + aggregatorTitle);
				if (payload.contains("materialId")) {
					String curTitle =  payload.substring(payload.indexOf("materialId") +11, payload.indexOf("</materialId"));
					logger.info("Current " + curTitle);
					if (curTitle.equals(aggregatorTitle))
						title.setAggregatorID(str.substring(str.indexOf("aggregatorID")+14, str.indexOf('"',(str.indexOf("aggregatorID")+14))));
				}
			}
			
			if (payload.contains("Tape"))
					title.setTape("1");
			if (payload.contains("File"))
					title.setFile("1");
		
		if (payload.contains("Format"))
			title.setFormat(payload.substring(payload.indexOf("Format") +7, payload.indexOf("</Format")));
			
		if (payload.contains("Duration"))
		{
			String duration = payload.substring(payload.indexOf("Duration") +9, payload.indexOf("</Duration"));
			title.setDuration(duration);
		}
		
		titleList.add(title);	
		}
		return titleList;
	}
	


	public CellProcessor[] getAcquisitionProcessor()
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
				new Optional()
		};
		return processors;
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
				new Optional()
		};
		return processors;
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
	
	public CellProcessor[] getPurgeProcessor()
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
