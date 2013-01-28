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
import com.mediasmiths.stdEvents.report.entity.AcquisitionDelivery;
import com.mediasmiths.stdEvents.report.entity.OrderStatus;

public class CsvImpl implements CsvAPI
{
	public static final transient Logger logger = Logger.getLogger(CsvImpl.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	public void writeOrderStatus(List<EventEntity> delivered, List<EventEntity> outstanding, List<EventEntity> overdue, List<EventEntity> unmatched)
	{
		List<OrderStatus> deliveredTitles = getTitleList(delivered, "Delivered");
		List<OrderStatus> outstandingTitles = getTitleList(outstanding, "Outstanding");
		List<OrderStatus> overdueTitles = getTitleList(overdue, "Overdue");
		List<OrderStatus> unmatchedTitles = getMaterialList(unmatched, "Unmatched");
		List<OrderStatus> titles = new ArrayList<OrderStatus>();
		titles.addAll(deliveredTitles);
		titles.addAll(outstandingTitles);
		titles.addAll(overdueTitles);
		logger.info(unmatchedTitles);
		titles.addAll(unmatchedTitles);
		
		createOrderStatusCsv(titles, "orderStatusCsv");
	}
	
	public void writeLateOrderStatus(List<EventEntity> outstanding, List<EventEntity> unmatched)
	{
		List<OrderStatus> outstandingList = getTitleList(outstanding, "Outstanding");
		List<OrderStatus> unmatchedList = getMaterialList(unmatched, "Unmatched");
		List<OrderStatus> titles = new ArrayList<OrderStatus>();
		titles.addAll(outstandingList);
		titles.addAll(unmatchedList);
		
		createOrderStatusCsv(titles, "lateOrderStatusCsv");
	}
	
	public void writeAcquisitionDelivery(List<EventEntity> file, List<EventEntity> tape)
	{
		List<AcquisitionDelivery> fileTitles = getAcquisitionDeliveryList(file, "File");
		List<AcquisitionDelivery> tapeTitles = getAcquisitionDeliveryList(tape, "Tape");
		List<AcquisitionDelivery> titles = new ArrayList<AcquisitionDelivery>();
		titles.addAll(fileTitles);
		titles.addAll(tapeTitles);
		
		ICsvBeanWriter beanWriter = null;
		try{
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "acquisitionDeliveryCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"status", "titleID", "format", "duration"};
			final CellProcessor[] processors = getAcquisitionProcessor();
			beanWriter.writeHeader(header);
			
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
	
	public void writeFileTapeIngest(List<EventEntity> completed, List<EventEntity> failed, List<EventEntity> unmatched)
	{
		logger.info("Completed: " + completed);
		List<OrderStatus> completedTitles = getMaterialList(completed, "Completed");
		List<OrderStatus> failedTitles = getMaterialList(failed, "Failed");
		List<OrderStatus> unmatchedTitles = getMaterialList(unmatched, "Unmatched");
		logger.info("Completed: " + completedTitles);
		List<OrderStatus> titles = new ArrayList<OrderStatus>();
		titles.addAll(completedTitles);
		titles.addAll(failedTitles);
		titles.addAll(unmatchedTitles);
		
		createOrderStatusCsv(titles, "fileTapeIngestCsv");
	}
	
	public void writeAutoQc(List<EventEntity> passed)
	{
		logger.info("QC: " + passed);
		List<OrderStatus> titles = getQCList(passed);
		logger.info("QC titles: " + titles);
		
		createOrderStatusCsv(titles, "autoQcCsv");
	}
	
	public void writePurgeTitles(List<EventEntity> purged)
	{
		List<OrderStatus> titles = getTitleList(purged, "Purged");
		
		createOrderStatusCsv(titles, "purgedContentCsv");
	}
	
	public void createOrderStatusCsv(List<OrderStatus> titles, String name)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + name + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String [] header = {"status", "titleID", "orderRef", "channel"};
			final CellProcessor[] processors = getTitleProcessor();
			beanWriter.writeHeader(header);
			
			for (OrderStatus title : titles)
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
	
	public List<OrderStatus> getTitleList(List<EventEntity> events, String status)
	{
		List<OrderStatus> titleList = new ArrayList<OrderStatus>();
		String titleID;
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			if (event.getEventName().equals("CreateOrUpdateTitle"))
				titleID = payload.substring(payload.indexOf("titleID")+9, payload.indexOf('"', (payload.indexOf("titleID")+9)));
			else if ((event.getEventName().equals("AddOrUpdateMaterial")) || (event.getEventName().equals("AddOrUpdatePackage")) || (event.getEventName().equals("DeletePackage")) || (event.getEventName().equals("DeleteMaterial")))
				titleID = payload.substring(payload.indexOf("titleID")+9, payload.indexOf('>', (payload.indexOf("titleID")+9)));
			else if (event.getEventName().equals("PurgeTitle"))
				titleID = payload.substring(payload.indexOf("titleID")+9, payload.indexOf('/', (payload.indexOf("titleID")+9)));
			else
				titleID = "not available";
			
			OrderStatus title = new OrderStatus();
			title.setTitleID(titleID);
			title.SetStatus(status);
			if (payload.contains("OrderReference"))
				title.setOrderRef(payload.substring(payload.indexOf("OrderReference")+15, payload.indexOf("</OrderReference")));
			if (payload.contains("channelName"))
				title.setChannel(payload.substring(payload.indexOf("channelName")+13, payload.indexOf('"',(payload.indexOf("channelName")+13))));
			if (payload.contains("aggregatorID"))
				title.setAggregatorID(payload.substring(payload.indexOf("aggregatorID")+14, payload.indexOf('"',(payload.indexOf("aggregatorID")+14))));
  			titleList.add(title);
			}	
		return titleList;
	}
	
	public List<OrderStatus> getMaterialList(List<EventEntity> events, String status)
	{
		List<OrderStatus> titleList = new ArrayList<OrderStatus>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			if (payload.contains("materialId"))
			{
				String titleID = payload.substring(payload.indexOf("materialId") +11, payload.indexOf("</materialId"));
				OrderStatus title = new OrderStatus();
				title.setTitleID(titleID);
				title.SetStatus(status);
				titleList.add(title);
			}
		}	
		return titleList;
	}
	
	public List<OrderStatus> getQCList(List<EventEntity> events)
	{
		List<OrderStatus> titleList = new ArrayList<OrderStatus>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			if (payload.contains("AssetId"))
			{
				String titleID = payload.substring(payload.indexOf("AssetId") +8, payload.indexOf("</AssetId"));
				OrderStatus title = new OrderStatus();
				title.setTitleID(titleID);
				title.SetStatus(event.getEventName());
				titleList.add(title);
			}
		}	
		return titleList;
	}
	
