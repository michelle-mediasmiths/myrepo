package com.mediasmiths.stdEvents.reporting.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.QueryParam;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.AddOrUpdateMaterial;
import com.mediasmiths.foxtel.ip.common.events.AddOrUpdatePackage;
import com.mediasmiths.foxtel.ip.common.events.CreateOrUpdateTitle;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.foxtel.ip.common.events.report.AutoQC;
import com.mediasmiths.foxtel.ip.common.events.report.ComplianceLogging;
import com.mediasmiths.foxtel.ip.common.events.report.Export;
import com.mediasmiths.foxtel.ip.common.events.report.ManualQA;
import com.mediasmiths.foxtel.ip.common.events.report.OrderStatus;
import com.mediasmiths.foxtel.ip.common.events.report.PurgeContent;
import com.mediasmiths.foxtel.ip.common.events.report.Watchfolder;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AggregatedBMS;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.reporting.csv.AcquisitionRpt;
import com.mediasmiths.stdEvents.reporting.csv.AutoQCRpt;
import com.mediasmiths.stdEvents.reporting.csv.ComplianceRpt;
import com.mediasmiths.stdEvents.reporting.csv.ExportRpt;
import com.mediasmiths.stdEvents.reporting.csv.ManualQARpt;
import com.mediasmiths.stdEvents.reporting.csv.OrderStatusRpt;
import com.mediasmiths.stdEvents.reporting.csv.PurgeContentRpt;
import com.mediasmiths.stdEvents.reporting.csv.TaskListRpt;
import com.mediasmiths.stdEvents.reporting.csv.TranscoderLoadRpt;
import com.mediasmiths.stdEvents.reporting.csv.WatchFolderRpt;

public class ReportUIImpl implements ReportUI
{
	@Inject
	private ThymeleafTemplater templater;
	
	@Inject
	private QueryAPI queryApi;
	
	@Inject
	private EventAPI eventApi;
	@Inject
	private OrderStatusRpt orderStatus;
	@Inject
	private AcquisitionRpt acquisition;
	@Inject
	private ManualQARpt manualQa;
	@Inject
	private AutoQCRpt autoQc;
	@Inject
	private TaskListRpt taskList;
	@Inject
	private PurgeContentRpt purgeContent;
	@Inject
	private ComplianceRpt compliance;
	@Inject
	private ExportRpt export;
	@Inject
	private WatchFolderRpt watchFolder;
	@Inject
	private TranscoderLoadRpt transcoderLoad;

	public static transient final Logger logger = Logger.getLogger(ReportUIImpl.class);
	
	private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd-MM-yyyy");
	private static final DateTimeFormatter toReadable = DateTimeFormat.forPattern("HH:mm:ss dd-MM-yyyy");
	
	int startDay;
	int startMonth;
	int startYear;
	public static Long startLong;
	public static Calendar startCal;
	public static Date startDate;
	
	int endDay;
	int endMonth;
	int endYear;
	public static Long endLong;
	public static Calendar endCal;
	public static Date endDate;
	
	public static String REPORT_NAME;
	
	public List<EventEntity> titleEvents = new ArrayList<EventEntity>();
	public List<CreateOrUpdateTitle> titles = new ArrayList<CreateOrUpdateTitle>();
	
	public void populateTitles()
	{
		titleEvents = queryApi.getByEventName("CreateOrUpdateTitle");
		for (EventEntity event : titleEvents) {
			CreateOrUpdateTitle title = (CreateOrUpdateTitle) unmarshall(event);
			titles.add(title);
		}
	}
	
	public List<EventEntity> packageEvents = new ArrayList<EventEntity>();
	public List<AddOrUpdatePackage> packages = new ArrayList<AddOrUpdatePackage>();
	
	public void populatePackages()
	{
		packageEvents = queryApi.getByEventName("AddOrUpdatePackage");
		for (EventEntity event : packageEvents) {
			AddOrUpdatePackage pack = (AddOrUpdatePackage) unmarshall(event);
			packages.add(pack);
		}
	}
	
	public List<Acquisition> populateAcq()
	{
		List<Acquisition> acqs = new ArrayList<Acquisition>();
		List<EventEntity> acqEvents = queryApi.getByEventName("ProgrammeContentAvailable");
		acqEvents.addAll(queryApi.getByEventName("MarketingContentAvailable"));
		for (EventEntity event : acqEvents) {
			Acquisition acq = (Acquisition) unmarshallRpt(event);
			acqs.add(acq);
		}
		return acqs;
	}
	
