package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
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
	private QueryAPIImpl queryApi;

	@Inject
	@Named("windowMax")
	public int MAX;

	public void writeOrderStatus(
			final List<com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus> orders,
			final DateTime start,
			final DateTime end,
			final String reportName)
	{
		logger.debug(">>>writeOrderStatus");
		logger.debug("List size: " + orders.size());

		fillTransientOrderStatusFields(orders, start, end);
		OrderStatusStats stats = getStats(orders);

		createCsv(orders, stats, reportName, start, end);
		logger.debug("<<<writeOrderStatus");
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

			final String startDate = start.toString(dateOnlyFormatter);
			final String endDate = end.toString(dateOnlyFormatter);
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
				putFormattedDateInCSVMap(header,5,orderMap,order.getRequiredBy(),dateOnlyFormat);

				orderMap.put(header[6], order.getComplete());
				orderMap.put(header[7], order.getOverdue());
				orderMap.put(header[8], order.getAggregatorID());
				orderMap.put(header[9], order.getTaskType());

				putFormattedDateInCSVMap(header,10,orderMap,order.getCompleted(),dateOnlyFormat);

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
