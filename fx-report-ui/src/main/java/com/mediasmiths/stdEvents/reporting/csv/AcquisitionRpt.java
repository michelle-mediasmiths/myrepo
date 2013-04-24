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
import com.mediasmiths.foxtel.ip.common.events.AddOrUpdatePackage;
import com.mediasmiths.foxtel.ip.common.events.CreateOrUpdateTitle;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AggregatedBMS;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;

import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.rest.ReportUI;
import com.mediasmiths.stdEvents.reporting.rest.ReportUIImpl;

public class AcquisitionRpt
{
	public static final transient Logger logger = Logger.getLogger(AcquisitionRpt.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	@Inject
	private ReportUI reportUi;
	
	private ReportUIImpl report = new ReportUIImpl();
	
	private List<String> channels = new ArrayList<String>();
	private List<String> formats = new ArrayList<String>();
	
	private int noFile=0;
	private int noTape=0;
	private double perFile=0;
	private double perTape=0;
	
	public void writeAcquisitionDelivery(List<EventEntity> materials, Date startDate, Date endDate, String reportName)
	{
		List<Acquisition> titles = getReportList(materials, startDate, endDate);
		setStats(titles);
		
		titles.add(addStats("No By File", Integer.toString(noFile)));
		titles.add(addStats("No By Tape", Integer.toString(noTape)));
		titles.add(addStats("% By File", Double.toString(perFile)));
		titles.add(addStats("% By Tape", Double.toString(perTape)));
		
		createCSV(titles, reportName);	
	}
	
	private Acquisition unmarshall(EventEntity event)
	{
		Object title = new Acquisition();
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);
		
		try{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.report.ObjectFactory.class);
			logger.info("Deserialising payload");
			title = JAXB_SERIALISER.deserialise(payload);
			logger.info("Object created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return (Acquisition) title;
	}
	
	private Object unmarshallBMS (EventEntity event) 
	{
		Object title = null;
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);
		
		try{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
			logger.info("Deserialising payload");
			title = JAXB_SERIALISER.deserialise(payload);
			logger.info("Object created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return title;
	}
	
	public List<Acquisition> getReportList(List<EventEntity> events, Date startDate, Date endDate)
	{
		logger.info("Creating acquisition list");
		
		List<CreateOrUpdateTitle> titles = report.titles;
		List<AddOrUpdatePackage> packages = report.packages;

//		UNCOMMENT TO USE WITH BMS AGGREGATION
//		List<AggregatedBMS> bms = report.bms;
		
		logger.info("Titles: " + titles.size() + " Packages: " + packages.size());
		
		List<Acquisition> acqs = new ArrayList<Acquisition>();
		for (EventEntity event : events)
		{
			Acquisition content = (Acquisition) unmarshall(event);
			content.setDateRange(startDate + " - " + endDate);
			if (content.isTapeDelivery())
				content.setTapeDel("1");
			if (content.isFileDelivery())
				content.setFileDel("1");
			
			AddOrUpdatePackage matchingPackage = new AddOrUpdatePackage();
			for(AddOrUpdatePackage pack : packages) {
				if ((pack.getMaterialID() != null) && (content.getMaterialID() != null)) {
					if (pack.getMaterialID().equals(content.getMaterialID())) {
						matchingPackage = pack;
					}
				}
			}
			
			for (CreateOrUpdateTitle title : titles) {
				if ((title.getTitleID() != null) && (matchingPackage.getTitleID() != null)) {
					if (title.getTitleID().equals(matchingPackage.getTitleID())) {
						content.setChannels(title.getChannels());
					}
				}
			}
			
//			for (AggregatedBMS b : bms) {
//				if ((b.getMaterialID() != null) && (b.getMaterialID().equals(content.getMaterialID())))
//						content.setChannels(b.getChannels());
//			}
			
			acqs.add(content);
		}
		return acqs;
	}
	
	private void createCSV(List<Acquisition> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "title", "materialID", "channels", "aggregatorID", "tapeDel", "fileDel", "format", "filesize", "titleLength"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (Acquisition title : titles)
			{
				beanWriter.write(title, header, processors);
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
	
	private CellProcessor[] getProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] {
				new Optional(),
				new Optional(), 
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional()
		};
		return processors;
	}
	
	private void setStats(List<Acquisition> events)
	{
		for (Acquisition event : events)
		{
			if (event.isFileDelivery())
				noFile ++;
			if (event.isTapeDelivery())
				noTape ++;
			perFile = (noFile / events.size()) * 100;
			perTape = (noTape / events.size()) * 100;
		}
	}
	
	private Acquisition addStats(String name, String value)
	{
		Acquisition acq = new Acquisition();
		acq.setTitle(name);
		acq.setMaterialID(value);
		return acq;
	}
}