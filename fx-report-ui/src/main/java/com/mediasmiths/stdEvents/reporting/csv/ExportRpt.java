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
import com.mediasmiths.foxtel.ip.common.events.ExportStart;
import com.mediasmiths.foxtel.ip.common.events.TcEvent;
import com.mediasmiths.foxtel.ip.common.events.TcNotification;
import com.mediasmiths.foxtel.ip.common.events.TcPassedNotification;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.foxtel.ip.common.events.report.Export;
import com.mediasmiths.foxtel.ip.common.events.report.OrderStatus;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.ExportRT;

public class ExportRpt
{
	public static final transient Logger logger = Logger.getLogger(ExportRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeExport(List<EventEntity> events, Date startDate, Date endDate, String reportName)
	{
		List<Export> exports = getReportList(events, startDate , endDate);
		createCsv(exports, reportName);
	}
	
	private Object unmarshall(EventEntity event)
	{
		Object title = null;
		String payload = event.getPayload();
		logger.info("event.eventName: " + event.getEventName());
		logger.info("event.id: " + event.getId());
		logger.info("Unmarshalling payload " + payload);

		try
		{
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
	
	private Object unmarshallRpt(EventEntity event) 
	{
		Object title = null;
		String payload = event.getPayload();
		logger.info("Unmarshalling payload: " + payload);
		
		try
		{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.report.ObjectFactory.class);
			title = JAXB_SERIALISER.deserialise(payload);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return title;
	}
	
	public List<Export> getReportList(List<EventEntity> events, Date startDate, Date endDate)
	{
		logger.info("Creating export list");
		List<Export> exports = new ArrayList<Export>();
		
		List<EventEntity> packageEvents = queryApi.getByEventName("AddOrUpdatePackage");
		List<AddOrUpdatePackage> packages = new ArrayList<AddOrUpdatePackage>();
		for (EventEntity event : packageEvents) {
			AddOrUpdatePackage pack = (AddOrUpdatePackage) unmarshall(event);
			packages.add(pack);
		}
		
		List<EventEntity> titleEvents = queryApi.getByEventName("CreateOrUpdateTitle");
		List<CreateOrUpdateTitle> titles = new ArrayList<CreateOrUpdateTitle>();
		for (EventEntity event : titleEvents) {
			CreateOrUpdateTitle title = (CreateOrUpdateTitle) unmarshall(event);
			titles.add(title);
		}
		
		List <EventEntity> acqEvents = queryApi.getByEventName("ProgrammeContentAvailable");
		acqEvents.addAll(queryApi.getByEventName("MarketingContentAvailable"));
		List<Acquisition> acqs = new ArrayList<Acquisition>();
		for (EventEntity event : acqEvents) {
			Acquisition acq = (Acquisition) unmarshallRpt(event);
			acqs.add(acq);
		}
		
		for (EventEntity event : events)
		{
			Export export = new Export();
			TcEvent tcNotification = (TcEvent) unmarshall(event);
			
			export.setDateRange(startDate + " - " + endDate);
			export.setMaterialID(tcNotification.getAssetID());
			export.setChannels(tcNotification.getChannelGroup().toString());
			export.setTaskStatus("FINISHED");
			
			if(event.getEventName().equals("CaptionProxySuccess")) {
				export.setExportType("Caption");
			}
			if (event.getEventName().equals("ComplianceProxySuccess")) {
				export.setExportType("Compliance");
			}
			if (event.getEventName().equals("ClassificationProxySuccess")) {
				export.setExportType("Classification");
			}
			
			AddOrUpdatePackage matchingPackage = new AddOrUpdatePackage();
			for (AddOrUpdatePackage pack : packages) {
				if (pack.getMaterialID().equals(export.getMaterialID())) {
					matchingPackage = pack;
				}
			}
			
			for (CreateOrUpdateTitle title : titles) {
				if (title.getTitleID().equals(matchingPackage.getTitleID())) {
					export.setTitle(title.getTitle());
					export.setChannels(title.getChannels());
				}
			}
			
			for (Acquisition acq : acqs) {
				if (acq.getMaterialID().equals(matchingPackage.getMaterialID())) {
					export.setTitleLength(acq.getTitleLength());
				}
			}
			
			exports.add(export);
		}		
		return exports;
	}
	
	private void createCsv(List<Export> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "title", "materialID", "channels", "taskStatus", "exportType", "titleLength"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (Export title : titles)
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
				new Optional()
		};
		return processors;
	}
}

