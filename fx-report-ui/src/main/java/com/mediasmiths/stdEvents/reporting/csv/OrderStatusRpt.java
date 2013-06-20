package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderStatusRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(OrderStatusRpt.class);

	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;

	@Inject
	private QueryAPI queryApi;

	@Inject
	@Named("windowMax")
	public int MAX;
	
	private static final String formatString = "dd-MM-yyyy";
	private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern(formatString);
	private static final DateFormat df = new SimpleDateFormat(formatString);

	public void writeOrderStatus(
			final List<com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus> orders,
			final DateTime start,
			final DateTime end,
			final String reportName)
	{
		logger.debug(">>>writeOrderStatus");
		logger.debug("List size: " + orders.size());

		fillTransientFields(orders, start, end);
		OrderStatusStats stats = getStats(orders);

		createCsv(orders, stats, reportName, start, end);
		logger.debug("<<<writeOrderStatus");
	}

	// fills in some information not returned by the sql query, perhaps the query could be modified to include this information but we will see how this goes
	private void fillTransientFields(
			List<com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus> orders,
			DateTime start,
			DateTime end)
	{
		Interval interval = new Interval(start, end);

		for (OrderStatus orderStatus : orders)
		{
			if (orderStatus.getRequiredBy() != null)
			{

				DateTime requiredByDate = new DateTime(orderStatus.getRequiredBy());

				if (orderStatus.getCompleted() != null)
				{
					// have required by and completed date
					DateTime completedDate = new DateTime(orderStatus.getCompleted());

					// determine if complete in date range
					if (interval.contains(completedDate))
					{
						orderStatus.setComplete(Boolean.TRUE);
					}
					else
					{
						orderStatus.setComplete(Boolean.FALSE);
					}

					// determine if overdue in date range
					if (completedDate.isAfter(requiredByDate))
					{
						orderStatus.setOverdue(Boolean.TRUE);
					}
					else
					{
						orderStatus.setOverdue(Boolean.FALSE);
					}

				}
				else
				{
					// no completed date
					orderStatus.setComplete(Boolean.FALSE);

					// determine if overdue in date range
					if (end.isAfter(requiredByDate))
					{
						orderStatus.setOverdue(Boolean.TRUE);
					}
					else
					{
						orderStatus.setOverdue(Boolean.FALSE);
					}
				}
			}
			else
			{
				// no required by date so cant be overdue
				orderStatus.setOverdue(Boolean.FALSE);

				if (orderStatus.getCompleted() != null)
				{
					// have completed date
					DateTime completedDate = new DateTime(orderStatus.getCompleted());

					// determine if complete in date range
					if (interval.contains(completedDate))
					{
						orderStatus.setComplete(Boolean.TRUE);
					}
					else
					{
						orderStatus.setComplete(Boolean.FALSE);
					}
				}
				else
				{
					// no completed date
					orderStatus.setComplete(Boolean.FALSE);
				}
			}
		}

	}

	private void createCsv(
			List<OrderStatus> orderStatuses,
			OrderStatusStats stats,
			String reportName,
			DateTime start,
			DateTime end)
	{
		ICsvMapWriter csvwriter = null;

		try
		{
			logger.info("reportName: " + reportName);
			FileWriter fileWriter = new FileWriter(REPORT_LOC + reportName + ".csv");
			csvwriter = new CsvMapWriter(fileWriter, CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = { "DateRange", "Title", "MaterialID", "Channels", "OrderReference", "RequiredBy",
					"CompletedInDateRange", "OverdueInDateRange", "AggregatorID", "TaskType", "CompletionDate" };
			final CellProcessor[] processors = getProcessor();
			csvwriter.writeHeader(header);

			final String startDate = start.toString(dateFormatter);
			final String endDate = end.toString(dateFormatter);
			final String dateRange = String.format("%s - %s", startDate, endDate);

			for (OrderStatus order : orderStatuses)
			{
				final Map<String, Object> orderMap = new HashMap<String, Object>();

				orderMap.put(header[0], dateRange);
				if (order.getTitle() != null)
				{
					orderMap.put(header[1], order.getTitle().getTitle());
					putChannelListToCSVMap(header, 3, orderMap, order.getTitle().getChannels());
				}
				else
				{
					orderMap.put(header[1], null);
					orderMap.put(header[3], null);
				}
				orderMap.put(header[2], order.getMaterialid());
				orderMap.put(header[4], order.getOrderReference());
				putFormattedDateInCSVMap(header,5,orderMap,order.getRequiredBy(),df);

				orderMap.put(header[6], order.getComplete());
				orderMap.put(header[7], order.getOverdue());
				orderMap.put(header[8], order.getAggregatorID());
				orderMap.put(header[9], order.getTaskType());

				putFormattedDateInCSVMap(header,10,orderMap,order.getCompleted(),df);

				csvwriter.write(orderMap, header, processors);
			}

			StringBuilder statsString = new StringBuilder();
			statsString.append(String.format("Total Delivered %d\n", stats.delivered));
			statsString.append(String.format("Total Outstanding %d\n", stats.outstanding));
			statsString.append(String.format("Total Overdue %d\n", stats.overdue));
			statsString.append(String.format("Total Unmatched %d\n", stats.unmatched));

			csvwriter.flush();
			IOUtils.write(statsString.toString(), fileWriter);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (csvwriter != null)
			{
				try
				{
					csvwriter.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private CellProcessor[] getProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] { new Optional(), new Optional(), new Optional(), new Optional(),
				new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), new Optional() };
		return processors;
	}

	class OrderStatusStats
	{

		public int delivered = 0;
		public int outstanding = 0;
		public int overdue = 0;
		public int unmatched = 0;

	}

	public OrderStatusStats getStats(List<OrderStatus> events)
	{

		OrderStatusStats stats = new OrderStatusStats();

		for (OrderStatus event : events)
		{

			if (event.getComplete().booleanValue())
			{
				stats.delivered++;
			}
			else if (!event.getComplete().booleanValue() && event.getRequiredBy() != null)
			{
				stats.outstanding++;
			}

			if (event.getOverdue().booleanValue() && event.getTaskType().equals(OrderStatus.TaskType.INGEST))
			{
				stats.overdue++;
			}

			if (event.getTaskType().equals(OrderStatus.TaskType.UNMATCHED))
			{
				stats.unmatched++;
			}
		}
		return stats;
	}
}
