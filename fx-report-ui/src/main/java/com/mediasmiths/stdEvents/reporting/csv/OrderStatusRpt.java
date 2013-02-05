package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.mediasmiths.stdEvents.report.entity.OrderStatus;

public class OrderStatusRpt
{
	public static final transient Logger logger = Logger.getLogger(CsvImpl.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
	
	public void writeOrderStatus(List<EventEntity> delivered, List<EventEntity> outstanding, List<EventEntity> overdue, List<EventEntity> unmatched, Date startDate, Date endDate)
	{
		List<OrderStatus> deliveredTitles = getTitleList(delivered, "Delivered");
		List<OrderStatus> outstandingTitles = getTitleList(outstanding, "Outstanding");
		List<OrderStatus> overdueTitles = getTitleList(overdue, "Overdue");
		List<OrderStatus> unmatchedTitles = getMaterialList(unmatched, "Unmatched", startDate, endDate);
		List<OrderStatus> titles = new ArrayList<OrderStatus>();
		titles.addAll(deliveredTitles);
		titles.addAll(outstandingTitles);
		titles.addAll(overdueTitles);
		logger.info(unmatchedTitles);
		List<OrderStatus> valid = new ArrayList<OrderStatus>();
		for (OrderStatus order : titles)
		{
			if (order.getIngestDate() != null) {
				String ingestString = order.getIngestDate();
				try
				{
					Date ingestDate = sdf.parse(ingestString);
					logger.info("Date: " + ingestDate);
					if ((ingestDate.after(startDate)) && (ingestDate.before(endDate)))
						order.setDateRange(startDate + " - " + endDate);
						valid.add(order);
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
			}
		}
		valid.addAll(unmatchedTitles);
		createOrderStatusCsv(titles, "orderStatusCsv");
	}
	
	public List<OrderStatus> getTitleList(List<EventEntity> events, String status)
	{
		List<OrderStatus> titleList = new ArrayList<OrderStatus>();
		String titleID;
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			logger.info(payload);
			OrderStatus title = new OrderStatus(); 
			if (payload.contains("titleID"))
				title.setTitleID(payload.substring(payload.indexOf("titleID")+9, payload.indexOf('"', (payload.indexOf("titleID")+9))));
			title.setStatus(status);			
			if (payload.contains("OrderReference"))
				title.setOrderRef(payload.substring(payload.indexOf("OrderReference")+15, payload.indexOf("</OrderReference")));
			if (payload.contains("channelName"))
				title.setChannel(payload.substring(payload.indexOf("channelName")+13, payload.indexOf('"',(payload.indexOf("channelName")+13))));
			
			List<EventEntity> aggregatorEvents = queryApi.getByEventName("AddOrUpdateMaterial");
			for (EventEntity aggregatorEvent : aggregatorEvents)
			{
				String str = aggregatorEvent.getPayload();
				String aggregatorTitle = str.substring(str.indexOf("materialID")+12, str.indexOf('"', (str.indexOf("materialID")+12)));
				logger.info("agg:" + aggregatorTitle);
				if (payload.contains("titleID")) {
					String curTitle =  payload.substring(payload.indexOf("titleID")+9, payload.indexOf('"', (payload.indexOf("titleID")+9)));
					logger.info("Current " + curTitle);
					if (curTitle.equals(aggregatorTitle)) {
						title.setAggregatorID(str.substring(str.indexOf("aggregatorID")+14, str.indexOf('"',(str.indexOf("aggregatorID")+14))));
						title.setOrderRef(str.substring(str.indexOf("OrderReference")+15, str.indexOf('<', (str.indexOf("OrderReference")))));
						title.setRequiredBy(str.substring(str.indexOf("RequiredBy")+11, str.indexOf("</RequiredBy")));
					}
				}
			}
			Date ingestDate = new Date(event.getTime());
			title.setIngestDate(ingestDate.toString());
  			titleList.add(title);
		}	
		return titleList;
	}
	
	public List<OrderStatus> getMaterialList(List<EventEntity> events, String status, Date startDate, Date endDate)
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
				title.setStatus(status);
				Date ingestDate = new Date(event.getTime());
				logger.info(titleID + " " + ingestDate);
				if ((ingestDate.after(startDate)) && (ingestDate.before(endDate))) {
					title.setDateRange(startDate + " - " + endDate);
					titleList.add(title);
				}
			}
			
		}	
		return titleList;
	}
	
	public void createOrderStatusCsv(List<OrderStatus> titles, String name)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + name + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String [] header = {"dateRange", "status", "titleID", "orderRef", "channel", "aggregatorID", "taskType", "ingestDate", "requiredBy"};
			final CellProcessor[] processors = getTitleProcessor();
			beanWriter.writeHeader(header);
			
			OrderStatus qDelivered = new OrderStatus("Quantity Delivered", Integer.toString(queryApi.getLength(queryApi.getDelivered())));
			OrderStatus qOutstanding = new OrderStatus("Quantity Outstanding", Integer.toString(queryApi.getLength(queryApi.getOutstanding())));
			//OrderStatus qOverdue = new OrderStatus("Quantity Overdue", Integer.toString(queryApi.getLength(queryApi.getOverdue())));		
			titles.add(qDelivered);
			titles.add(qOutstanding);
			//titles.add(qOverdue);
			
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
	
	public CellProcessor[] getTitleProcessor()
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
}
