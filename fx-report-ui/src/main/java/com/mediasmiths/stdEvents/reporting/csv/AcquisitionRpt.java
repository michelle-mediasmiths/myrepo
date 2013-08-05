package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.commons.io.FileUtils;
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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcquisitionRpt extends ReportUtils
{
	public static final transient Logger log = Logger.getLogger(AcquisitionRpt.class);

	// Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;

	@Inject
	private QueryAPIImpl queryApi;

	public void writeAcquisitionDelivery(
			final List<com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus> orders,
			final DateTime startDate,
			final DateTime endDate,
			final String reportName)
	{
		log.debug(">>>writeAcquisitionDelivery");

		fileSizeAndDeliveryType(orders);
		fillTransientOrderStatusFields(orders, startDate, endDate);
		OrderStatusStats stats = getStats(orders);
		createCSV(orders, stats, reportName, startDate, endDate);

		log.debug("<<<writeAcquisitionDelivery");
	}

	public static String acquisitionReportFileSize(long fileSize)
	{
		String displaySize;

		if (fileSize >= FileUtils.ONE_GB)
		{
			displaySize = String.valueOf(fileSize/ FileUtils.ONE_GB);

			log.debug("fileSize long : " + fileSize);
			log.debug("file size in GB : " + displaySize);
		}
		else
		{
			double file = (double) fileSize;
			DecimalFormat twoDForm = new DecimalFormat("#.####");

			Double fileDisplaySize = file / ((double) FileUtils.ONE_GB);

			log.debug("file size long : " + fileSize);
			log.debug("file size in GB : " + Double.valueOf(twoDForm.format(fileDisplaySize)));

			displaySize = Double.valueOf(twoDForm.format(fileDisplaySize)).toString();
		}
		return displaySize;
	}

	private void fileSizeAndDeliveryType(List<OrderStatus> orders)
	{
		//Retrieving file size in GB

		for (OrderStatus order : orders)
		{
			long fileSize=0;
			String aggregatorID = order.getAggregatorID();
			//Set FileDel/TapeDel
			if (aggregatorID != null)
			{
				if (aggregatorID.toLowerCase().equals("ruzz")
				    || aggregatorID.toLowerCase().equals("vizcapture")
				    || aggregatorID.toLowerCase().equals("dart"))
				{
					order.setTapeDelivery(Boolean.TRUE);
					order.setFileDelivery(Boolean.FALSE);
				}
			}
		}
	}

	private void createCSV(List<OrderStatus> orderStatuses, OrderStatusStats stats, String reportName, DateTime start, DateTime end)
	{
		ICsvMapWriter csvwriter = null;
		try
		{
			log.info("reportName: " + reportName);
			FileWriter fileWriter = new FileWriter(REPORT_LOC + reportName + ".csv");
			csvwriter = new CsvMapWriter(fileWriter, CsvPreference.STANDARD_PREFERENCE);
			log.info("Saving to: " + REPORT_LOC);

			final String[] header = {"dateRange",
			                         "title",
			                         "materialID",
			                         "channels",
			                         "aggregatorID",
			                         "tapeDel",
			                         "fileDel",
			                         "format",
			                         "Filesize (GB)",
			                         "titleLength"};

			final CellProcessor[] processors = getProcessor();
			csvwriter.writeHeader(header);

			final String startDate = start.toString(dateOnlyFormatter);
			final String endDate = end.toString(dateOnlyFormatter);
			final String dateRange = String.format("%s - %s", startDate, endDate);

			for (OrderStatus order : orderStatuses)
			{
				final Map<String, Object> orderMap = new HashMap<String, Object>();
				orderMap.put(header[0], dateRange);

				log.info("If ingest is completed then get the details from orderstatus");

				if (order.getTitle() != null)
				{
					orderMap.put(header[1], order.getTitle().getTitle());
					putChannelListToCSVMap(header, 3,orderMap, order.getTitle().getChannels());
				}
				else
				{
					orderMap.put(header[1], null);
					orderMap.put(header[3], null);
				}
				orderMap.put(header[2], order.getMaterialid());
				orderMap.put(header[4], order.getAggregatorID());

				if (order.isTapeDelivery())
				{
					orderMap.put(header[5], "1");
				}
				else
				{
					orderMap.put(header[5], "0");
				}

				if (order.isFileDelivery())
				{
					orderMap.put(header[6], "1");
				}
				else
				{
					orderMap.put(header[6], "0");
				}

				orderMap.put(header[7], order.getFormat());

				if (order.getFileSize() != null)
				{
					orderMap.put(header[8], acquisitionReportFileSize(order.getFileSize()));
				}
				else
				{
					orderMap.put(header[8], null);
				}
				orderMap.put(header[9], order.getTitleLengthReadableString());

				csvwriter.write(orderMap, header, processors);
			}

			StringBuilder statsString = new StringBuilder();
			statsString.append(String.format("No By File %d\n", stats.noFile));
			statsString.append(String.format("No By Tape %d\n", stats.noTape));
			statsString.append(String.format("%% By File %.2f\n", stats.perFile));
			statsString.append(String.format("%% By Tape %.2f\n", stats.perTape));

			csvwriter.flush();
			IOUtils.write(statsString.toString(), fileWriter);
		}
		catch (IOException e)
		{
			log.error("error writing report",e);
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
					log.error("IOException closing csv writer", e);
				}
			}
		}
		log.debug("<<<createCSV");
	}

	private CellProcessor[] getProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] { new Optional(), new Optional(), new Optional(), new Optional(),
				new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), new Optional() };
		return processors;
	}

	class OrderStatusStats
	{

		int noFile = 0;
		int noTape = 0;
		int total = 0;
		double perFile = 0;
		double perTape = 0;
	}

	public OrderStatusStats getStats(List<OrderStatus> orders)
	{

		OrderStatusStats stats = new OrderStatusStats();

		for(OrderStatus order : orders)
		{
			if (order.getComplete() != null &&
			    order.getAggregatorID() != null &&
			    order.getAggregatorID().equalsIgnoreCase("ruzz") &&
			    order.getAggregatorID().equalsIgnoreCase("DART") &&
			    order.getAggregatorID().equalsIgnoreCase("VizCapture"))
			{
				stats.noTape++;
			}
			else
			{
				stats.noFile++;
			}
			stats.total++;
		}

		log.debug("noFile: " + stats.noFile + " noTape: " + stats.noTape);

		if (stats.total > 0)
		{
			stats.perFile = (stats.noFile / stats.total) * 100;
			stats.perTape = (stats.noTape / stats.total) * 100;
		}

		log.debug("perFile: " + stats.perFile + " perTape: " + stats.perTape);
		return stats;
	}
}	
