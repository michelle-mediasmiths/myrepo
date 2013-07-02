package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.TranscodeJob;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.MutableDateTime;
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
import java.util.Set;
import java.util.TreeSet;

public class TranscoderLoadRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(TranscoderLoadRpt.class);

	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;

	@Inject
	private QueryAPIImpl queryApi;


	public void writeTranscoderLoad(List<TranscodeJob> jobs, DateTime startDate, DateTime endDate, String reportName)
	{
		logger.debug(">>>writeTranscoderLoad");
		TranscodeStats stats = getStats(jobs);
		createCsv(jobs, stats, startDate, endDate, reportName);
	}


	private void createCsv(final List<TranscodeJob> jobs,
	                       final TranscodeStats stats,
	                       final DateTime start,
	                       final DateTime end,
	                       final String reportName)
	{
		ICsvMapWriter csvwriter = null;
		try
		{
			logger.info("reportName: " + reportName);
			FileWriter fileWriter = new FileWriter(REPORT_LOC + reportName + ".csv");
			csvwriter = new CsvMapWriter(fileWriter, CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange",
			                         "Transcode Job Created",
			                         "Transcode Job Started",
			                         "Transcode Job Updated",
			                         "Source Format",
			                         "Destination Format",
			                         "Status"};

			csvwriter.writeHeader(header);

			final String startDate = start.toString(dateOnlyFormatString);
			final String endDate = end.toString(dateOnlyFormatString);
			final String dateRange = String.format("%s - %s", startDate, endDate);

			for (TranscodeJob job : jobs)
			{
				final Map<String, Object> map = new HashMap<String, Object>();

				map.put(header[0], dateRange);

				putFormattedDateInCSVMap(header, 1, map, job.getCreated(), dateAndTimeFormat);
				putFormattedDateInCSVMap(header, 2, map, job.getStarted(), dateAndTimeFormat);
				putFormattedDateInCSVMap(header, 3, map, job.getUpdated(), dateAndTimeFormat);


				map.put(header[4], job.getSourceFormat());
				map.put(header[5], job.getDestinationFormat());
				map.put(header[6], job.getStatus());

				csvwriter.write(map, header, getProcessor());
			}

			StringBuilder statsString = new StringBuilder();
			statsString.append(String.format("Total Submitted %d\n", stats.totalSubmitted));
			statsString.append(String.format("Total Completed %d\n", stats.totalSuccessful));
			statsString.append(String.format("Total Failed %d\n", stats.totalFailures));
			statsString.append(String.format("Average transcodes per day %s  (standard deviation %s)\n", stats.averageTranscodesPerDay, stats.averagePerDayStandardDeviation));
			statsString.append(String.format("Max concurrent transcodes %d\n", stats.maxConcurrentTranscodes));
			statsString.append(String.format("Average queued time %s\n",getDDHHMMSSStringForMillis(stats.averageQueuedTime)));
			statsString.append(String.format("Average transcode time %s\n",getDDHHMMSSStringForMillis(stats.averageTranscodeTime)));
			statsString.append(String.format("Number on high priority (>6) %d\n", stats.numberOnHighPriority));

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
					logger.error("IOException closing csv writer", e);
				}
			}
		}
	}


	private TranscodeStats getStats(final List<TranscodeJob> jobs)
	{
		TranscodeStats ret = new TranscodeStats();

		long totalQueuedTime = 0l;
		int numberOfJobsCountedForQueueTime = 0;

		long totalTranscodeTime = 0l;
		int numberOfJobsCountedForTranscodetime = 0;

		for (TranscodeJob j : jobs)
		{
			ret.totalSubmitted++;
			if (j.isFailed())
			{
				ret.totalFailures++;
			}

			if (j.isSuccess())
			{
				ret.totalSuccessful++;

				Long queuedTime = j.getQueuedTime();

				if (queuedTime != null)
				{
					numberOfJobsCountedForQueueTime++;
					totalQueuedTime += queuedTime;
				}

				Long transcodeTime = j.getTranscodeTime();

				if (transcodeTime != null)
				{
					numberOfJobsCountedForTranscodetime++;
					totalTranscodeTime += transcodeTime;
				}
			}

			if (j.getPriority() != null && j.getPriority() > 6)
			{
				ret.numberOnHighPriority++;
			}
		}

		final TranscodeStatsMaxConcurrentAndAveragePerDay maxConcurrentAndAveragePerDayTranscodes = getMaxConcurrentAndAveragePerDayTranscodes(jobs);
		ret.maxConcurrentTranscodes =maxConcurrentAndAveragePerDayTranscodes.maxConcurrentTranscodes;
		ret.averageTranscodesPerDay=maxConcurrentAndAveragePerDayTranscodes.averagePerDay;
		ret.averagePerDayStandardDeviation=maxConcurrentAndAveragePerDayTranscodes.sdPerDay;

		if (numberOfJobsCountedForQueueTime != 0)
		{
			ret.averageQueuedTime = totalQueuedTime / numberOfJobsCountedForQueueTime;
		}

		if (numberOfJobsCountedForTranscodetime != 0)
		{
			logger.info("Using overly simplistic average transcode (time of total time spent transcoding)/(number of jobs)");
			ret.averageTranscodeTime = totalTranscodeTime / numberOfJobsCountedForTranscodetime;
		}

		return ret;
	}


	//two stats calculated with the one function so save an extra sort + loop
	protected TranscodeStatsMaxConcurrentAndAveragePerDay getMaxConcurrentAndAveragePerDayTranscodes(final List<TranscodeJob> jobs)
	{
		TranscodeStatsMaxConcurrentAndAveragePerDay ret = new TranscodeStatsMaxConcurrentAndAveragePerDay();

		//forms an ordered set of start and stop times then iterates through that list, increasing a counter when a 'start' time is encountered and decreasing when a 'stop' is encountered.
		Set<TranscodeStartOrStop> times = new TreeSet<TranscodeStartOrStop>();

		for (TranscodeJob job : jobs)
		{
			if (job.isSuccess())
			{
				DateTime created = new DateTime(job.getCreated());
				DateTime updated = new DateTime(job.getUpdated());

				if (created.isBefore(updated))
				{
					times.add(new TranscodeStartOrStop(created, true));
					times.add(new TranscodeStartOrStop(updated, false));
				}
			}
		}

		int maxConcurrent = 0;
		int currentConcurrent = 0;

		//Map day -> count in that day
		Map<Integer, Integer> numberCreatedPerDay = new HashMap<Integer, Integer>();

		//loop over the times
		for (TranscodeStartOrStop t : times)
		{
			if (t.start)
			{

				//if the job is starting, record a job starting against that day
				Integer day = daysSinceEpoch(t.time);
				if (numberCreatedPerDay.containsKey(day))
				{
					numberCreatedPerDay.put(day, numberCreatedPerDay.get(day) + 1);
				}
				else
				{
					numberCreatedPerDay.put(day, Integer.valueOf(1));
				}

				currentConcurrent++;
				if (currentConcurrent > maxConcurrent)
				{
					maxConcurrent = currentConcurrent;
				}
			}
			else
			{
				currentConcurrent--;
			}
		}

		ret.maxConcurrentTranscodes= maxConcurrent;


		try
		{
			//calculate mean transcodes per day + standard deviation
			StandardDeviation standardDeviation = new StandardDeviation();

			Integer totalByDay = 0;

			for (Integer day : numberCreatedPerDay.keySet())
			{
				standardDeviation.increment((double) numberCreatedPerDay.get(day));
				totalByDay += numberCreatedPerDay.get(day);
			}

			if (numberCreatedPerDay.keySet().size() != 0)
			{
				ret.averagePerDay = totalByDay / numberCreatedPerDay.keySet().size();
				ret.sdPerDay = standardDeviation.getResult();
			}
		}
		catch (Exception e)
		{
			logger.error("Error calculating average transcodes per day", e);
		}

		return ret;
	}

	private Integer daysSinceEpoch(DateTime d){
		MutableDateTime epoch = new MutableDateTime();
		epoch.setDate(0); //Set to Epoch time
		Days days = Days.daysBetween(epoch, d);
		return days.getDays();
	}

	class TranscodeStartOrStop implements Comparable
	{

		final DateTime time;
		final boolean start;


		public TranscodeStartOrStop(DateTime time, boolean start)
		{
			this.time = time;
			this.start = start;
		}


		@Override
		public int compareTo(final Object o)
		{
			if (o instanceof TranscodeStartOrStop)
			{
				return time.compareTo(((TranscodeStartOrStop) o).time);
			}
			else
			{
				return 0;
			}
		}

		@Override
		public String toString()
		{
			return "TranscodeStartOrStop{" +
			       "time=" + time +
			       ", start=" + start +
			       '}';
		}

	}

	//this class is just for returning 3 related values from one method
	class TranscodeStatsMaxConcurrentAndAveragePerDay{
		int maxConcurrentTranscodes;
		int averagePerDay;
		double sdPerDay;
	}

	class TranscodeStats
	{
		int totalSubmitted;
		int totalSuccessful;
		int totalFailures;

		int averageTranscodesPerDay;
		double averagePerDayStandardDeviation;
		int maxConcurrentTranscodes;

		long averageQueuedTime;
		long averageTranscodeTime;
		int numberOnHighPriority;
	}


	public CellProcessor[] getProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[]{new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional()};
		return processors;
	}
}