	public List<AcquisitionDelivery> getAcquisitionDeliveryList(List<EventEntity> events, String status)
	{
		List<AcquisitionDelivery> titleList = new ArrayList<AcquisitionDelivery>();
		for(EventEntity event : events)
		{
			String payload = event.getPayload();
			AcquisitionDelivery title = new AcquisitionDelivery();
			if (payload.contains("materialId"))
			{
				String titleID = payload.substring(payload.indexOf("materialId") +11, payload.indexOf("</materialId"));
				title.setTitleID(titleID);
				title.setStatus(status);
			}
			if (payload.contains("Format"))
			{
				String format = payload.substring(payload.indexOf("Format") +7, payload.indexOf("</Format"));
				title.setFormat(format);
			}
			if (payload.contains("Duration"))
			{
				String duration = payload.substring(payload.indexOf("Duration") +9, payload.indexOf("</Duration"));
				title.setDuration(duration);
			}
			titleList.add(title);	
		}
		return titleList;
	}
	
	public CellProcessor[] getTitleProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] {
				new NotNull(),
				new NotNull(),
				new Optional(),
				new Optional()
		};
		return processors;
	}

	public CellProcessor[] getAcquisitionProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] {
				new NotNull(), 
				new NotNull(),
				new Optional(),
				new Optional()
		};
		return processors;
	}	
}
