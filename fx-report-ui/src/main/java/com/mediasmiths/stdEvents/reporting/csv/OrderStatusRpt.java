package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

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
	
	public void writeOrderStatus(List<EventEntity> delivered, List<EventEntity> outstanding, List<EventEntity> overdue, List<EventEntity> unmatched, Date startDate, Date endDate, String reportName)
	{
		List<OrderStatus> deliveredTitles = getTitleList(delivered, "Delivered");
		List<OrderStatus> outstandingTitles = getTitleList(outstanding, "Outstanding");
		List<OrderStatus> overdueTitles = getTitleList(overdue, "Overdue");
		List<OrderStatus> unmatchedTitles = getMaterialList(unmatched, "Unmatched", startDate, endDate);
		List<OrderStatus> titles = new ArrayList<OrderStatus>();
		titles.addAll(deliveredTitles);
		titles.addAll(outstandingTitles);
		titles.addAll(overdueTitles);
		titles.addAll(unmatchedTitles);
		logger.info(unmatchedTitles);
		
		for (OrderStatus order : titles)
		{
			order.setDateRange(startDate + " - " + endDate);					
		}
		
		createOrderStatusCsv(titles, "orderStatusCsv", reportName);
	}
	
	public List<OrderStatus> getTitleList(List<EventEntity> events, String status)
	{
		List<OrderStatus> titleList = new ArrayList<OrderStatus>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			logger.info(payload);
			OrderStatus title = new OrderStatus(); 
			title.setStatus(status);	
			if (payload.contains("ProgrammeTitle"))
				title.setTitle(payload.substring(payload.indexOf("ProgrammeTitle")+15, payload.indexOf("</ProgrammeTitle")));
			if (payload.contains("titleID"))
				title.setMaterialID(payload.substring(payload.indexOf("titleID")+9, payload.indexOf('"', (payload.indexOf("titleID")+9))));
			if (payload.contains("OrderReference"))
				title.setOrderRef(payload.substring(payload.indexOf("OrderReference")+15, payload.indexOf("</OrderReference")));
			if (payload.contains("channelName"))
				title.setChannel(payload.substring(payload.indexOf("channelName")+13, payload.indexOf('"',(payload.indexOf("channelName")+13))));
			
			Calendar required = null;
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
						required = DatatypeConverter.parseDate(title.getRequiredBy());
						logger.info("RequiredBy: " + required);
					}
				}
			}
			
			Calendar completion = null;
			List<EventEntity> completionEvents = queryApi.getByEventName("ProgrammeContentAvailable");
			for (EventEntity completionEvent : completionEvents)
			{
				String str = completionEvent.getPayload();
				if (str.contains("titleID"))
				{
					String titleID = str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9)));
					String curTitle = payload.substring(payload.indexOf("titleID")+9, payload.indexOf('"', (payload.indexOf("titleID")+9)));
					logger.info("TitleID: " + titleID + " curTitle: " + curTitle);
					if (titleID.equals(curTitle))
					{
						if (str.contains("DateOfDelivery"))
						{
							title.setCompletionDate(str.substring(str.indexOf("DateOfDelivery")+15, str.indexOf("</DateOfDelivery")));
							logger.info("CompletionDate: " + title.getCompletionDate());
							completion = DatatypeConverter.parseDate(title.getCompletionDate());
							logger.info("cal: " + completion);
						}
					}
				}	
			}
			
			if ((required != null) && (completion == null))
				title.setOverdueInDateRange("1");
			else if (required.before(completion))
				title.setOverdueInDateRange("1");
			else if (required.after(completion))
				title.setCompletedInDateRange("1");
			
			title.setTaskType("Ingest");

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
			logger.info("unmatched payload:" + payload);
			OrderStatus title = new OrderStatus();
			Date ingestDate = new Date(event.getTime());
				
			title.setStatus(status);
			title.setTitle(payload);
			title.setTaskType("Unmatched");
			
			if ((ingestDate.after(startDate)) && (ingestDate.before(endDate))) {
				title.setDateRange(startDate + " - " + endDate);
				titleList.add(title);
			}
		}
		return titleList;
			
	}
	
	public void createOrderStatusCsv(List<OrderStatus> titles, String name, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String [] header = {"dateRange", "status", "title", "materialID", "channel", "orderRef", "requiredBy", "completedInDateRange", "overdueInDateRange", "aggregatorID", "taskType", "completionDate"};
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
	
	//2013-02-13T10:00:00+11:00
	//public Date getDate(String dateString)
	//{
//		Date date = new Date();
//		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"); 
//		date = (Date) formatter.parse(dateString);
		
		//Calendar cal = DatatypeConverter.parseDate(dateString);		


	//}
	
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
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional()
		};
		return processors;
	}
}
