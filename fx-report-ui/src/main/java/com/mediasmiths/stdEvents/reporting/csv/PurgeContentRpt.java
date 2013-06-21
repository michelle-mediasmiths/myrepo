package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Purge;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurgeContentRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(PurgeContentRpt.class);

	private static final String formatString = "dd-MM-yyyy hh:mm:ss";
	private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern(formatString);
	private static final DateFormat df = new SimpleDateFormat(formatString);

	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;

	@Inject
	private QueryAPIImpl queryApi;


	public void writePurgeTitles(List<Purge> purgeReportRecords, DateTime startDate, DateTime endDate, String reportName)
	{
		logger.info("Purge content report");
		PurgeContentStats stats = getStats(purgeReportRecords, startDate, endDate);
		createCsv(purgeReportRecords, stats, reportName, startDate, endDate);
	}


	class PurgeContentStats
	{
		int dueToExpire = 0;
		int numProtected = 0;
		int numExtended = 0;
		int numPurged = 0;
	}


	private PurgeContentStats getStats(final List<Purge> purgeReportRecords, final DateTime startDate, final DateTime endDate)
	{

		PurgeContentStats stats = new PurgeContentStats();

		final DateTime threeDays = new DateTime().plusDays(3);

		for (Purge p : purgeReportRecords)
		{
			if (isDateInRange(p.getDateExpires(), startDate, threeDays))
			{
				stats.dueToExpire++;
			}

			if (isDateInRange(p.getDateProtected(), startDate, endDate))
			{
				stats.numProtected++;
			}

			if (isDateInRange(p.getDateExtended(), startDate, endDate))
			{
				stats.numExtended++;
			}

			if (isDateInRange(p.getDatePurged(), startDate, endDate))
			{
				stats.numPurged++;
			}
		}
		return stats;
	}


	private boolean isDateInRange(Date date, DateTime rangeStart, DateTime rangeEnd)
	{
		if (date != null)
		{
			DateTime dateTime = new DateTime(date);

			if (dateTime.isAfter(rangeStart) && dateTime.isBefore(rangeEnd))
			{
				logger.debug(String.format("Date %s is within range start %s end %s", dateTime, rangeStart, rangeEnd));
				return true;
			}
		}
		return false;
	}


	private void createCsv(List<Purge> purgeReportRecords,
	                       PurgeContentStats stats,
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
			                         "entityType",
			                         "title",
			                         "materialID",
			                         "channels",
			                         "protected",
			                         "extended",
			                         "purged",
			                         "expires"};
			final CellProcessor[] processors = getProcessor();
			csvwriter.writeHeader(header);

			final String startDate = start.toString(dateFormatter);
			final String endDate = end.toString(dateFormatter);
			final String dateRange = String.format("%s - %s", startDate, endDate);

			for (Purge record : purgeReportRecords)
			{
				final Map<String, Object> map = new HashMap<String, Object>();
				map.put(header[0], dateRange);
				map.put(header[1], record.getAssetType());
				map.put(header[3], record.getHouseID());

				final Title title = record.getTitle();
				if (title != null)
				{
					map.put(header[2], title.getTitle());
					putChannelListToCSVMap(header,4,map,title.getChannels());
				}
				else
				{
					logger.debug(String.format("title of entry for %s is null", record.getHouseID()));
					map.put(header[2], null);
					map.put(header[4], null);
				}

				putPossibleNullBooleanInCSVMap(header, 5, map, record.getProtected());
				putPossibleNullBooleanInCSVMap(header, 6, map, record.getExtended());
				putPossibleNullBooleanInCSVMap(header, 7, map, record.getPurged());
				putFormattedDateInCSVMap(header,8,map, record.getDateExpires(), df);

				csvwriter.write(map, header, processors);
			}

			StringBuilder statsString = new StringBuilder();
			statsString.append(String.format("Total Due to Expire %d\n", stats.dueToExpire));
			statsString.append(String.format("Amount Protected in date range %d\n", stats.numProtected));
			statsString.append(String.format("Amount Extended in date range %d\n", stats.numExtended));
			statsString.append(String.format("Amount Purged in date range %d\n", stats.numPurged));


			csvwriter.flush();
			IOUtils.write(statsString.toString(), fileWriter);
		}
		catch (IOException e)
		{
			logger.error("error writing report",e);
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
		return new CellProcessor[]{new Optional(),
		                           new Optional(),
		                           new Optional(),
		                           new Optional(),
		                           new Optional(),
		                           new Optional(),
		                           new Optional(),
		                           new Optional(),
		                           new Optional()};
	}
}
