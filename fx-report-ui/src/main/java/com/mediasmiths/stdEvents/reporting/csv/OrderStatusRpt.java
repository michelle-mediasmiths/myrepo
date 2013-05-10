package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.report.OrderStatus;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AggregatedBMS;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;

public class OrderStatusRpt
{
	public static final transient Logger logger = Logger.getLogger(OrderStatusRpt.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	@Inject
	@Named("windowMax")
	public int MAX;
	
	public int delivered=0;
	public int outstanding=0;
	public int overdue=0;
	public int unmatched=0;
		
	public void writeOrderStatus(final List<AggregatedBMS> events, final DateTime startDate, final DateTime endDate, final String reportName)
	{
		logger.debug(">>>writeOrderStatus");
		logger.info("List size: " + events.size());
		List<OrderStatus> orders = getReportList(events, startDate, endDate);
		setStats(orders);
		orders.add(addStats("Total Delivered", Integer.toString(delivered)));
		orders.add(addStats("Total Outstanding", Integer.toString(outstanding)));
		orders.add(addStats("Total Overdue", Integer.toString(overdue)));
		orders.add(addStats("Total Unmatched", Integer.toString(unmatched)));
		
		createCsv(orders, reportName);
		logger.debug("<<<writeOrderStatus");
	}
	
	public List<OrderStatus> getReportList(final List<AggregatedBMS> events, final DateTime startDate, final DateTime endDate)
	{
		logger.info("USING AGGREGATED BMS TABLE");
		List<OrderStatus> orders = new ArrayList<OrderStatus>();
		
		for (AggregatedBMS bms : events) 
		{
			OrderStatus order = new OrderStatus();
			order.setDateRange(startDate + " - " + endDate);
			order.setTitle(bms.getTitle());
			order.setMaterialID(bms.getMaterialID());
			order.setChannels(bms.getChannels());
			order.setTaskType("Ingest");
			order.setAggregatorID(bms.getAggregatorID());
			if ((bms.getRequiredBy() != null) && (bms.getCompletionDate() != null)) {
				try
				{
					order.setRequiredBy(DatatypeFactory.newInstance().newXMLGregorianCalendar(bms.getRequiredBy()));
					order.setCompletionDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(bms.getCompletionDate()));
				}
				catch (DatatypeConfigurationException e)
				{
					e.printStackTrace();
				}
			}
			
			if ((order.getRequiredBy() != null) && (order.getCompletionDate() != null))
			{
				logger.debug("required by and completion dates were set, checking if within range");
				if (order.getRequiredBy().toGregorianCalendar().after(order.getCompletionDate().toGregorianCalendar()))
					order.setCompletedInDateRange("1");
				else
					order.setOverdueInDateRange("1");
			}
			else if (order.getRequiredBy() != null)
			{
				logger.debug("required by date not null");
				if (order.getRequiredBy().toGregorianCalendar().before(new Date()))
					order.setOverdueInDateRange("1");
			}
			
			orders.add(order);
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
	
	public void setStats (List<OrderStatus> events)
	{
		for (OrderStatus event : events)
		{
			if ((event.getCompletedInDateRange() != null) && (event.getTaskType() != null))
			{
				if ((event.getCompletedInDateRange().equals("1")) && (event.getTaskType().equalsIgnoreCase("Ingest")))
					delivered++;
				
				logger.info("getting outstanding");
				if ((!event.getCompletedInDateRange().equals("1")) && (event.getTaskType().equalsIgnoreCase("Ingest")))
					outstanding++;	
			}
			
			logger.info("getting overdue");
			if (event.getOverdueInDateRange() != null)
			{
				if ((event.getOverdueInDateRange().equals("1")) && (event.getTaskType().equalsIgnoreCase("Ingest")))
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