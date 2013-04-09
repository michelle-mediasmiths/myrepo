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
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;

import com.mediasmiths.stdEvents.reporting.rest.ReportUI;

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
	
	private List<String> channels = new ArrayList<String>();
	private List<String> formats = new ArrayList<String>();
	
	private int noFile=0;
	private int noTape=0;
	private double perFile=0;
	private double perTape=0;
	
	public void writeAcquisitionDelivery(List<EventEntity> materials, Date startDate, Date endDate, String reportName)
	{
		List<Acquisition> titles = getReportList(materials, startDate, endDate);
		//setStats(titles);
		
		Acquisition noFileS = addStats("No By File", Integer.toString(noFile));
		Acquisition noTapeS = addStats("No By Tape", Integer.toString(noTape));
		Acquisition perFileS = addStats("% By File", Double.toString(perFile));
		Acquisition perTapeS = addStats("% By Tape", Double.toString(perTape));
		
		titles.add(noFileS);
		titles.add(noTapeS);
		titles.add(perFileS);
		titles.add(perTapeS);
		
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
		
		List<EventEntity> packageEvents = queryApi.getByEventName("AddOrUpdatePackage");
		List<AddOrUpdatePackage> packages = new ArrayList<AddOrUpdatePackage>();
		for (EventEntity event : packageEvents) {
			AddOrUpdatePackage pack = (AddOrUpdatePackage) unmarshallBMS(event);
			packages.add(pack);
		}
		
		List<EventEntity> titleEvents = queryApi.getByEventName("CreateOrUpdateTitle");
		List<CreateOrUpdateTitle> titles = new ArrayList<CreateOrUpdateTitle>();
		for (EventEntity event : titleEvents) {
			CreateOrUpdateTitle title = (CreateOrUpdateTitle) unmarshallBMS(event);
			titles.add(title);
		}
		
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
	
//	ICsvBeanWriter beanWriter = null;
//	try{
//		beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
//		final String[] header = {"dateRange", "title", "materialID", "channels", "aggregatorID", "tape", "file", "format", "fileSize", "duration"};
//		final CellProcessor[] processors = getAcquisitionProcessor();
//		beanWriter.writeHeader(header);
//		
//		AcquisitionDelivery noFile = new AcquisitionDelivery("No Delivered By File", Integer.toString(queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File"))));
//		AcquisitionDelivery noTape = new AcquisitionDelivery("No Delivered By Tape", Integer.toString(queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"))));
//		titles.add(noFile);
//		titles.add(noTape);
//		
//		int total = (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"))) + (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File")));
//		logger.info("total: " + total);
//		double perByTape = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"));
//		double perByFile = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File"));
//		perByTape = (perByTape/total)*100;
//		perByFile = (perByFile/total)*100;
//		logger.info("Tape: " + perByTape + "File: " + perByFile);
//		
//		AcquisitionDelivery perFile = new AcquisitionDelivery("% By File", Double.toString(perByFile));
//		AcquisitionDelivery perTape = new AcquisitionDelivery("% By Tape", Double.toString(perByTape));
//		titles.add(perFile);
//		titles.add(perTape);
//		
//		for (String channel : channels) 
//		{
//			AcquisitionDelivery channelStat = new AcquisitionDelivery(channel, Integer.toString(getByChannel(channel, titles).size()));
//			titles.add(channelStat);
//		}
//		
//		for (String format : formats)
//		{
//			AcquisitionDelivery formatStat = new AcquisitionDelivery(format, Integer.toString(getByFormat(format, titles).size()));
//			titles.add(formatStat);
//		}
//		
//		for (AcquisitionDelivery title : titles)
//		{
//			beanWriter.write(title, header, processors);
//		}
//	}
//	catch (IOException e)
//	{
//		e.printStackTrace();
//	}
//	finally {
//		if (beanWriter != null)
//		{
//			try
//			{
//				beanWriter.close();
//			}
//			catch(IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	List<AcquisitionDelivery> titleList = new ArrayList<AcquisitionDelivery>();
//	for(EventEntity event : events)
//	{
//		String payload = event.getPayload();
//		logger.info(payload);
//		AcquisitionDelivery title = new AcquisitionDelivery();
//		title.setDateRange(startDate.toString() + " - " + endDate.toString());
//		if (payload.contains("materialId"))
//		{
//			String materialId = payload.substring(payload.indexOf("materialId") +11, payload.indexOf("</materialId"));
//			title.setMaterialID(materialId);
//		}
//		
//		List<EventEntity> channelEvents = queryApi.getByEventName("CreateOrUpdateTitle");
//		for (EventEntity channelEvent : channelEvents)
//		{
//			String str = channelEvent.getPayload();
//			String channelTitle = str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9)));
//			logger.info("Channel: " + channelTitle);
//			if (payload.contains("materialId")) {
//				String curTitle =  payload.substring(payload.indexOf("materialId") +11, payload.indexOf("</materialId"));
//				logger.info("Current: " + curTitle);
//				if (curTitle.equals(channelTitle)) {
//					title.setChannels(str.substring(str.indexOf("channelName")+13, str.indexOf('"',(str.indexOf("channelName")+13))));
//					logger.info("Channel name: " + title.getChannels());
//					extractChannels(title.getChannels());
//					if (str.contains("ProgrammeTitle"))
//						title.setTitle(str.substring(str.indexOf("ProgrammeTitle")+15, str.indexOf("</ProgrammeTitle")));
//				}
//			}
//		}
//		
//		List<EventEntity> aggregatorEvents = queryApi.getByEventName("AddOrUpdateMaterial");
//		for (EventEntity aggregatorEvent : aggregatorEvents)
//		{
//			String str = aggregatorEvent.getPayload();
//			String aggregatorTitle = str.substring(str.indexOf("materialID")+12, str.indexOf('"', (str.indexOf("materialID")+12)));
//			logger.info("agg:" + aggregatorTitle);
//			if (payload.contains("materialId")) {
//				String curTitle =  payload.substring(payload.indexOf("materialId") +11, payload.indexOf("</materialId"));
//				logger.info("Current " + curTitle);
//				if (curTitle.equals(aggregatorTitle))
//					title.setAggregatorID(str.substring(str.indexOf("aggregatorID")+14, str.indexOf('"',(str.indexOf("aggregatorID")+14))));
//			}
//		}
//		
//		if ((payload.contains("Tape")) || (payload.contains("tapeMediaType")))
//				title.setTape("1");
//		if ((payload.contains("File")) || (payload.contains("fileMediaType")))
//				title.setFile("1");
//	
//	if (payload.contains("Format")) {
//		title.setFormat(payload.substring(payload.indexOf("Format") +7, payload.indexOf("</Format")));
//		extractFormats(title.getFormat());
//	}
//	if (payload.contains("Duration"))
//	{
//		String duration = payload.substring(payload.indexOf("Duration") +9, payload.indexOf("</Duration"));
//		title.setDuration(duration);
//	}
//	
//	titleList.add(title);	
//	}
//	return titleList;
//}
//
//private void extractChannels(String channel)
//{
//	if (channel.contains(","))
//	{
//		String[] channelArray = channel.split(",");
//		logger.info("Channel array: " + channelArray);
//		for (int i=0; i<channelArray.length; i++) {
//			if (!channels.contains(channelArray[i]))
//				channels.add(channelArray[i]);
//		}
//	}
//	else {
//		if (!channels.contains(channel)) {
//			logger.info("Adding channel: " + channel);
//			channels.add(channel);
//		}
//	}
//}
//public void extractFormats(String format)
//{
//	if (!formats.contains(format))
//	{
//		logger.info("Adding format");
//		formats.add(format);
//	}
//}
//
//public List<AcquisitionDelivery> getByChannel(String channel, List<AcquisitionDelivery> materials)
//{
//	List<AcquisitionDelivery> byChannel = new ArrayList<AcquisitionDelivery>();
//	logger.info("Material list: " + materials);
//	for (AcquisitionDelivery event : materials)
//	{
//		if (event.getChannels() != null) {
//			if (event.getChannels().contains(channel))
//				byChannel.add(event);
//		}
//	}
//	logger.info("Channel list: " + byChannel);
//	return byChannel;
//}
//
//public List<AcquisitionDelivery> getByFormat(String format, List<AcquisitionDelivery> materials)
//{
//	List<AcquisitionDelivery> byFormat = new ArrayList<AcquisitionDelivery>();
//	for (AcquisitionDelivery event : materials)
//	{
//		if (event.getFormat() != null) {
//			if (event.getFormat().contains(format))
//				byFormat.add(event);
//		}
//	}
//	return byFormat;
//}
