package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AcquisitionRpt extends ReportUtils
{
	public static final transient Logger log = Logger.getLogger(AcquisitionRpt.class);

	// Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;

	@Inject
	private QueryAPIImpl queryApi;

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
		double perFile = 0;
		double perTape = 0;
		
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

		if (total > 0)
		{
			perFile = (noFile / total) * 100;
			perTape = (noTape / total) * 100;
		}
		
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

		String startF = startDate.toString(dateOnlyFormatString);
		String endF = endDate.toString(dateOnlyFormatString);

		for (EventEntity event : events) 
		{
			Acquisition acq = (Acquisition) unmarshallReport(event);

			acq.setDateRange(startF + " - " + endF);
			OrderStatus order = queryApi.getOrderStatusById(acq.getMaterialID());


			if (order!= null && order.getTitle() != null)
			{
				log.debug("title: " + order.getTitle().getTitle());
				log.debug("channels: " + order.getTitle().getChannels().toString());
				acq.setChannels(StringUtils.join(order.getTitle().getChannels(), ';'));

			}

			//Retrieving file size in GB

			long fileSize;

			if (order!= null && order.getFileSize() != null)
			{
				fileSize = order.getFileSize();
			}
			else
			{
				fileSize = Long.parseLong(acq.getFilesize());
			}
			if (fileSize!= 0)
			{
				acq.setFilesize(acquisitionReportFileSize(fileSize));
			}


			String aggregator = acq.getAggregatorID();
			boolean isFromTape = false;
			if (aggregator != null)
			{
				if (aggregator.toLowerCase().equals("ruzz")
						|| aggregator.toLowerCase().equals("vizcapture")
						|| aggregator.toLowerCase().equals("dart"))
				{
					isFromTape = true;
				}
			}
			
			log.debug("file: " + acq.isFileDelivery() + " tape: " + acq.isTapeDelivery());
			if (acq.isFileDelivery())
			{
				acq.setFileDel("1");
				acq.setTapeDel("0");
			}
			else if (acq.isTapeDelivery() || isFromTape)
			{
				acq.setTapeDel("1");
				acq.setFileDel("0");
			}

			log.info(acq.getMaterialID() + " " + acq.getTitle() + " " + acq.getChannels());
			acqs.add(acq);
		}

		log.debug("<<<getReportList");
		return acqs;
	}


	public static String acquisitionReportFileSize(long fileSize)
	{
		String displaySize;

		if (fileSize >= FileUtils.ONE_GB)
		{
			displaySize = String.valueOf(fileSize/ FileUtils.ONE_GB);

			log.debug("fileSize long : " + fileSize);
			log.debug("file size in GB : " + displaySize);
		}
		else
		{
			double file = (double) fileSize;
			DecimalFormat twoDForm = new DecimalFormat("#.####");

			Double fileDisplaySize = file / ((double) FileUtils.ONE_GB);

			log.debug("file size long : " + fileSize);
			log.debug("file size in GB : " + Double.valueOf(twoDForm.format(fileDisplaySize)));

			displaySize = Double.valueOf(twoDForm.format(fileDisplaySize)).toString();
		}
		return displaySize;
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
					"format", "Filesize (GB)", "titleLength" };
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
