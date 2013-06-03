package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;

public class AcquisitionRpt extends ReportUtils
{
	public static final transient Logger log = Logger.getLogger(AcquisitionRpt.class);

	// Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;

	@Inject
	private QueryAPI queryApi;

	public void writeAcquisitionDelivery(
			final List<OrderStatus> materials,
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
			final List<OrderStatus> events,
			final DateTime startDate,
			final DateTime endDate)
	{
		log.debug(">>>getReportList");

		List<Acquisition> acqs = new ArrayList<Acquisition>();

		String startF = startDate.toString(dateFormatter);
		String endF = endDate.toString(dateFormatter);

		for (OrderStatus event : events) 
		{
			Acquisition acq = (Acquisition) unmarshallReport(test);

			acq.setDateRange(startF + " - " + endF);

			OrderStatus order = queryApi.getOrderStatusById(acq.getMaterialID());
			if (order.getTitle() != null)
			{
				log.debug("title: " + order.getTitle().getTitle());
				log.debug("channels: " + order.getTitle().getChannels());
				acq.setChannels(order.getTitle().getChannels().toString());
				break;
			}

			long filesize = Long.valueOf(acq.getFilesize()).longValue();
			log.debug("filesize long: " + filesize);
			acq.setFilesize(FileUtils.byteCountToDisplaySize(filesize));

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