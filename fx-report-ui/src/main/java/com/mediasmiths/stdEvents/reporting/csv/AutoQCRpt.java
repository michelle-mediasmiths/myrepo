package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AutoQC;
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

public class AutoQCRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(AutoQCRpt.class);

	// Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;

	@Inject
	private QueryAPIImpl queryApi;

	public void writeAutoQc(List<AutoQC> events, DateTime start, DateTime end, String reportName)
	{
		logger.debug(">>>writeAutoQC");
		logger.debug("List size: " + events.size());

		AutoQCStats stats = getStats(events,start,end);
		createCsv(events,stats, reportName, start, end);
	}

	private AutoQCStats getStats(List<AutoQC> events, DateTime start, DateTime end)
	{
		AutoQCStats stats = new AutoQCStats();
		
		for (AutoQC autoQC : events)
		{
			if(autoQC.getTaskFinishedTime() != null){
				stats.total++;
			}
			
			if(autoQC.getTaskStatus() != null && autoQC.getTaskStatus().equals("FINISHED_FAILED"));{
				stats.failcount++;
			}
			
			if(autoQC.getOverride() != null && autoQC.getOverride().booleanValue()==true){
				stats.numberOverriden++;
			}
			
			//TODO: averages???
		}
		
		return stats;
	}

	private void createCsv(List<AutoQC> events, AutoQCStats stats, String reportName, DateTime start, DateTime end)
	{
		ICsvMapWriter csvwriter = null;
		try
		{
			logger.info("reportName: " + reportName);
			FileWriter fileWriter = new FileWriter(REPORT_LOC + reportName + ".csv");
			csvwriter = new CsvMapWriter(fileWriter, CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = { "dateRange", "title", "materialID", "channels", "contentType", "operator", "taskStatus",
					"qcStatus", "taskStart", "taskFinish", "warningTime", "manualOverride", "failureParameter", "titleLength" };


			csvwriter.writeHeader(header);

			final String startDate = start.toString(dateOnlyFormatString);
			final String endDate = end.toString(dateOnlyFormatString);
			final String dateRange = String.format("%s - %s", startDate, endDate);

			for (AutoQC a : events)
			{
				final Map<String, Object> aqcMap = new HashMap<String, Object>();

				aqcMap.put(header[0], dateRange);
				aqcMap.put(header[1], a.getAssetTitle());
				aqcMap.put(header[2], a.getMaterialid());
				if (a.getOrderStatus() != null && a.getOrderStatus().getTitle() != null)
				{
					putChannelListToCSVMap(header, 3, aqcMap, a.getOrderStatus().getTitle().getChannels());
				}
				else
				{
					aqcMap.put(header[3], null);
				}

				aqcMap.put(header[4], contentTypeToHumanString(a.getContentType()));
				aqcMap.put(header[5], a.getOperator());
				aqcMap.put(header[6], a.getTaskStatus());
				aqcMap.put(header[7], a.getQcStatus());

				putFormattedDateInCSVMap(header,8,aqcMap, a.getCreatedTime(), dateAndTimeFormat);
				putFormattedDateInCSVMap(header,9,aqcMap, a.getTaskFinishedTime(), dateAndTimeFormat);
				putFormattedDateInCSVMap(header,10,aqcMap, a.getWarningTime(), dateAndTimeFormat);

				if (a.getOverride() != null && a.getOverride().equals(Boolean.TRUE))
				{
					aqcMap.put(header[11], "1");
				}
				else
				{
					aqcMap.put(header[11], null);
				}
				aqcMap.put(header[12], a.getFailureParameter());

				if (a.getOrderStatus() != null)
				{
					aqcMap.put(header[13],a.getOrderStatus().getTitleLengthReadableString());
				}
				else
				{
					aqcMap.put(header[13], null);
				}

				csvwriter.write(aqcMap, header, getProcessor());
			}
			
			StringBuilder statsString = new StringBuilder();
			statsString.append(String.format("Total QCed %d\n", stats.total));
			statsString.append(String.format("Total Failed %d\n", stats.failcount));
			statsString.append(String.format("Total Overriden %d\n", stats.numberOverriden));
			//TODO: averages??!
			
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
					logger.error("IOException closing csv writer",e);
				}
			}
		}
	}

	private CellProcessor[] getProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] { new Optional(), new Optional(), new Optional(), new Optional(),
				new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), new Optional(),
				new Optional(), new Optional(), new Optional() };
		return processors;
	}

	class AutoQCStats
	{
		public int total= 0;
		public int averageConcurrent = 0;
		public int totalConcurrent = 0;
		public int failcount=0;
		public int numberOverriden = 0;
	}
}
