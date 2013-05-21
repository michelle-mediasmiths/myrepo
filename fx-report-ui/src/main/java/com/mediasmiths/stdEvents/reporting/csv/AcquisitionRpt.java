package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
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
	
	private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd-MM-yyyy");

	public void writeAcquisitionDelivery(
			final List<EventEntity> materials,
			final DateTime startDate,
			final DateTime endDate,
			final String reportName)
	{
		log.debug(">>>writeAcquisitionDelivery");
		List<Acquisition> titles = getReportList(materials, startDate, endDate);
		
		int noFile = 0;
		int noTape = 0;
		int total = 0;
		
		for (Acquisition acq :titles)
		{
			if (acq.isFileDelivery()) {
				noFile ++;
			}
			else if (acq.isTapeDelivery()) {
				noTape ++;
			}
			total ++;
		}
		
		log.debug("noFile: " + noFile + " noTape: " + noTape);
		
		double perFile = (noFile / total) * 100;
		double perTape = (noTape / total) * 100;
		
		log.debug("perFile: " + perFile + " perTape: " + perTape);
		
		titles.add(addStats("No By File", Integer.toString(noFile)));
		titles.add(addStats("No By Tape", Integer.toString(noTape)));
		titles.add(addStats("% By File", Double.toString(perFile)));
		titles.add(addStats("% By Tape", Double.toString(perTape)));

		createCSV(titles, reportName);
		log.debug("<<<writeAcquisitionDelivery");
	}

	private List<Acquisition> getReportList(
			final List<EventEntity> events,
			final DateTime startDate,
			final DateTime endDate)
	{
		log.debug(">>>getReportList");
		
		List<Acquisition> acqs = new ArrayList<Acquisition>();
		List<OrderStatus> orders = getOrders(startDate, endDate);
		
		String startF = startDate.toString(dateFormatter);
		String endF = endDate.toString(dateFormatter);

		for (EventEntity event : events) 
		{
			Acquisition acq = (Acquisition) unmarshall(event);
			
			acq.setDateRange(startF + " - " + endF);
			
			for (OrderStatus order : orders) 
			{
				String materialID = order.getMaterialid();
				if (materialID.equals(acq.getMaterialID())) 
				{
					log.debug("matching order found, materialID: " + materialID);
					if (order.getTitle() != null)
					{
						log.debug("title: " + order.getTitle().getTitle());
						log.debug("channels: " + order.getTitle().getChannels());
						acq.setChannels(order.getTitle().getChannels().toString());
						break;
					}
				}
			}
			
			log.debug("file: " + acq.isFileDelivery() + " tape: " + acq.isTapeDelivery());
			if (acq.isFileDelivery())
			{
				acq.setFileDel("1");
			}
			else if (acq.isTapeDelivery())
			{
				acq.setTapeDel("1");
			}
			
			log.info(acq.getMaterialID() + " " + acq.getTitle() + " " + acq.getChannels());
			acqs.add(acq);
		}
		
		log.debug("<<<getReportList");
		return acqs;
	}

	private List<OrderStatus> getOrders(DateTime start, DateTime end)
	{		
		return queryApi.getOrdersInDateRange(start, end);
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