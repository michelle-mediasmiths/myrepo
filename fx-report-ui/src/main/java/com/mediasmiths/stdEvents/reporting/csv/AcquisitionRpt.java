package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.commons.io.FileUtils;
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

		OrderStatusStats stats = getStats(orders);

		//Retrieving file size in GB

		for (OrderStatus order : orders)
		{
			long fileSize;

			if (order!= null && order.getFileSize() != null)
			{
				fileSize = order.getFileSize();
			}
			else
			{
				fileSize = order.getFileSize();
			}
			if (fileSize!= 0)
			{
				order.setFileSize(acquisitionReportFileSize(fileSize));
			}

		}

		createCSV(orders, stats, reportName, startDate, endDate);
		log.debug("<<<writeAcquisitionDelivery");
	}

	public static Long acquisitionReportFileSize(long fileSize)
	{
		long displaySize;

		if (fileSize >= FileUtils.ONE_GB)
		{
			displaySize = Long.valueOf(fileSize/ FileUtils.ONE_GB);

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

			displaySize = Long.valueOf(twoDForm.format(fileDisplaySize));
		}
		return displaySize;
	}
//

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

				if (order.getComplete() != null)
				{
					orderMap.put(header[0], dateRange);
					if (order.getTitle() != null && order.getComplete() != null)
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
					orderMap.put(header[4], order.getAggregatorID());

					String aggregatorID = order.getAggregatorID();
					boolean isFromTape = false;
					if (aggregatorID != null)
					{
						if (aggregatorID.toLowerCase().equals("ruzz")
						    || aggregatorID.toLowerCase().equals("vizcapture")
						    || aggregatorID.toLowerCase().equals("dart"))
						{
							isFromTape = true;
						}
					}
					if (order.isTapeDelivery() || isFromTape)
					{
						order.setTapeDel("1");
						order.setFileDel("0");
					}
					else if (order.isFileDelivery())
					{
						order.setFileDel("1");
						order.setTapeDel("0");
					}

					orderMap.put(header[5], order.getTapeDel());
					orderMap.put(header[6], order.getFileDel());
					orderMap.put(header[6], order.getComplete());
					orderMap.put(header[7], order.getFormat());
					orderMap.put(header[8], order.getFileSize());
					orderMap.put(header[9], order.getTitleLength());
				}

				csvwriter.write(orderMap, header, processors);
			}

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

	private Acquisition addStats(String name, String value)
	{
		Acquisition acq = new Acquisition();
		acq.setTitle(name);
		acq.setMaterialID(value);
		return acq;
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
			if (order.getAggregatorID().equalsIgnoreCase("ruzz") && order.getAggregatorID().equalsIgnoreCase("DART") && order.getAggregatorID().equalsIgnoreCase("VizCapture"))
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
