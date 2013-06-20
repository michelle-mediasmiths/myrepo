package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.std.types.Framerate;
import com.mediasmiths.std.types.SampleCount;
import com.mediasmiths.std.types.Timecode;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ManualQAEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
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

public class ManualQARpt extends ReportUtils
{
	private static final transient Logger logger = Logger.getLogger(ManualQARpt.class);

	@Inject
	@Named("reportLoc")
	private String REPORT_LOC;

	@Inject
	private QueryAPI queryApi;


	public void writeManualQA(List<ManualQAEntity> manualQAs, DateTime startDate, DateTime endDate, String reportName)
	{
		logger.debug(">>>writeManualQA");
		ManualQAStats stats = getStats(manualQAs);
		createCsv(manualQAs, stats, reportName, startDate, endDate);
		logger.debug("<<<writeManualQA");
	}


	private ManualQAStats getStats(final List<ManualQAEntity> manualQAs)
	{
		ManualQAStats stats = new ManualQAStats();

		for (ManualQAEntity qa : manualQAs)
		{

			if (qa.getTaskStatus().contains("FINISHED"))
			{
				stats.total++;
			}
			if (qa.getTaskStatus().contains("FAILED"))//not certain this is right
			{
				stats.failed++;
			}
			if (qa.getReordered() != null && qa.getReordered())
			{
				stats.reordered++;
			}

			String previewStatus = qa.getPreviewStatus();

			if (previewStatus != null && (previewStatus.contains("fix") || previewStatus.contains("stitch")))
			{
				stats.requiresFixOrStitch++;
			}

			if (Boolean.TRUE.equals((qa.getHrPreview())))
			{
				stats.totalHR++;
			}

			if (qa.getEverEscalated())
			{

				final Interval timeEscalatedFor = qa.getTimeEscalatedFor();
				if (timeEscalatedFor != null)
				{
					stats.totalEscalatedTime += timeEscalatedFor.toDurationMillis();
					stats.escalatedWithTime++;
				}

				stats.escalated++;
			}
		}

		if (stats.totalEscalatedTime != 0)
		{
			stats.averageTimeEscalated = stats.totalEscalatedTime / stats.escalatedWithTime;
		}

		return stats;
	}


	private void createCsv(List<ManualQAEntity> manualqas, ManualQAStats stats, String reportName, DateTime start, DateTime end)
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
			                         "operator",
			                         "aggregatorID",
			                         "taskStatus",
			                         "previewStatus",
			                         "hrPreview",
			                         "hrPreviewRequested by",
			                         "escalated",
			                         "timeEscalated",
			                         "titleLength",
			                         "reordered"};
			final CellProcessor[] processors = getProcessor();
			csvwriter.writeHeader(header);

			final String startDate = start.toString(dateFormatter);
			final String endDate = end.toString(dateFormatter);
			final String dateRange = String.format("%s - %s", startDate, endDate);

			for (ManualQAEntity m : manualqas)

			{
				final Map<String, Object> mqaMap = new HashMap<String, Object>();
				mqaMap.put(header[0], dateRange);

				mqaMap.put(header[1], m.getAssetTitle());
				mqaMap.put(header[2], m.getMaterialid());
				List<String> channelsList = m.getChannelsList();
				if (channelsList != null)
				{
					mqaMap.put(header[3], StringUtils.join(channelsList, ';'));
				}
				else
				{
					mqaMap.put(header[3], null);
				}
				mqaMap.put(header[4], m.getOperator());
				mqaMap.put(header[5], m.getAggregator());
				mqaMap.put(header[6], m.getTaskStatus());
				mqaMap.put(header[7], m.getPreviewStatus());
				final Boolean hrPreview = m.getHrPreview();
				if (hrPreview != null && hrPreview)
				{
					mqaMap.put(header[8], "1");
				}
				else
				{
					mqaMap.put(header[8], "0");
				}
				mqaMap.put(header[9], m.getHrPreviewRequestedBy());

				final Boolean escalated = m.getEverEscalated();
				if (escalated != null && escalated)
				{
					mqaMap.put(header[10], "1");
				}
				else
				{
					mqaMap.put(header[10], "0");
				}

				Interval timeEscalated = null;

				if (m.getEverEscalated())
				{
					timeEscalated = m.getTimeEscalatedFor();
				}

				if (timeEscalated != null)
				{

					Period p = timeEscalated.toPeriod();
					String timeEscalatedString = getPeriodString(p);
					mqaMap.put(header[11], timeEscalatedString);
				}
				else
				{
					mqaMap.put(header[11], null);
				}

				final Integer titleLength = m.getTitleLength();
				if (titleLength != null)
				{
					try
					{
						Timecode t = Timecode.getInstance(new SampleCount(titleLength, Framerate.HZ_25));
						mqaMap.put(header[12], t.toSMPTEString());
					}
					catch (Exception e)
					{
						logger.error("Error producing timecode string for length " + titleLength, e);
						mqaMap.put(header[12], null);
					}
				}
				else
				{
					mqaMap.put(header[12], null);
				}

				Boolean reordered = m.getReordered();

				if (reordered != null && reordered)
				{
					mqaMap.put(header[13], "1");
				}
				else
				{
					mqaMap.put(header[13], "0");
				}
				csvwriter.write(mqaMap, header, processors);
			}

			StringBuilder statsString = new StringBuilder();
			statsString.append(String.format("Total QA'd %d\n", stats.total));
			statsString.append(String.format("Total Failed QA %d\n", stats.failed));
			statsString.append(String.format("Total Needs Reordered %d\n", stats.reordered));
			statsString.append(String.format("Total Escalated %d\n", stats.escalated));
			statsString.append(String.format("Titles requiring fix/stitch %d\n", stats.requiresFixOrStitch));
			statsString.append(String.format("Total HR preview %d\n", stats.totalHR));
			statsString.append(String.format("Total Proxy preview %d\n", stats.total - stats.totalHR));
			try
			{
				statsString.append(String.format("Average time Escalated %s\n",
				                                 getPeriodString(new Duration(stats.averageTimeEscalated).toPeriod())));
			}
			catch (Exception e)
			{
				logger.error("Error calculated average time escalated " + stats.averageTimeEscalated, e);
			}

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


	private String getPeriodString(final Period p)
	{
		return String.format("%02d:%02d:%02d:%02d",
						                                           p.getDays(),
						                                           p.getHours(),
						                                           p.getMinutes(),
						                                           p.getSeconds());
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
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional(),
		                                                       new Optional()};
	}


	class ManualQAStats
	{
		int total = 0;
		int failed = 0;
		int reordered = 0;
		long escalated = 0;
		long totalEscalatedTime = 0;
		long escalatedWithTime = 0;
		long averageTimeEscalated = 0;
		int requiresFixOrStitch=0;
		int totalHR=0;
	}
}
