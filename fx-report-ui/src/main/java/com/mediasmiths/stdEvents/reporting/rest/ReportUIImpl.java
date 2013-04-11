package com.mediasmiths.stdEvents.reporting.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

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
import com.mediasmiths.foxtel.ip.common.events.report.OrderStatus;
import com.mediasmiths.foxtel.ip.common.events.report.PurgeContent;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

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
	public String getParent()
	{
		TemplateCall call = templater.template("parent");
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
		//logger.info("getting valid " + eventTime + " " + startLong);
		
		int startComp = eventTime.compareTo(startLong);
		int endComp = eventTime.compareTo(endLong);
		//logger.info("Start compare: " + startComp + " End compare: " + endComp);
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
	
	@Transactional
	public void chooseReport(@QueryParam("start")String start, @QueryParam("end")String end, @QueryParam("name")String name, @QueryParam("rpt")String rpt, @QueryParam("type") String type)
	{
		logger.info("start: " + start + " end: " + end);
		logger.info("name: " + name + " rpt: " + rpt);
		logger.info("type: " + type);
		
		saveStartDate(start);
		saveEndDate(end);
		saveReportName(name);
		
		boolean csv = false;
		boolean ui = false;
		if (type.equals("csv"))
			csv = true;
		else if (type.equals("ui"))
			ui = true;
		logger.info("csv=" + csv + " ui=" + ui);
		
		if (rpt.equals("OrderStatus")) {
			logger.info("generating OrderStatus__");
			if (csv)
				getOrderStatusCSV();
			if (ui)
				getOrderStatusUI();
		}
		else if (rpt.equals("AcquisitionDelivery")) {
			logger.info("generating AcquisitionDelivery__");
			if (csv)
				getAquisitionReportCSV();
			if (ui)
				getAquisitionReportUI();
		}
		else if (rpt.equals("AutoQC")) {
			logger.info("generating AutoQC__");
			if (csv)
				getAutoQCCSV();
			if (ui)
				getAutoQCUI();
		}
		else if (rpt.equals("PurgeContent")) {
			logger.info("generating PurgeContent__");
			if (csv)
				getPurgeContentCSV();
			if (ui)
				getPurgeContentUI();
		}
		else if (rpt.equals("ComplianceEdits")) {
			logger.info("generating ComplianceEdits__");
			if (csv)
				getComplianceEditCSV();
		}
		else if (rpt.equals("Export")) {
			logger.info("generating Export");
			if (csv)
				getExportCSV();
			if (ui)
				getExportUI();
		}		
	}

	@Transactional
	public void getOrderStatusCSV()
	{
		
 		logger.info("writeOrderStatus: " + REPORT_NAME + " max: " + MAX);
 		List<EventEntity> orders = getInDate(queryApi.getByEventNameWindow("CreateorUpdateTitle", MAX));
 		logger.info("List size: " + orders.size());
		orderStatus.writeOrderStatus(orders, startDate, endDate, REPORT_NAME);
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

		try
		{
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
	
	@Transactional
	public String getOrderStatusUI()
	{
		final TemplateCall call = templater.template("order_status");
		List<OrderStatus> orders = getReportList(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "CreateorUpdateTitle", MAX)));
		call.set("orders", orders);
		logger.info("OrderStatusUI complete");
		return call.process();
	}

	@Transactional
	public String getAquisitionReportUI()
	{
		final TemplateCall call = templater.template("acquisition_delivery");
		List<EventEntity> events = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", MAX));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/content", "MarketingContentAvailable", MAX)));
		AcquisitionRpt report = new AcquisitionRpt();
		List<Acquisition> materials = report.getReportList(events, startDate, endDate);
		call.set("materials", materials);
		return call.process();
	}
	
	@Transactional
	public void getAquisitionReportCSV()
	{
		List<EventEntity> materials = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", MAX));
		materials.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/content", "MarketingContentAvailable", MAX)));
		acquisition.writeAcquisitionDelivery(materials, startDate, endDate, REPORT_NAME);
	}

	@Transactional
	public void getManualQACSV()
	{
		List<EventEntity> preview = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/preview", "ManualQAReport", MAX));
		manualQa.writeManualQA(preview, startDate, endDate, REPORT_NAME);
	}

	@Transactional
	public void getAutoQCCSV()
	{
		List<EventEntity> events = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "AutoQCPassed", MAX));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "QcFailedReOrder", MAX)));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "QcProblemwithTcMedia", MAX)));
		events.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "CerifyQCError", MAX)));
		autoQc.writeAutoQc(events, startDate, endDate, REPORT_NAME);
	}
	
	@Transactional
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

	@Transactional
	public void getTaskListCSV()
	{
		List<EventEntity> tasks = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/preview", "TaskListReport", MAX));
		taskList.writeTaskList(tasks, startDate, endDate, REPORT_NAME);
	}	
	
	@Transactional
	public void getPurgeContentCSV()
	{
		List<EventEntity> purged = getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "PurgeTitle", MAX));
		purged.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeleteMaterial", MAX)));
		purged.addAll(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeletePackage", MAX)));
		purgeContent.writePurgeTitles(purged, startDate, endDate, REPORT_NAME);
	}
	
	@Transactional
	public String getPurgeContentUI()
	{
		TemplateCall call = templater.template("purge_content");
		PurgeContentRpt report = new PurgeContentRpt();
		List<PurgeContent> purged = report.getReportList(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "PurgeTitle", MAX)), startDate, endDate);
		purged.addAll(report.getReportList(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeleteMaterial", MAX)), startDate, endDate));
		purged.addAll(report.getReportList(getInDate(queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeletePackage", MAX)), startDate, endDate));
		call.set("purged", purged);
		return call.process();
	}

	@Transactional
	public void getComplianceEditCSV()
	{
		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("ComplianceLoggingMarker", MAX));
		compliance.writeCompliance(events, startDate, endDate, REPORT_NAME);
	}
	
	@Transactional
	public String getComplianceEditsUI()
	{
		TemplateCall call = templater.template("compliance_edit");
		ComplianceRpt report = new ComplianceRpt();
		List<ComplianceLogging> compliance = report.getReportList(getInDate(queryApi.getByEventNameWindow("ComplianceLoggingMarker", MAX)), startDate, endDate);
		call.set("comps", compliance);
		return call.process();
	}
	
	@Transactional
	public void getExportCSV()
	{
		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("CaptionProxySuccess", MAX));
		events.addAll(getInDate(queryApi.getByEventNameWindow("ComplianceProxySuccess", MAX)));
		events.addAll(getInDate(queryApi.getByEventNameWindow("ClassificationProxySuccess", MAX)));
		export.writeExport(events, startDate, endDate, REPORT_NAME);
	}
	
	@Transactional
	public String getExportUI()
	{
		TemplateCall call = templater.template("export");
		ExportRpt report = new ExportRpt();
		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("CaptionProxySuccess", MAX));
		events.addAll(getInDate(queryApi.getByEventNameWindow("ComplianceProxySuccess", MAX)));
		events.addAll(getInDate(queryApi.getByEventNameWindow("ClassificationProxySuccess", MAX)));
		List<Export> exports = report.getReportList(events, startDate, endDate);
		call.set("exports", exports);
		return call.process();
	}
	
	@Transactional
	public void getWatchFolderStorageCSV()
	{
		List<EventEntity> events = new ArrayList<EventEntity>();
		//NEED TEST DATA TO SEND REPORT
		watchFolder.writeWatchFolder(events, startDate, endDate, REPORT_NAME);
	}
	
	@Transactional
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

	@Override
	public String displayPath(@QueryParam("path")String path)
	{
		final TemplateCall call = templater.template("path_demo");
		call.set("path", path);
		return call.process();
	}

//	@Transactional
//	public String getOrderStatusUI()
//	{
//		final TemplateCall call = templater.template("order_status");
//		
//		call.set("delivered", queryApi.getDelivered());
//		call.set("deliveredQ", queryApi.getLength(queryApi.getDelivered()));
//		call.set("notDelivered", queryApi.getOutstanding());
//		call.set("notDeliveredQ", queryApi.getLength(queryApi.getOutstanding()));
//		call.set("overdue", queryApi.getOverdue());
//		call.set("overdueQ", queryApi.getLength(queryApi.getOverdue()));
//		call.set("outstanding", queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable"));
//		call.set("outstandingQ", queryApi.getLength(queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable")));
//
//		return call.process();
//	}	
//	
//	@Transactional
//	public void getOrderStatusPDF()
//	{
//		List<EventEntity> delivered = queryApi.getDelivered();
//		List<EventEntity> outstanding = queryApi.getOutstanding();
//		List<EventEntity> overdue = queryApi.getOverdue();
// 		List<EventEntity> unmatched = queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable");
//
// 		String deliveredQ = Integer.toString(queryApi.getLength(queryApi.getDelivered()));
// 		String notDeliveredQ = Integer.toString(queryApi.getLength(queryApi.getOutstanding()));
// 		String overdueQ = Integer.toString(queryApi.getLength(queryApi.getOverdue()));
// 		String unmatchedQ = Integer.toString(queryApi.getLength(queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable")));
// 		
// 		logger.info("Creating pdf jasper report");
//		jasperApi.createOrderStatus(delivered, outstanding, overdue, unmatched,
//				deliveredQ, notDeliveredQ, overdueQ, unmatchedQ);	
//	}
//
//	@Transactional
//	public String getLateOrderStatusUI()
//	{
//		final TemplateCall call = templater.template("late_order_status");
//
//		call.set("outstanding", queryApi.getOutstanding());
//		call.set("outstandingQ", queryApi.getTotal(queryApi.getOutstanding()));
//		call.set("unmatched", queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable"));
//		call.set("unmatchedQ", queryApi.getLength(queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable")));
//
//		return call.process();
//	}
//	
//	@Transactional
//	public void getLateOrderStatusCSV()
//	{
//		List<EventEntity> outstanding = queryApi.getOutstanding();
//		List<EventEntity> unmatched = queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable");
//		
//		//csvApi.writeLateOrderStatus(outstanding, unmatched);
//		
//		String outstandingQ = Integer.toString(queryApi.getTotal(queryApi.getOutstanding()));	
//		String unmatchedQ = Integer.toString(queryApi.getLength(queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable")));
//	}
//	
//	@Transactional
//	public void getLateOrderStatusPDF()
//	{
//		List<EventEntity> outstanding = queryApi.getOutstanding();
//		List<EventEntity> unmatched = queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable");
//				
//		String outstandingQ = Integer.toString(queryApi.getTotal(queryApi.getOutstanding()));	
//		String unmatchedQ = Integer.toString(queryApi.getLength(queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable")));
//
//		jasperApi.createLateOrderStatus(outstanding, unmatched, outstandingQ, unmatchedQ);
//	}
//
//	
//	@Transactional
//	public void getAquisitionReportPDF()
//	{
//		List <EventEntity> rptList = queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape");
//		rptList.addAll(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File"));
//		
//		int total = (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"))) + (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File")));
//		int perByTape = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape")) / total;
//		int perByFile = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File")) / total;
//
//		perByTape = perByTape * 100;
//		perByFile = perByFile * 100;
//
//		jasperApi.createAcquisition(rptList, Integer.toString(queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"))), Integer.toString(queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File"))), Integer.toString(perByTape), Integer.toString(perByFile));
//	}
//
//	@Transactional
//	public String getFileTapeIngestUI()
//	{
//		final TemplateCall call = templater.template("file_tape_ingest");
//		
//		call.set("completed", queryApi.getEvents("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable"));
//		call.set("completedFormats", queryApi.getFormat(queryApi.getEvents("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable")));
//		call.set("failed", queryApi.getEvents("http://www.foxtel.com.au/ip/content", "failed"));
//		call.set("unmatched", queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable"));
//
//		return call.process();
//	}
//	
//	@Transactional
//	public void getFileTapeIngestCSV()
//	{
//		List<EventEntity> completed = queryApi.getEvents("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable");
//		List<EventEntity> failed = queryApi.getEvents("http://www.foxtel.com.au/ip/content", "failed");
//		List<EventEntity> unmatched = queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable");
//
//		csvApi.writeFileTapeIngest(completed, failed, unmatched);
//	}
//	
//	@Transactional
//	public void getFileTapeIngestPDF()
//	{
//		List<EventEntity> rptList = queryApi.getEvents("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable");
//		rptList.addAll(queryApi.getEvents("http://www.foxtel.com.au/ip/content", "failed"));
//		rptList.addAll(queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable"));
//		
//		jasperApi.createFileTapeIngest(rptList);
//	}
//		
//	@Transactional
//	public void getAutoQCPDF()
//	{
//		List<EventEntity> passed = queryApi.getEvents("http://www.foxtel.com.au/ip/qc", "AutoQCPassed");
//		
//		jasperApi.createAutoQc(queryApi.getEvents("http://www.foxtel.com.au/ip/qc", "AutoQCPassed"), Integer.toString(queryApi.getLength(queryApi.getEvents("http://www.foxtel.com.au/ip/qc", "AutoQCPassed"))), Integer.toString(queryApi.getLength(queryApi.getEvents("http://www.foxtel.com.au/ip/qc", "autoqcfailed"))));	
//	}
//	
//	@Transactional
//	public String getPurgeContentUI()
//	{
//		final TemplateCall call = templater.template("purge_content");
//
//		call.set("purged", queryApi.getEvents("http://www.foxtel.com.au/ip/bms", "PurgeTitle"));
//
//		call.set("amtPurged", queryApi.getTotal(queryApi.getEvents("http://www.foxtel.com.au/ip/bms", "PurgeTitle")));
//		call.set("amtExpired", queryApi.getLength(queryApi.getExpiring()));
//		call.set("amtProtected", queryApi.getTotal(queryApi.getProtected()));
//
//		return call.process();
//	}
//	@Transactional
//	public void getPurgeContentPDF()
//	{
//		List<EventEntity> purged = queryApi.getEvents("http://www.foxtel.com.au/ip/bms", "PurgeTitle");
//		jasperApi.createPurgeContent(queryApi.getEvents("http://www.foxtel.com.au/ip/bms", "PurgeTitle"), Integer.toString(queryApi.getTotal(queryApi.getEvents("http://www.foxtel.com.au/ip/bms", "PurgeTitle"))), Integer.toString(queryApi.getLength(queryApi.getExpiring())), Integer.toString(queryApi.getTotal(queryApi.getProtected())));
//	}
//
//	@Transactional
//	public String getComplianceEditsUI()
//	{
//		final TemplateCall call = templater.template("compliance_edit");
//		call.set("compliance", queryApi.getCompliance());
//		call.set("complianceQ", queryApi.getLength(queryApi.getCompliance()));
//		return call.process();
//	}
	
}
