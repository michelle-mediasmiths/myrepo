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
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			if (acq.isTapeDelivery() || isFromTape)
			{
				acq.setTapeDel("1");
				acq.setFileDel("0");
			}
			else if (acq.isFileDelivery())
			{
				acq.setFileDel("1");
				acq.setTapeDel("0");
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
		ICsvMapWriter csvwriter = null;
		try
		{
			log.info("reportName: " + reportName);
			FileWriter fileWriter = new FileWriter(REPORT_LOC + reportName + ".csv");
			csvwriter = new CsvMapWriter(fileWriter, CsvPreference.STANDARD_PREFERENCE);
			log.info("Saving to: " + REPORT_LOC);

			final String[] header = {"dateRange",
			                         "title",
			                         "materialID",
			                         "channels",
			                         "aggregatorID",
			                         "tapeDel",
			                         "fileDel",
			                         "format",
			                         "Filesize (GB)",
			                         "titleLength"};

			final CellProcessor[] processors = getProcessor();
			csvwriter.writeHeader(header);

			for (Acquisition title : titles)
			{
				final Map<String, Object> map = new HashMap<String, Object>();

				map.put(header[0], title.getDateRange());
				map.put(header[1], title.getTitle());
				map.put(header[2], title.getMaterialID());
				map.put(header[3], title.getChannels());
				map.put(header[4], title.getAggregatorID());
				map.put(header[5], title.getTapeDel());
				map.put(header[6], title.getFileDel());
				map.put(header[7], title.getFormat());
				map.put(header[8], title.getFilesize());
				map.put(header[9], title.getTitleLength());

				csvwriter.write(map, header, processors);
			}
		}
		catch (IOException e)
		{
			log.error("error writing report",e);
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
					log.error("IOException closing csv writer", e);
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