	/**
	 * Returns all the records in the BMS table
	 * @return
	 */
	public List<AggregatedBMS> populateBMS() 
	{
		logger.debug(">>>populateBMS");
		return queryApi.getAllBMS();
	}
	
	@Inject
	@Named("windowMax")
	public int MAX;

	@Transactional
	public String getReport()
	{
		TemplateCall call = templater.template("report");
		return call.process();
	}
	
	@Transactional
	public String getPopup()
	{
		TemplateCall call = templater.template("pop-up");
		return call.process();
	}
	
	@Transactional
	public String getByNamespace(@QueryParam("namespace") String namespace)
	{
		final TemplateCall call = templater.template("search");
		call.set("events", queryApi.getByNamespace(namespace));
		return call.process();
	}
	
	@Transactional
	public String getByNamespaceWindow(@QueryParam("namespace") String namespace)
	{
		final TemplateCall call = templater.template("search");
		logger.info("ReportUIImpl max: " + MAX);
		call.set("events", queryApi.getByNamespaceWindow(namespace, MAX));
		return call.process();
	}
	
	@Transactional
	public String getByEventName(@QueryParam("eventname") String eventname)
	{
		final TemplateCall call = templater.template("search");
		call.set("events", queryApi.getByEventName(eventname));
		return call.process();
	}
	
	@Transactional
	public String getByNamespaceEventname(@QueryParam("namespace") String namespace, @QueryParam("eventname") String eventname)
	{
		final TemplateCall call = templater.template("search");
		call.set("events", queryApi.getEvents(namespace, eventname));
		return call.process();
	}
	
