package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.AddOrUpdateMaterial;
import com.mediasmiths.foxtel.ip.common.events.AddOrUpdatePackage;
import com.mediasmiths.foxtel.ip.common.events.CreateOrUpdateTitle;
import com.mediasmiths.foxtel.ip.common.events.report.OrderStatus;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.OrderStatusRT;

public class OrderStatusRpt
{
	public static final transient Logger logger = Logger.getLogger(OrderStatusRpt.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	private int delivered=0;
	private int outstanding=0;
	private int overdue=0;
	private int unmatched=0;
		
	public void writeOrderStatus(List<EventEntity> events, Date startDate, Date endDate, String reportName)
	{
		List<OrderStatus> orders = getReportList(events, startDate, endDate);
		//setStats(orders);
		OrderStatus del = addStats("Total Delivered", Integer.toString(delivered));
		OrderStatus out = addStats("Total Outstanding", Integer.toString(outstanding));
		OrderStatus ove = addStats("Total Overdue", Integer.toString(overdue));
		OrderStatus unm = addStats("Total Unmatched", Integer.toString(unmatched));
		
		orders.add(del);
		orders.add(out);
		orders.add(ove);
		orders.add(unm);
		createCsv(orders, reportName);
	}
	
	private Object unmarshall(EventEntity event)
	{
		Object placeholder = null;
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);

		try
		{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
			logger.info("Deserialising payload");
			placeholder = JAXB_SERIALISER.deserialise(payload);
			logger.info("Object created");
		}
		catch (Exception e)		
		{
			e.printStackTrace();
		}	
		return placeholder;
	}
		
	 
	private List<OrderStatus> getReportList (List<EventEntity> events, Date startDate, Date endDate)
	{
		logger.info("Creating orderStatus list");
		List<OrderStatus> orders = new ArrayList<OrderStatus>();
		for (EventEntity event : events)
		{
			CreateOrUpdateTitle title = (CreateOrUpdateTitle) unmarshall(event);
			AddOrUpdatePackage pack = new AddOrUpdatePackage();
			List<EventEntity> packList = queryApi.getByEventName("AddOrUpdatePackage");
			for (EventEntity packEvent : packList)
			{
				AddOrUpdatePackage currentPack = (AddOrUpdatePackage) unmarshall(packEvent);
				if (currentPack.getTitleID().equals(title.getTitleID())) {
					pack = currentPack;
					logger.info("package found");
				}
			}
			
			AddOrUpdateMaterial material = new AddOrUpdateMaterial();
			List<EventEntity> materialList = queryApi.getByEventName("AddOrUpdateMaterial");
			for (EventEntity materialEvent : materialList)
			{
				AddOrUpdateMaterial currentMaterial = (AddOrUpdateMaterial) unmarshall(materialEvent);
				if (currentMaterial.getMaterialID().equals(pack.getMaterialID())) {
					material = currentMaterial;
					logger.info("material found");
				}
			}
			
			OrderStatus order = new OrderStatus();
			order.setDateRange(startDate + " - " + endDate);
			order.setTitle(title.getTitle());
			order.setMaterialID(pack.getMaterialID());
			order.setChannels(title.getChannels());
			order.setRequiredBy(pack.getRequiredBy());
			order.setCompletionDate(material.getCompletionDate());
			
			if ((order.getRequiredBy() != null) && (order.getCompletionDate() != null))
			{
				Calendar required = DatatypeConverter.parseDateTime(order.getRequiredBy().toString());
				Calendar completed = DatatypeConverter.parseDateTime(order.getCompletionDate().toString());
				if (required.after(completed))
					order.setCompletedInDateRange("1");
				else
					order.setOverdueInDateRange("1");
			}
			else if (order.getRequiredBy() != null)
			{
				Calendar required = DatatypeConverter.parseDateTime(order.getRequiredBy().toString());
				if (required.after(new Date()))
					order.setOverdueInDateRange("1");
			}

			orders.add(order);
			logger.info("Order: " + title.getTitle() + " " + title);
		}
		return orders;
	}
	
