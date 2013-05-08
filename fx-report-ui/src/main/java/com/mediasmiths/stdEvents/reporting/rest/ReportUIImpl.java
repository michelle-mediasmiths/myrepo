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
	
	public List<EventEntity> acqEvents = new ArrayList<EventEntity>();
	public List<Acquisition> acqs = new ArrayList<Acquisition>();
	
	public void populateAcq()
	{
		acqEvents = queryApi.getByEventName("ProgrammeContentAvailable");
		acqEvents.addAll(queryApi.getByEventName("MarketingContentAvailable"));
		for (EventEntity event : acqEvents) {
			Acquisition acq = (Acquisition) unmarshallRpt(event);
			acqs.add(acq);
		}
	}
	
	/**
	 * Returns all the records in the BMS table
	 * @return
	 */
	public List<AggregatedBMS> populateBMS() 
	{
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
	
	public boolean checkDate(Long eventTime)
	{
		boolean within = false;
		
		int startComp = eventTime.compareTo(startLong);
		int endComp = eventTime.compareTo(endLong);
		if ((startComp > 0) && (endComp < 0))
		{
			within = true;
		}
		return within;
	}
	
	private List<EventEntity> getInDate(List<EventEntity> events)
	{
		List <EventEntity> valid = new ArrayList<EventEntity>();
		for (EventEntity event : events)
		{
			logger.info("event.getTime()" + event.getTime());
			boolean within = checkDate(event.getTime());
			if (within)
				valid.add(event);
		}
		return valid;
	}
	
	private List<AggregatedBMS> getInDateBMS(List<AggregatedBMS> events)
	{
			List<AggregatedBMS> valid = new ArrayList<AggregatedBMS>();
			for (AggregatedBMS bms : events) {
				boolean within = checkDate(bms.getTime());
				if (within)
					valid.add(bms);
			}
			return valid;
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
		logger.debug("start: " + start + " end: " + end);
		logger.debug("name: " + reportName + " rpt: " + rptType);
		
		Date startD = new Date();
		Date endD = new Date();
	
		if(StringUtils.isBlank(start)) 
		{
			startD = new Date();
			logger.debug(String.format("start date was null, setting to: %s" , startD.toString()));
		}
		else 
		{
			try
			{
				startD = new SimpleDateFormat("yyyy-MM-dd").parse(start);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
		
		if (StringUtils.isBlank(end)) {
			endD = new Date();
			logger.debug(String.format("end date was null, setting to: %s" , endD.toString()));
		}
		else
		{
			try
			{
				endD = new SimpleDateFormat("yyyy-MM-dd").parse(end);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
				
		//saveReportName(reportName);
		
		if (rptType.equals("OrderStatus")) {
			logger.info("generating OrderStatus__");
				getOrderStatusCSV(startD, endD, reportName);
		}
		else if (rptType.equals("AcquisitionDelivery")) {
			logger.info("generating AcquisitionDelivery__");
				getAquisitionReportCSV();

		}
		else if (rptType.equals("ManualQA")) {
			logger.info("generating ManualQA__");
				getManualQACSV();
		}
		else if (rptType.equals("AutoQC")) {
			logger.info("generating AutoQC__");
				getAutoQCCSV();
		}
		else if (rptType.equals("PurgeContent")) {
			logger.info("generating PurgeContent__");
				getPurgeContentCSV();
		}
		else if (rptType.equals("ComplianceEdits")) {
			logger.info("generating ComplianceEdits__");
				getComplianceEditCSV();
		}
		else if (rptType.equals("Export")) {
			logger.info("generating Export");
				getExportCSV();
		}
		else if (rptType.equals("Watchfolder")) {
			logger.info("generating Watchfolder__");
				getWatchFolderStorageCSV();;
		}
		logger.debug("<<<chooseReport");
		return null;		
	}

	@Transactional(readOnly=true)
	public void getOrderStatusCSV(Date start, Date end, String reportName)
	{
		logger.debug(">>>getOrderStatusCSV");
		
		List<AggregatedBMS> bms = populateBMS();
		List<AggregatedBMS> orders = getInDateBMS(bms);
		logger.info("List size: " + orders.size());
		logger.debug(String.format("Requesting order status report for date range: %s to %s; report name will be: %s ", start.toString(), end.toString(), reportName));
		orderStatus.writeOrderStatus(orders, start, end, reportName);
		logger.debug("<<<getOrderStatusCSV");
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
	public String getOrderStatusUI()
	{
		final TemplateCall call = templater.template("order_status");
		List<OrderStatus> orders = getReportList(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "CreateorUpdateTitle", MAX)));
		call.set("orders", orders);
		logger.info("OrderStatusUI complete");
		return call.process();
	}

	@Transactional(readOnly=true)
	public String getAquisitionReportUI()
	{
		populateTitles();
		populatePackages();
		populateAcq();
		final TemplateCall call = templater.template("acquisition_delivery");
		List<EventEntity> events = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", MAX));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/content", "MarketingContentAvailable", MAX)));
		AcquisitionRpt report = new AcquisitionRpt();
		List<Acquisition> materials = report.getReportList(events, startDate, endDate);
		call.set("materials", materials);
		return call.process();
	}
	
	@Transactional(readOnly=true)
	public void getAquisitionReportCSV()
	{
		populateTitles();
		populatePackages();
		populateAcq();
		List<EventEntity> materials = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", MAX));
		materials.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/content", "MarketingContentAvailable", MAX)));
		acquisition.writeAcquisitionDelivery(materials, startDate, endDate, REPORT_NAME);
	}

	@Transactional(readOnly=true)
	public void getManualQACSV()
	{
		populateAcq();
		List<EventEntity> preview = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/preview", "ManualQA", MAX));
		manualQa.writeManualQA(preview, startDate, endDate, REPORT_NAME);
	}
	
	@Transactional(readOnly=true)
	public String getManualQAUI()
	{
		populateAcq();
		TemplateCall call = templater.template("manual_qa");
		List<EventEntity> events = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/preview", "ManualQA", MAX));
		ManualQARpt report = new ManualQARpt();
		List<ManualQA> qas = report.getReportList(events, startDate, endDate);
		call.set("qas", qas);
		return call.process();
	}

	@Transactional(readOnly=true)
	public void getAutoQCCSV()
	{
		List<EventEntity> events = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "AutoQCPassed", MAX));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "QcFailedReOrder", MAX)));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "QcProblemwithTcMedia", MAX)));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "CerifyQCError", MAX)));
		autoQc.writeAutoQc(events, startDate, endDate, REPORT_NAME);
	}
	
	@Transactional(readOnly=true)
	public String getAutoQCUI()
	{
		final TemplateCall call = templater.template("auto_qc");
		List<EventEntity> events = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "AutoQCPassed", MAX));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "QcFailedReOrder", MAX)));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "QcProblemwithTcMedia", MAX)));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "CerifyQCError", MAX)));
		AutoQCRpt report = new AutoQCRpt();
		List<AutoQC> qcs = report.getReportList(events, startDate, endDate);
		call.set("qcs", qcs);
		return call.process();
	}

	@Transactional(readOnly=true)
	public void getTaskListCSV()
	{
		List<EventEntity> tasks = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/preview", "TaskListReport", MAX));
		taskList.writeTaskList(tasks, startDate, endDate, REPORT_NAME);
	}	
	
	@Transactional(readOnly=true)
	public void getPurgeContentCSV()
	{
		populateTitles();
		List<EventEntity> purged = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "PurgeTitle", MAX));
		purged.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeleteMaterial", MAX)));
		purged.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeletePackage", MAX)));
		purgeContent.writePurgeTitles(purged, startDate, endDate, REPORT_NAME);
	}
	
	@Transactional(readOnly=true)
	public String getPurgeContentUI()
	{
		populateTitles();
		TemplateCall call = templater.template("purge_content");
		PurgeContentRpt report = new PurgeContentRpt();
		List<PurgeContent> purged = report.getReportList(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "PurgeTitle", MAX)), startDate, endDate);
		purged.addAll(report.getReportList(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeleteMaterial", MAX)), startDate, endDate));
		purged.addAll(report.getReportList(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeletePackage", MAX)), startDate, endDate));
		call.set("purged", purged);
		return call.process();
	}

	@Transactional(readOnly=true)
	public void getComplianceEditCSV()
	{
		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("ComplianceLoggingMarker", MAX));
		compliance.writeCompliance(events, startDate, endDate, REPORT_NAME);
	}
	
	@Transactional(readOnly=true)
	public String getComplianceEditsUI()
	{
		TemplateCall call = templater.template("compliance_edit");
		ComplianceRpt report = new ComplianceRpt();
		List<ComplianceLogging> compliance = report.getReportList(getInDate(queryApi.getByEventNameWindow("ComplianceLoggingMarker", MAX)), startDate, endDate);
		call.set("compliance", compliance);
		return call.process();
	}
	
	@Transactional(readOnly=true)
	public void getExportCSV()
	{
		populateTitles();
		populatePackages();
		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("CaptionProxySuccess", MAX));
		events.addAll(getInDate(queryApi.getByEventNameWindow("ComplianceProxySuccess", MAX)));
		events.addAll(getInDate(queryApi.getByEventNameWindow("ClassificationProxySuccess", MAX)));
		export.writeExport(events, startDate, endDate, REPORT_NAME);
	}
	
	@Transactional(readOnly=true)
	public String getExportUI()
	{
		TemplateCall call = templater.template("export");
		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("CaptionProxySuccess", MAX));
		events.addAll(getInDate(queryApi.getByEventNameWindow("ComplianceProxySuccess", MAX)));
		events.addAll(getInDate(queryApi.getByEventNameWindow("ClassificationProxySuccess", MAX)));
		List<Export> exports = export.getReportList(events, startDate, endDate);
		call.set("exports", exports);
		return call.process();
	}
	
	@Transactional(readOnly=true)
	public void getWatchFolderStorageCSV()
	{
		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("FilePickUp", MAX));
		watchFolder.writeWatchFolder(events, startDate, endDate, REPORT_NAME);
	}
	
	@Transactional(readOnly=true)
	public String getWatchFolderStorageUI()
	{
		TemplateCall call = templater.template("watch_folder");
		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("FilePickUp", MAX));
		List<Watchfolder> files = watchFolder.getWatchFolderList(events, startDate, endDate);
		call.set("files", files);
		return call.process();
	}
	
	@Transactional(readOnly=true)
	public void getTranscoderLoadCSV()
	{
		List<EventEntity> events = queryApi.getByNamespaceWindow("http://www.foxtel.com.au/ip/tc", MAX);
		List<EventEntity> valid = new ArrayList<EventEntity>();
		for (EventEntity event : events) {
			boolean within = checkDate(event.getTime());
			if (within)
				valid.add(event);
		}
		transcoderLoad.writeTranscoderLoad(valid, startDate, endDate, REPORT_NAME);
	}

	@Transactional(readOnly=true)
	public String displayPath(@QueryParam("path")String path)
	{
		final TemplateCall call = templater.template("path_demo");
		call.set("path", path);
		return call.process();
	}
}
