package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.FilePickupDetails;
import com.mediasmiths.foxtel.ip.common.events.report.Watchfolder;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;

public class WatchFolderRpt
{
	public static final transient Logger logger = Logger.getLogger(WatchFolderRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeWatchFolder(List<EventEntity> events, Date startDate, Date endDate, String reportName)
	{
		List<Watchfolder> watched = getWatchFolderList(events, startDate, endDate);
		
		createCsv(watched, reportName);	
	}
	
	private Object unmarshall(EventEntity event)
	{
		Object file = null;
		String payload = event.getPayload();
		logger.info("Unmarshalling payload: " + payload);
		try {
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
			file = JAXB_SERIALISER.deserialise(payload);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}
	
	public List<Watchfolder> getWatchFolderList(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<Watchfolder> files = new ArrayList<Watchfolder>();
		for (EventEntity event : events)
		{
			Watchfolder file = new Watchfolder();
			FilePickupDetails details = (FilePickupDetails) unmarshall(event);
			file.setDateRange(startDate + " - " + endDate);
			file.setFilename(details.getFilename());
			file.setDiscovered(new Date(details.getTimeDiscovered()).toString());
			file.setProcessed(new Date(details.getTimeProcessed()).toString());
			files.add(file);
		}
		
		return files;
	}
	
	private void createCsv (List<Watchfolder> files, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try { 
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "filename", "discovered", "processed", "aggregator"};
			final CellProcessor[] processors = getWatchFolderProcessor();
			beanWriter.writeHeader(header);
			
			for (Watchfolder file : files) {
				beanWriter.write(file, header, processors);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally {
			if (beanWriter != null)
			{
				try
				{
					beanWriter.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
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