	@Transactional
	public void saveStartDate(@QueryParam("start") String start)
	{
		try
		{
			startDate = new SimpleDateFormat("yyyy-MM-dd").parse(start);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		startLong = startDate.getTime();						
		logger.info("Start date: " + startDate + " Start long: " + startLong);
	}
	
	@Transactional
	public void saveEndDate(@QueryParam("end") String end)
	{
		try
		{
			endDate = new SimpleDateFormat("yyyy-MM-dd").parse(end);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		endLong = endDate.getTime();
		logger.info("End date: " + endDate + " End long: " + endLong);
	}
	
	public boolean checkDate(Long eventTime, Date start, Date end)
	{
		logger.debug(">>>checkDate");
		boolean within = false;
		Date event = new Date(eventTime);
		logger.debug("eventDate: " + eventTime + " " + event);
		if ((event.after(start)) && (event.before(end)))
		{
			within = true;
		}
		logger.debug("<<<checkDate");
		return within;
	}
	
	private List<EventEntity> getInDate(List<EventEntity> events, Date start, Date end)
	{
		logger.debug(">>>getInDate");
		List <EventEntity> valid = new ArrayList<EventEntity>();
		for (EventEntity event : events)
		{
			logger.info("event.getTime()" + event.getTime());
			boolean within = checkDate(event.getTime(), start, end);
			if (within)
				valid.add(event);
		}
		logger.debug("<<<getInDate");
		return valid;
	}
	
	private List<AggregatedBMS> getInDateBMS(final DateTime start, final DateTime end)
	{
		logger.debug(String.format("Date range checked from %s to %s", start, end));
		return queryApi.getAllBMSbyDate(start, end);
	}
	
	@Transactional
	public void saveReportName(@QueryParam("name") String name)
	{
		REPORT_NAME = name;
		logger.info("REPORT_NAME: " + REPORT_NAME);
	}
	
	@Transactional
	public String getById(@QueryParam("id") Long id)
	{
		final TemplateCall call = templater.template("search");
		call.set("events", queryApi.getById(id));
		return call.process();
	}
	
	@Transactional(readOnly=true)
	public String chooseReport(final String start, final String end, final String reportName, final String rptType)
	{
		logger.debug(">>>chooseReport");
		logger.debug("Received startdate: " + start + " enddate: " + end);
		logger.debug("Received reportname: " + reportName + " rptType: " + rptType);
				
		DateTime startDate = StringUtils.isBlank(start) ? new DateTime() : dateFormatter.parseDateTime(start);
		DateTime endDate = StringUtils.isBlank(end) ? new DateTime() : dateFormatter.parseDateTime(end);
		logger.debug(String.format("Start date formatted: %s", startDate.toString()));
		logger.debug(String.format("End date formatted: %s", endDate.toString()));
		
		if (rptType.equals("OrderStatus")) {
			logger.info("generating OrderStatus__");
				getOrderStatusCSV(startDate, endDate, reportName);
		}
		else if (rptType.equals("AcquisitionDelivery")) {
			logger.info("generating AcquisitionDelivery__");
				getAquisitionReportCSV(startDate, endDate, reportName);

		}
		else if (rptType.equals("ManualQA")) {
			logger.info("generating ManualQA__");
				getManualQACSV(startDate, endDate, reportName);
		}
		else if (rptType.equals("AutoQC")) {
			logger.info("generating AutoQC__");
				getAutoQCCSV(startDate, endDate, reportName);
		}
//		else if (rptType.equals("PurgeContent")) {
//			logger.info("generating PurgeContent__");
//				getPurgeContentCSV();
//		}
//		else if (rptType.equals("ComplianceEdits")) {
//			logger.info("generating ComplianceEdits__");
//				getComplianceEditCSV();
//		}
//		else if (rptType.equals("Export")) {
//			logger.info("generating Export");
//				getExportCSV();
//		}
//		else if (rptType.equals("Watchfolder")) {
//			logger.info("generating Watchfolder__");
//				getWatchFolderStorageCSV();;
//		}
		logger.debug("<<<chooseReport");
		return null;		
	}

	private List<OrderStatus> getReportList(List<EventEntity> events)
	{
		List<OrderStatus> orders = new ArrayList<OrderStatus>();
		
		List<AddOrUpdatePackage> packages = new ArrayList<AddOrUpdatePackage>();
		for (EventEntity pack : queryApi.getByEventNameWindow("AddOrUpdatePackage", MAX)) {
			AddOrUpdatePackage currentPack = (AddOrUpdatePackage) unmarshall(pack);
			packages.add(currentPack);
		}
		List<AddOrUpdateMaterial> materials = new ArrayList<AddOrUpdateMaterial>();
		for (EventEntity material : queryApi.getByEventNameWindow("AddOrUpdateMaterial", MAX)) {
			AddOrUpdateMaterial currentMaterial = (AddOrUpdateMaterial) unmarshall(material); 
			materials.add(currentMaterial);
		}
		
		for (EventEntity event : events)
		{
			CreateOrUpdateTitle title = (CreateOrUpdateTitle) unmarshall(event);
			
			AddOrUpdatePackage matchingPack = new AddOrUpdatePackage();
			for (AddOrUpdatePackage pack : packages) {
				if (pack.getTitleID().equals(title.getTitleID())) {
					matchingPack = pack;
				}
			}
			AddOrUpdateMaterial matchingMaterial = new AddOrUpdateMaterial();
			for (AddOrUpdateMaterial material : materials) {
				if (material.getMaterialID().equals(matchingPack.getMaterialID())) {
					matchingMaterial = material;
				}
			}
			
			OrderStatus order = new OrderStatus();
			order.setDateRange(startDate + " - " + endDate);
			order.setTitle(title.getTitle());
			order.setMaterialID(matchingPack.getMaterialID());
			order.setChannels(title.getChannels());
			order.setRequiredBy(matchingPack.getRequiredBy());
			if (event.getEventName().equals("UnmatchedContentAvailable"))
				order.setTaskType("Unmatched");
			else
				order.setTaskType("Ingest");
			order.setCompletionDate(matchingMaterial.getCompletionDate());
			
			orders.add(order);
		}
		logger.info("Orders length: " + orders.size());
		return orders;
	}
	
	private Object unmarshall(EventEntity event)
	{
		Object placeholder = null;
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);
		try {
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
			logger.info("Deserialising payload");
			placeholder = JAXB_SERIALISER.deserialise(payload);
			logger.info("Object created");
		}
		catch (Exception e)		
		{
			e.printStackTrace();
		}	
		return placeholder;
	}
	
	private Object unmarshallRpt(EventEntity event)
	{
		Object rpt = null;
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);
		try {
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.report.ObjectFactory.class);
			logger.info("Deserialising payload");
			rpt = JAXB_SERIALISER.deserialise(payload);
			logger.info("Object created");
		}
		catch (Exception e)		
		{
			e.printStackTrace();
		}	
		return rpt;
	}
	
	@Transactional(readOnly=true)
	public void getOrderStatusCSV(final DateTime start, final DateTime end, final String reportName)
	{
		logger.debug(">>>getOrderStatusCSV");
		
		List<AggregatedBMS> orders = getInDateBMS(start, end);
		String startDate = start.toString(toReadable);
		String endDate = end.toString(toReadable);
		logger.info("dates readable start: " + startDate + " end: " + endDate);
		logger.info("List size: " + orders.size());
		logger.debug(String.format("Requesting order status report for date range: %s to %s; report name will be: %s ", start.toString(), end.toString(), reportName));
		orderStatus.writeOrderStatus(orders, startDate, endDate, reportName);
		
		logger.debug("<<<getOrderStatusCSV");
	}
	
	@Transactional(readOnly=true)
	public void getAquisitionReportCSV(final DateTime start, final DateTime end, final String reportName)
	{
		logger.debug(">>>getAcquisitionReportCSV");
		
		List<AggregatedBMS> bms = queryApi.getCompletedBefore(end.toDate());
		
		List<EventEntity> materials = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", MAX), start.toDate(), end.toDate());
		materials.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/content", "MarketingContentAvailable", MAX), start.toDate(), end.toDate()));
		acquisition.writeAcquisitionDelivery(materials, bms, start, end, reportName);
		
		logger.debug("<<<getAcquisitionReportCSV");
	}

	@Transactional(readOnly=true)
	public void getManualQACSV(final DateTime start, final DateTime end, final String reportName)
	{
		List<Acquisition> acqs = populateAcq();
		List<EventEntity> preview = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/preview", "ManualQA", MAX), start.toDate(), end.toDate());
		manualQa.writeManualQA(preview, acqs, start, end, reportName);
	}
	
	@Transactional(readOnly=true)
	public void getAutoQCCSV(final DateTime start, final DateTime end, final String reportName)
	{
		List<EventEntity> events = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "AutoQCPassed", MAX), start.toDate(), end.toDate());
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "QcFailedReOrder", MAX), start.toDate(), end.toDate()));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "QcProblemwithTcMedia", MAX), start.toDate(), end.toDate()));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "CerifyQCError", MAX), start.toDate(), end.toDate()));
		autoQc.writeAutoQc(events, start, end, reportName);
	}

