package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.DiskUsageEvent;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiskUsageRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(DiskUsageRpt.class);

	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;

	@Inject
	private QueryAPIImpl queryApi;

	private final JAXBSerialiser serializer = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);


	public void writeDiskUsage(List<EventEntity> events, DateTime startDate, DateTime endDate, String reportName)
	{
		List<DiskUsageReportEntry> reports = getReportList(events, startDate, endDate);
		createCsv(reports, reportName);
	}


	public List<DiskUsageReportEntry> getReportList(List<EventEntity> events, DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>getReportList");
		List<DiskUsageReportEntry> ret = new ArrayList<DiskUsageReportEntry>();
		Set<String> dateAndChannels = new HashSet<String>();

		for (EventEntity event : events)
		{
			DiskUsageEvent diskUsageEvent = (DiskUsageEvent) serializer.deserialise(event.getPayload());

			if (diskUsageEvent != null)
			{
				DiskUsageReportEntry entry = new DiskUsageReportEntry();
				entry.setDate(new DateTime(event.getTime()));
				entry.setChannel(diskUsageEvent.getChannel());
				entry.setHrSize(getFormattedBytesToGBOrPassedString(diskUsageEvent.getHrSize()));
				entry.setLrSize(getFormattedBytesToGBOrPassedString(diskUsageEvent.getLrSize()));
				entry.setOthersSize(getFormattedBytesToGBOrPassedString(diskUsageEvent.getOthersSize()));
				entry.setTotalSize(getFormattedBytesToGBOrPassedString(diskUsageEvent.getTotalSize()));
				entry.setTsmSize(getFormattedBytesToGBOrPassedString(diskUsageEvent.getTsmSize()));

				//during deployments + restarts there may be an additional set of disk usage events generated as the report, the following makes sure that only one row for a date + channel combination is output
				String dateString = new DateTime(event.getTime()).toString(dateOnlyFormatter);
				String dateAndChannelString = dateString + " " + entry.getChannel();
				if (!dateAndChannels.contains(dateAndChannelString))
				{
					dateAndChannels.add(dateAndChannelString);
					ret.add(entry);
				}
				else
				{
					logger.info("Ignoring duplicate entry for  " + dateAndChannelString);
				}
			}
			else
			{
				logger.warn("Null report after unmarshalling event : " + event.toString());
			}
		}
		logger.debug("<<<getReportList");


		Collections.sort(ret, new Comparator<DiskUsageReportEntry>()
		{

			@Override
			public int compare(final DiskUsageReportEntry o1, final DiskUsageReportEntry o2)
			{
				int value1 = o1.getDate().compareTo(o2.getDate()); //sort first by date
				if (value1 == 0)
				{
					return o1.getChannel().compareTo(o2.getChannel()); //then by channel
				}

				return value1;
			}
		});

		return ret;
	}


	private String getFormattedBytesToGBOrPassedString(final String s)
	{
		try
		{
			Double d = Double.parseDouble(s);
			double gb = ((Double) d) / 1000000000.0d;

			return String.format(" %.2f", gb);
		}
		catch (NumberFormatException e)
		{
			logger.info("Could not parse string " + s + " as a double, disk usage event may be old");
			return s;
		}
	}


	private void createCsv(List<DiskUsageReportEntry> entries, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try
		{
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"date", "channel", "hrSize", "tsmSize", "lrSize", "othersSize", "totalSize"};
			final CellProcessor[] processors = getProcessors();
			beanWriter.writeHeader(header);

			for (DiskUsageReportEntry entry : entries)
			{
				beanWriter.write(entry, header, processors);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (beanWriter != null)
			{
				try
				{
					beanWriter.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}


	private static CellProcessor[] getProcessors()
	{
		final CellProcessor[] processors = new CellProcessor[]{new FormatDateTimeProcessor(), //date
		                                                       new NotNull(), // channel
		                                                       new Optional(), // HR Size
		                                                       new Optional(), // TSM Size
		                                                       new Optional(), // LR Size
		                                                       new Optional(), // Others Size
		                                                       new Optional(), // Total Size
		};

		return processors;
	}


	private static class FormatDateTimeProcessor extends CellProcessorAdaptor
	{
		public FormatDateTimeProcessor()
		{
			super();
		}


		public FormatDateTimeProcessor(CellProcessor next)
		{
			super(next);
		}


		@Override
		public Object execute(final Object value, final CsvContext context)
		{
			if (value == null)
			{
				return null;
			}
			return next.execute(((DateTime) value).toString(dateOnlyFormatter), context);
		}
	}
}
