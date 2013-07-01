package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.FilePickupDetails;
import com.mediasmiths.foxtel.ip.common.events.PickupNotification;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class WatchFolderRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(WatchFolderRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPIImpl queryApi;
	
	public void writeWatchFolder(List<EventEntity> events, DateTime startDate, DateTime endDate, String reportName)
	{
		List<FilePickupDetails> files = getReportList(events, startDate, endDate);
		WatchFolderStats stats = getStats(files);
		createCsv(files, stats,reportName,startDate,endDate);
	}


	protected WatchFolderStats getStats(final List<FilePickupDetails> files)
	{
		WatchFolderStats ret = new WatchFolderStats();
		SortedMap<String, WatchFolderAverage> averages = new TreeMap();

		for (FilePickupDetails f : files)
		{

			final String filename = f.getFilename();
			final String ext = FilenameUtils.getExtension(filename);
			final String aggregator = f.getAggregator();
			final Long start = f.getTimeDiscovered();
			final Long end = f.getTimeProcessed();

			if(aggregator==null){
				logger.info("null aggregator in entry for file "+filename+" and path "+f.getFilePath());
				continue;
			}

			if (end > start && !(end == 0 || start == 0))
			{ //dont include entries that appeared processed before discovered or uninitialised

				final Long time = end - start;

				WatchFolderAverage watchFolderAverage = averages.get(aggregator);
				if (watchFolderAverage == null)
				{
					watchFolderAverage = new WatchFolderAverage();
					averages.put(aggregator, watchFolderAverage);
				}
				logger.trace(String.format("ext %s time %d",ext,time));
				watchFolderAverage.addEntry(ext, time);
			}
			else
			{
				logger.warn("Unexpected start or end date for file " + filename);
			}
		}

		ret.statsString = buildStatsString(averages);
		return ret;
	}

	private String buildStatsString(final SortedMap<String, WatchFolderAverage> averages)
	{
		StringBuilder sb = new StringBuilder();

		for (String aggregator : averages.keySet())
		{
			WatchFolderAverage wfa = averages.get(aggregator);
			long meanforFolder = wfa.getMean();
			double sdForFolder = wfa.getStandardDeviation();
			buildWatchFolderStatsLine(sb, aggregator, "all", wfa.high, wfa.low, meanforFolder,sdForFolder);

			final SortedMap<String, ExtAverage> extensionAverages = wfa.getExtensionAverages();
			if (extensionAverages.keySet().size() > 1)
			{

				for (String ext : extensionAverages.keySet())
				{
					ExtAverage extAverage = extensionAverages.get(ext);
					long meanForExt = extAverage.getMean();
					double sdForExt = extAverage.getStandardDeviation();
					buildWatchFolderStatsLine(sb, aggregator, ext, extAverage.high, extAverage.low, meanForExt,sdForExt);
				}
			}
		}

		String ret = sb.toString();

		logger.debug("Stats string " + ret);
		return ret;
	}


	private void buildWatchFolderStatsLine(final StringBuilder sb,
	                                       final String aggregator,
	                                       final String ext,
	                                       final long high,
	                                       final long low,
	                                       final long meanForExt,
	                                       final double standardDeviationForExt)
	{
		sb.append("Folder: ")
		  .append(aggregator)
		  .append(" ")
		  .append(ext)
		  .append(" high: ")
		  .append(getHHMMSSmmm(new Period(high)))
		  .append(" low: ")
		  .append(getHHMMSSmmm(new Period(low)))
		  .append(" average: ")
		  .append(getHHMMSSmmm(new Period(meanForExt)))
		  .append(" sd: ")
		  .append(getHHMMSSmmm(new Period((long)standardDeviationForExt)))
		  .append("\r\n");
	}


	private class FileAverage
	{

		long numberInFolder;
		long totalTime;

		long high = Long.MIN_VALUE;
		long low = Long.MAX_VALUE;
		StandardDeviation standardDeviation = new StandardDeviation();


		public void addEntry(String ext, long time)
		{

			numberInFolder++;
			totalTime += time;

			if (time < low)
			{
				low = time;
			}

			if (time > high)
			{
				high = time;
			}

			standardDeviation.increment((double) time);
		}


		public long getMean()
		{
			logger.debug(String.format("number %d totalTime %s", numberInFolder, totalTime));

			if (numberInFolder != 0)
			{
				return totalTime / numberInFolder;
			}
			else
			{
				return 0;
			}
		}

		public double getStandardDeviation(){
			return standardDeviation.getResult();
		}
	}


	private class WatchFolderAverage extends FileAverage
	{

		private SortedMap<String, ExtAverage> extensionAverages = new TreeMap();


		public void addEntry(String ext, long time)
		{

			ExtAverage extAverage = getExtensionAverages().get(ext);

			if (extAverage == null)
			{
				extAverage = new ExtAverage();
				getExtensionAverages().put(ext.toLowerCase(), extAverage);
			}
			extAverage.addEntry(ext, time);
			super.addEntry(ext, time);
		}


		public SortedMap<String, ExtAverage> getExtensionAverages()
		{
			return extensionAverages;
		}
	}

	private class ExtAverage extends FileAverage
	{

	}

	class WatchFolderStats
	{
		String statsString;
	}


	public List<FilePickupDetails> getReportList(List<EventEntity> events, DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>getReportList");
		
		List<FilePickupDetails> files = new ArrayList<FilePickupDetails>();
		
		String startF = startDate.toString(dateOnlyFormatString);
		String endF = endDate.toString(dateOnlyFormatString);
		
		for (EventEntity event : events) 
		{
			try
			{
				PickupNotification ppn = (PickupNotification) unmarshallEvent(event);

				final List<FilePickupDetails> details = ppn.getDetails();
				for(FilePickupDetails file : details){
					files.add(file);
				}
			}
			catch (Exception cce)
			{
				logger.warn("Error unmarshalling event", cce);
			}
		}
		
		logger.debug("<<<getReportList");
		return files;
	}
	
	private void createCsv (List<FilePickupDetails> files, WatchFolderStats stats,String reportName,DateTime start,
	                        DateTime end)
	{
		ICsvMapWriter csvwriter = null;
		try
		{
			logger.info("reportName: " + reportName);
			FileWriter fileWriter = new FileWriter(REPORT_LOC + reportName + ".csv");
			csvwriter = new CsvMapWriter(fileWriter, CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "filename", "timeDiscovered", "timeCompleted", "aggregator"};
			final CellProcessor[] processors = getWatchFolderProcessor();
			csvwriter.writeHeader(header);

			final String dateRange = getDateRangeString(start, end);

			for (FilePickupDetails file : files) {
				final Map<String, Object> map = new HashMap<String, Object>();
				map.put(header[0], dateRange);
				map.put(header[1],file.getFilename());
				putFormattedDateInCSVMap(header, 2, map, file.getTimeDiscovered(), dateAndTimeFormat);
				putFormattedDateInCSVMap(header, 3, map, file.getTimeProcessed(), dateAndTimeFormat);
				map.put(header[4], file.getAggregator());

				csvwriter.write(map, header, processors);
			}

			csvwriter.flush();
			IOUtils.write(stats.statsString, fileWriter);
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
	
	public CellProcessor[] getWatchFolderProcessor() 
	{
		final CellProcessor[] processors = new CellProcessor[] {
				new Optional(),
				new Optional(), 
				new Optional(),
				new Optional(),
				new Optional()
		};
		return processors;
	}
}
