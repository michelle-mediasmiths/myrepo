package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.tc.priorities.TranscodeJobType;
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
		ExtendedPublishingStats stats = getStats(events);
		createCsv(events, stats, reportName, startDate, endDate);
	}


	private ExtendedPublishingStats getStats(final List<ExtendedPublishing> events)
	{
		ExtendedPublishingStats ret = new ExtendedPublishingStats();
		for (ExtendedPublishing task : events)
		{

			final String exportType = task.getExportType();

			if (TranscodeJobType.COMPLIANCE_PROXY.getText().equals(exportType))
			{
				ret.compliance++;
			}
			else if (TranscodeJobType.PUBLICITY_PROXY.getText().equals(exportType))
			{
				ret.publicity++;
			}
			else if (TranscodeJobType.CAPTION_PROXY.getText().equals(exportType))
			{
				ret.captions++;
			}
			else
			{
				logger.warn("Empty or unknown export type " + exportType);
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

			final String startDate = start.toString(dateOnlyFormatString);
			final String endDate = end.toString(dateOnlyFormatString);
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

				putTitleAndChannels(header, map, title, 1, 3);

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
			statsString.append(String.format("No. of Titles Exported ComplianceLogging %d\n", stats.compliance));
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