	private void createCsv(List<OrderStatus> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String [] header = {"dateRange", "title", "materialID", "channels", "orderRef", "requiredBy", "completedInDateRange", "overdueInDateRange", "aggregatorID", "taskType", "completionDate"};
			final CellProcessor[] processors = getProcessor();
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
				new Optional()
		};
		return processors;
	}
	
	private void setStats (List<OrderStatus> events)
	{
		for (OrderStatus event : events)
		{
			if (!(event.getCompletedInDateRange() == null))
			{
				if ((event.getCompletedInDateRange().equals("1")) && (event.getTaskType().equals("Ingest")))
					delivered++;
				logger.info("getting outstanding");
				if ((! event.getCompletedInDateRange().equals("1")) && (event.getTaskType().equals("Ingest")))
					outstanding++;
				logger.info("getting overdue");
			}
			
			if (!(event.getOverdueInDateRange() == null))
			{
				if ((event.getOverdueInDateRange().equals("1")) && ((event.getTaskType().equals("Ingest")) || (event.getTaskType().equals("ingest"))))
					overdue++;
			}
			
			logger.info("getting unmatched");
			if (event.getTaskType() != null)
			{
				if (event.getTaskType().equals("Unmatched"))
					unmatched++;
			}
		}	
	}
	
	private OrderStatus addStats(String name, String value)
	{
		OrderStatus stat = new OrderStatus();
		stat.setTitle(name);
		stat.setMaterialID(value);
		return stat;
	}
}
	





//logger.info("Creating orderStatus list");
//List<OrderStatusRT> orders = new ArrayList<OrderStatusRT>();
//for (EventEntity event : events)
//{
//	String payload = event.getPayload();
//	OrderStatusRT title = new OrderStatusRT();
//	title.setDateRange(startDate + " - " + endDate);
//	if (payload.contains("ProgrammeTitle"))
//		title.setTitle(payload.substring(payload.indexOf("ProgrammeTitle")+15, payload.indexOf("</ProgrammeTitle")));
//	if (payload.contains("mediaID"))
//		title.setMaterialID(payload.substring(payload.indexOf("mediaID")+8, payload.indexOf("</mediaID")));
//	if (payload.contains("OrderReference"))
//		title.setOrderRef(payload.substring(payload.indexOf("OrderReference")+15, payload.indexOf("</OrderReference")));
//	if (payload.contains("channelName"))
//		title.setChannel(payload.substring(payload.indexOf("channelName")+13, payload.indexOf('"',(payload.indexOf("channelName")+13))));
//	
//	Calendar required = null;
//	List<EventEntity> aggregatorEvents = queryApi.getByEventName("AddOrUpdateMaterial");
//	for (EventEntity aggregatorEvent : aggregatorEvents)
//	{
//		String str = aggregatorEvent.getPayload();
//		String aggregatorTitle = str.substring(str.indexOf("mediaID")+8, str.indexOf("</mediaID"));
//		logger.info("agg:" + aggregatorTitle);
//		if (payload.contains("mediaID")) {
//			String curTitle =  payload.substring(payload.indexOf("mediaID")+8, payload.indexOf("</mediaID"));
//			logger.info("Current " + curTitle);
//			if (curTitle.equals(aggregatorTitle)) {
//				title.setAggregatorID(str.substring(str.indexOf("aggregatorID")+14, str.indexOf('"',(str.indexOf("aggregatorID")+14))));
//				title.setOrderRef(str.substring(str.indexOf("OrderReference")+15, str.indexOf('<', (str.indexOf("OrderReference")))));
//				title.setRequiredBy(str.substring(str.indexOf("RequiredBy")+11, str.indexOf("</RequiredBy")));
//				required = DatatypeConverter.parseDate(title.getRequiredBy());
//				logger.info("RequiredBy: " + required);
//			}
//		}
//	}
//	
//	Calendar completion = null;
//	List<EventEntity> completionEvents = queryApi.getByEventName("CreationComplete");
//	for (EventEntity completionEvent : completionEvents)
//	{
//		String str = completionEvent.getPayload();
//		if (str.contains("mediaID"))
//		{
//			String titleID = str.substring(str.indexOf("mediaID")+8, str.indexOf("</mediaID"));
//			String curTitle = payload.substring(payload.indexOf("mediaID")+9, payload.indexOf("</mediaID"));
//			logger.info("TitleID: " + titleID + " curTitle: " + curTitle);
//			if (titleID.equals(curTitle))
//			{
//				if (str.contains("DateOfDelivery"))
//				{
//					title.setCompletionDate(str.substring(str.indexOf("DateOfDelivery")+15, str.indexOf("</DateOfDelivery")));
//					logger.info("CompletionDate: " + title.getCompletionDate());
//					completion = DatatypeConverter.parseDate(title.getCompletionDate());
//					logger.info("cal: " + completion);
//				}
//			}
//		}	
//	}