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
		
//		setStats(orders);
//		OrderStatus del = new OrderStatus("Total Delivered", Integer.toString(delivered));
//		OrderStatus out = new OrderStatus("Total Outstanding", Integer.toString(outstanding));
//		OrderStatus ove = new OrderStatus("Total Overdue", Integer.toString(overdue));
//		OrderStatus unm = new OrderStatus("Total Unmatched", Integer.toString(unmatched));
//		
//		orders.add(del);
//		orders.add(out);
//		orders.add(ove);
//		orders.add(unm);
		
		createCsv(orders, reportName);
	}
	
	private OrderStatus unmarshall(EventEntity event)
	{
		Object title = new OrderStatus();
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);

		try
		{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.report.ObjectFactory.class);
			logger.info("Deserialising payload");
			title = JAXB_SERIALISER.deserialise(payload);
			logger.info("Object created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return (OrderStatus)title;
	}
	 
	private List<OrderStatus> getReportList (List<EventEntity> events, Date startDate, Date endDate)
	{
		logger.info("Creating orderStatus list");
		List<OrderStatus> orders = new ArrayList<OrderStatus>();
		for (EventEntity event : events)
		{
			OrderStatus title = unmarshall(event);
			title.setDateRange(startDate + " - " + endDate);
			if ((title.getRequiredBy() != null) && (title.getCompletionDate() != null))
			{
				Calendar required = DatatypeConverter.parseDateTime(title.getRequiredBy().toString());
				Calendar completed = DatatypeConverter.parseDateTime(title.getCompletionDate().toString());
				if (required.after(completed))
					title.setCompletedInDateRange("1");
				else 
					title.setOverdueInDateRange("1");
			}
			else if ((title.getRequiredBy() != null) && (title.getCompletionDate() == null))
				title.setOverdueInDateRange("1");
			
			orders.add(title);
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
			
			if ((event.getCompletedInDateRange().equals("1")) && (event.getTaskType().equals("Ingest")))
				delivered++;
			logger.info("getting outstanding");
			if ((! event.getCompletedInDateRange().equals("1")) && (event.getTaskType().equals("Ingest")))
				outstanding++;
			logger.info("getting overdue");
			
			if ((event.getOverdueInDateRange().equals("1")) && (event.getTaskType().equals("Ingest")))
				overdue++;
			logger.info("getting unmatched");
			if (event.getTaskType().equals("Unmatched"))
				unmatched++;
		}
	}
	
	
}