//	@Transactional(readOnly=true)
//	public void getTaskListCSV(final DateTime start, final DateTime end, final String reportName)
//	{
//		List<EventEntity> tasks = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/preview", "TaskListReport", MAX), start.toDate(), end.toDate());
//		taskList.writeTaskList(tasks, start, end, reportName);
//	}	
	
	@Transactional(readOnly=true)
	public void getPurgeContentCSV(final DateTime start, final DateTime end, final String reportName)
	{
		List<AggregatedBMS> bms = populateBMS();
		List<EventEntity> purged = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "PurgeTitle", MAX), start.toDate(), end.toDate());
		purged.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeleteMaterial", MAX), start.toDate(), end.toDate()));
		purged.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeletePackage", MAX), start.toDate(), end.toDate()));
		purgeContent.writePurgeTitles(purged, bms, start, end, reportName);
	}

//	@Transactional(readOnly=true)
//	public void getComplianceEditCSV()
//	{
//		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("ComplianceLoggingMarker", MAX));
//		compliance.writeCompliance(events, startDate, endDate, REPORT_NAME);
//	}
//	
//	@Transactional(readOnly=true)
//	public void getExportCSV()
//	{
//		populateTitles();
//		populatePackages();
//		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("CaptionProxySuccess", MAX));
//		events.addAll(getInDate(queryApi.getByEventNameWindow("ComplianceProxySuccess", MAX)));
//		events.addAll(getInDate(queryApi.getByEventNameWindow("ClassificationProxySuccess", MAX)));
//		export.writeExport(events, startDate, endDate, REPORT_NAME);
//	}
//	
//	@Transactional(readOnly=true)
//	public void getWatchFolderStorageCSV()
//	{
//		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("FilePickUp", MAX));
//		watchFolder.writeWatchFolder(events, startDate, endDate, REPORT_NAME);
//	}
//	
//	@Transactional(readOnly=true)
//	public void getTranscoderLoadCSV()
//	{
//		List<EventEntity> events = queryApi.getByNamespaceWindow("http://www.foxtel.com.au/ip/tc", MAX);
//		List<EventEntity> valid = new ArrayList<EventEntity>();
//		for (EventEntity event : events) {
//			boolean within = checkDate(event.getTime());
//			if (within)
//				valid.add(event);
//		}
//		transcoderLoad.writeTranscoderLoad(valid, startDate, endDate, REPORT_NAME);
//	}
}
