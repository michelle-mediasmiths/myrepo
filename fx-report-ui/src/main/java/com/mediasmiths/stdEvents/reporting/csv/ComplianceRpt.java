package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ComplianceLogging;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
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

public class ComplianceRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(ComplianceRpt.class);

	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;


	public void writeCompliance(List<ComplianceLogging> tasks, DateTime startDate, DateTime endDate, String reportName)
	{
		ComplianceLoggingStats stats = getStats(tasks);
		createCsv(tasks, stats, reportName, startDate, endDate);
	}


	class ComplianceLoggingStats
	{

		int total = 0;
		Duration averageTime = new Duration(0);
	}


	private ComplianceLoggingStats getStats(List<ComplianceLogging> tasks)
	{

		ComplianceLoggingStats ret = new ComplianceLoggingStats();
		ret.total = tasks.size();
		int numfinished = 0;

		Duration totalduration = new Duration(0);

		for (ComplianceLogging task : tasks)
		{
			if (task.getComplete() != null && task.getComplete() && task.getDateCompleted() != null)
			{
				DateTime start = new DateTime(task.getTaskCreated());
				DateTime finished = new DateTime(task.getDateCompleted());

				if (finished.isAfter(start))
				{
					totalduration.withDurationAdded(new Duration(start, finished), 1);
					numfinished++;
				}
				else
				{
					logger.warn(String.format("finished date not after start date for a task %s (material %s), ignoring for average calculation",task.getTaskID(),task.getMaterialID()));
				}
			}
		}

		if (numfinished != 0)
		{
			long totaltimeMillis = totalduration.getMillis();
			long averageTimeMillis = totaltimeMillis / numfinished;
			logger.info(String.format("Title time %d numfinished %d averagetime %d",totaltimeMillis,numfinished,averageTimeMillis));

			ret.averageTime = new Duration(averageTimeMillis);
		}

		return ret;
	}


	private void createCsv(List<ComplianceLogging> tasks,
	                       ComplianceLoggingStats stats,
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
			                         "materialId",
			                         "channels",
			                         "taskStatus",
			                         "taskStart",
			                         "taskFinish",
			                         "externalCompliance"};
			final CellProcessor[] processors = getProcessor();
			csvwriter.writeHeader(header);

			final String dateRange = getDateRangeString(start, end);

			for (ComplianceLogging task : tasks)
			{
				final Map<String, Object> map = new HashMap<String, Object>();
				map.put(header[0], dateRange);

				putTitleAndChannels(header, map, task.getTitle(), 1, 3);

				map.put(header[2], task.getMaterialID());
				map.put(header[4], task.getTaskStatus());

				putFormattedDateInCSVMap(header, 5, map, task.getTaskCreated(), dateAndTimeFormat);

				if (task.getComplete() != null && task.getComplete())
				{
					putFormattedDateInCSVMap(header, 6, map, task.getDateCompleted(), dateAndTimeFormat);
				}
				else
				{
					map.put(header[6], null);
				}

				putPossibleNullBooleanInCSVMap(header, 7, map, task.getExternalCompliance());

				csvwriter.write(map, header, processors);
			}


			StringBuilder statsString = new StringBuilder();
			statsString.append(String.format("No. of titles %d\n", stats.total));
			final Period averageTime = stats.averageTime.toPeriod();
			statsString.append(String.format("Average logging completion time %02d:%02d:%02d:%02d\n",
			                                 averageTime.getDays(),
			                                 averageTime.getHours(),
			                                 averageTime.getMinutes(),
			                                 averageTime.getSeconds()));


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
