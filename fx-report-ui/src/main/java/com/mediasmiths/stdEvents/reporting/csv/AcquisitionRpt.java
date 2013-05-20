package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.AddOrUpdatePackage;
import com.mediasmiths.foxtel.ip.common.events.CreateOrUpdateTitle;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AggregatedBMS;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;

public class AcquisitionRpt
{
	public static final transient Logger log = Logger.getLogger(AcquisitionRpt.class);

	// Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;

	@Inject
	private QueryAPI queryApi;

	public void writeAcquisitionDelivery(
			final List<EventEntity> materials,
			final List<AggregatedBMS> bms,
			final DateTime startDate,
			final DateTime endDate,
			final String reportName)
	{
		log.debug(">>>writeAcquisitionDelivery");
		List<Acquisition> titles = getReportList(materials, bms, startDate, endDate);

		int noFile = 0;
		int noTape = 0;

		for (Acquisition event : titles)
		{
			if (event.isFileDelivery())
				noFile++;
			if (event.isTapeDelivery())
				noTape++;
		}

		titles.add(addStats("No By File", Integer.toString(noFile)));
		titles.add(addStats("No By Tape", Integer.toString(noTape)));
		titles.add(addStats("% By File", Double.toString((noFile / titles.size()) * 100)));
		titles.add(addStats("% By Tape", Double.toString((noTape / titles.size()) * 100)));

		createCSV(titles, reportName);
		log.debug("<<<writeAcquisitionDelivery");
	}

	/*
	 * pseudo code to change logic
	 */
	private List<Acquisition> getReportList(
			final List<EventEntity> events,
			final List<AggregatedBMS> bms,
			final DateTime startDate,
			final DateTime endDate)
	{
		log.debug(">>>getReportList");

		List<CreateOrUpdateTitle> titles = getTitles();
		List<AddOrUpdatePackage> packages = getPackages();

		log.info(String.format("Titles: %d; Packages: %d", titles.size(), packages.size()));

		List<Acquisition> acqs = new ArrayList<Acquisition>();
		for (EventEntity event : events)
		{
			Acquisition content = (Acquisition) unmarshall(event);
			//TODO: format date
			content.setDateRange(startDate + " - " + endDate);
			
			if (content.isFileDelivery())
			{
				content.setFileDel("1");
			}
			else if (content.isTapeDelivery())
			{
				content.setTapeDel("1");
			}

			final String contentMatId = content.getMaterialID();
			
			AddOrUpdatePackage matchingPackage = null;
			
			for (AddOrUpdatePackage pack : packages)
			{
				if (StringUtils.isNotBlank(pack.getMaterialID()) && StringUtils.isNotBlank(contentMatId))
				{
					if (pack.getMaterialID().equals(contentMatId))
					{
						matchingPackage = pack;
						break;
					}
				}
			}

			for (CreateOrUpdateTitle title : titles)
			{
				if (StringUtils.isNotBlank(title.getTitleID()) && StringUtils.isNotBlank(matchingPackage.getTitleID()))
				{
					if (title.getTitleID().equals(matchingPackage.getTitleID()))
					{
						content.setChannels(title.getChannels());
						break;
					}
				}
			}

			for (AggregatedBMS b : bms)
			{
				final String matId = b.getMaterialID();
				if (StringUtils.isNotBlank(matId) && (matId.equals(contentMatId)))
				{
					content.setChannels(b.getChannels());
				}
			}

			acqs.add(content);
		}
		log.debug("<<<getReportList");
		return acqs;
	}

	// TODO - should these be within the date range provided?
	//search on date range
	private List<CreateOrUpdateTitle> getTitles()
	{
		List<CreateOrUpdateTitle> titles = new ArrayList<CreateOrUpdateTitle>();

		final List<EventEntity> titleEvents = queryApi.getByEventName("CreateOrUpdateTitle");
		for (EventEntity event : titleEvents)
		{
			CreateOrUpdateTitle title = (CreateOrUpdateTitle) unmarshall(event);
			titles.add(title);
		}
		return titles;
	}

	// TODO - should these be within the date range provided?
	private List<AddOrUpdatePackage> getPackages()
	{
		List<AddOrUpdatePackage> packages = new ArrayList<AddOrUpdatePackage>();

		final List<EventEntity> packageEvents = queryApi.getByEventName("AddOrUpdatePackage");
		for (EventEntity event : packageEvents)
		{
			AddOrUpdatePackage pack = (AddOrUpdatePackage) unmarshall(event);
			packages.add(pack);
		}

		return packages;
	}

	private Object unmarshall(final EventEntity event)
	{
		Object obj = null;
		String payload = event.getPayload();
		log.info("Unmarshalling payload " + payload);

		try
		{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.report.ObjectFactory.class);
			log.info("Deserialising payload");
			obj = JAXB_SERIALISER.deserialise(payload);
			log.info("Object created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return obj;
	}

	private void createCSV(final List<Acquisition> titles, final String reportName)
	{
		log.debug(">>>createCSV");
		ICsvBeanWriter beanWriter = null;
		try
		{
			log.info(String.format("Writing Report Name: %s to %s", reportName, REPORT_LOC));
			beanWriter = new CsvBeanWriter(
					new FileWriter(new StringBuilder(REPORT_LOC).append(reportName).append(".csv").toString()),
					CsvPreference.STANDARD_PREFERENCE);

			final String[] header = { "dateRange", "title", "materialID", "channels", "aggregatorID", "tapeDel", "fileDel",
					"format", "filesize", "titleLength" };
			beanWriter.writeHeader(header);

			final CellProcessor[] processors = getProcessor();
			for (Acquisition title : titles)
			{
				beanWriter.write(title, header, processors);
			}
		}
		catch (IOException e)
		{
			log.error(String.format(
					"An exception was caught whilst writing Acquisition Report %s for the following reason: %s",
					reportName,
					e.getMessage()));
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
					log.error(String.format(
							"An exception was caught whilst closing the report writer for the following reason: %s",
							e.getMessage()));
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
}