package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ExtendedPublishing;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
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

public class ExportRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(ExportRpt.class);

	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;

	@Inject
	private QueryAPIImpl queryApi;


	public void writeExport(List<ExtendedPublishing> events, DateTime startDate, DateTime endDate, String reportName)
	{
//		List<Export> exports = getReportList(events, startDate , endDate);
//
//		int compliance=0;
//		int captioning=0;
//		int publicity=0;
//
//		for (Export export : exports)
//		{
//			if (export.getExportType().equals("compliance"))
//			{
//				compliance ++;
//			}
//			else if (export.getExportType().equals("caption"))
//			{
//				captioning ++;
//			}
//			else if (export.getExportType().equals("classification"))
//			{
//				publicity ++;
//			}
//		}
//
//		exports.add(addStats("No. of Compliance", Integer.toString(compliance)));
//		exports.add(addStats("No. of Captioning", Integer.toString(captioning)));
//		exports.add(addStats("No. of Publicity", Integer.toString(publicity)));
//

		ExtendedPublishingStats stats = getStats(events);
		createCsv(events, stats, reportName, startDate, endDate);
	}


	private ExtendedPublishingStats getStats(final List<ExtendedPublishing> events)
	{
		ExtendedPublishingStats ret = new ExtendedPublishingStats();
		for (ExtendedPublishing task : events)
		{

			final String exportType = task.getExportType();

			if ("Compliance Proxy".equals(exportType))
			{
				ret.compliance++;
			}
			else if ("Publicity Proxy".equals(exportType))
			{
				ret.publicity++;
			}
			else if ("Caption Proxy".equals(exportType))
			{
				ret.captions++;
			}
		}

		return ret;
	}


	class ExtendedPublishingStats
	{
		int compliance;
		int captions;
		int publicity;
	}


	private void createCsv(List<ExtendedPublishing> tasks,
	                       ExtendedPublishingStats stats,
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
			final String[] header = {"dateRange",
			                         "title",
			                         "materialID",
			                         "channels",
			                         "taskStatus",
			                         "exportType",
			                         "titleLength",
			                         "requestedBy"};
			final CellProcessor[] processors = getProcessor();
			csvwriter.writeHeader(header);

			final String startDate = start.toString(dateFormatter);
			final String endDate = end.toString(dateFormatter);
			final String dateRange = String.format("%s - %s", startDate, endDate);

			for (ExtendedPublishing task : tasks)
			{
				final Map<String, Object> map = new HashMap<String, Object>();
				map.put(header[0], dateRange);

				final OrderStatus orderStatus = task.getOrderStatus();

				Title title = null;
				if (orderStatus != null)
				{
					title = orderStatus.getTitle();
				}

				if (title != null)
				{
					map.put(header[1], title.getTitle());
					putChannelListToCSVMap(header, 3, map, title.getChannels());
				}
				else
				{
					map.put(header[1], null);
					map.put(header[3], null);
				}

				map.put(header[2], task.getMaterialID());
				map.put(header[4], task.getTaskStatus());
				map.put(header[5], task.getExportType());

				if (orderStatus != null)
				{
					map.put(header[6], orderStatus.getTitleLengthReadableString());
				}
				else
				{
					map.put(header[6], null);
				}

				map.put(header[7], task.getRequestedBy());
				csvwriter.write(map, header, processors);
			}

			StringBuilder statsString = new StringBuilder();
			statsString.append(String.format("No. of Titles Exported Compliance %d\n", stats.compliance));
			statsString.append(String.format("No. of Titles Exported Publicity %d\n", stats.publicity));
			statsString.append(String.format("No. of Titles Exported Captioning %d\n", stats.captions));

			csvwriter.flush();
			IOUtils.write(statsString.toString(), fileWriter);
		}

		catch (IOException e)
		{
			logger.error("error writing report", e);
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
					logger.error("IOException closing csv writer", e);
				}
			}
		}
	}


	private CellProcessor[] getProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[]{new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional()};
		return processors;
	}
}

