package com.mediasmiths.stdEvents.reporting.rest;

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
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.thymeleaf.TemplateCall;
import com.mediasmiths.std.guice.thymeleaf.Templater;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.jasper.JasperAPI;
import com.mediasmiths.stdEvents.reporting.csv.AcquisitionRpt;
import com.mediasmiths.stdEvents.reporting.csv.AutoQCRpt;
import com.mediasmiths.stdEvents.reporting.csv.ComplianceRpt;
import com.mediasmiths.stdEvents.reporting.csv.CsvAPI;
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
	private Templater templater;
	
	@Inject
	private QueryAPI queryApi;
	
	@Inject
	private JasperAPI jasperApi;
	
	@Inject
	private CsvAPI csvApi;
	
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
	public void saveStartDate(@QueryParam("date") String date, @QueryParam("month") String month, @QueryParam("year") String year)
	{
		startDay = Integer.parseInt(date);
		startMonth = Integer.parseInt(month);
		startYear = Integer.parseInt(year);
		
		Calendar startCal = Calendar.getInstance();
		startCal.set(startCal.SECOND, 00);
		startCal.set(startYear, startMonth, startDay, 00, 00);
		
		startLong = startCal.getTimeInMillis();
		
		startDate = new Date(startLong);
				
		logger.info("Start date: " + startCal + " Start long: " + startLong);
	}
	
	@Transactional
	public void saveEndDate(@QueryParam("date") String date, @QueryParam("month") String month, @QueryParam("year") String year)
	{
		endDay = Integer.parseInt(date);
		endMonth = Integer.parseInt(month);
		endYear = Integer.parseInt(year);
		
		Calendar endCal = Calendar.getInstance();
		endCal.set(endYear, endMonth, endDay, 00, 00);
		endCal.set(endCal.SECOND, 00);
		endLong = endCal.getTimeInMillis();
		
		endDate = new Date(endLong);

		logger.info("End date: " + endCal + " End long: " + endLong);
	}
	
	public boolean checkDate(Long eventTime)
	{
		boolean within = false;
		logger.info("getting valid " + eventTime + " " + startLong);
		
		int startComp = eventTime.compareTo(startLong);
		int endComp = eventTime.compareTo(endLong);
		logger.info("Start compare: " + startComp + " End compare: " + endComp);
		if ((startComp > 0) && (endComp < 0))
		{
			within = true;
		}
		return within;
	}
	
	public Date longToCal(Long longDate)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(longDate);
		Date date = cal.getTime();
		return date;
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
	public void getOrderStatusCSV()
	{
		List<EventEntity> delivered = getInDate(queryApi.getDelivered());
		
		List<EventEntity> outstanding = getInDate(queryApi.getOutstanding());
		//List<EventEntity> overdue = getInDate(queryApi.getOverdue());
		List<EventEntity> overdue = new ArrayList<EventEntity>();
 		List<EventEntity> unmatched = getInDate(queryApi.getEvents("http://www.foxtel.com.au/ip/content", "UnmatchedContentAvailable"));
 		logger.info("writeOrderStatus: " + REPORT_NAME);
		orderStatus.writeOrderStatus(delivered, outstanding, overdue, unmatched, startDate, endDate, REPORT_NAME);
	}
	
	public List<EventEntity> getInDate(List<EventEntity> events)
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
	public String getAquisitionReportUI()
	{
		final TemplateCall call = templater.template("acquisition_delivery");
		
		call.set("tapeEvents", queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"));
		call.set("fileEvents", queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File"));

		call.set("noByTape", queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape")));
		call.set("noByFile", queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File")));
		
		int total = (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"))) + (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File")));
		int perByTape = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape")) / total;
		int perByFile = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File")) / total;

		perByTape = perByTape * 100;
		perByFile = perByFile * 100;
		
		call.set("perByTape", perByTape);
		call.set("perByFile", perByFile);

		return call.process();
	}
	
	@Transactional
	public void getAquisitionReportCSV()
	{
		List<EventEntity> materials = queryApi.getByNamespace("http://www.foxtel.com.au/ip/content");
		List<EventEntity> valid = new ArrayList<EventEntity>();
		for (EventEntity event : materials) {
			boolean within = checkDate(event.getTime());
			logger.info("Start long: " + startLong + " End long: " + endLong + " Current long: " + event.getTime() + " Valid: " + within);
			if (within)
				valid.add(event);
		}
		acquisition.writeAcquisitionDelivery(valid, startDate, endDate, REPORT_NAME);
		
		int total = (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"))) + (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File")));
		int perByFile = 0;
		int perByTape = 0;
		if (total != 0)
		{
			perByTape = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape")) / total;
			perByFile = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File")) / total;
		}

		perByTape = perByTape * 100;
		perByFile = perByFile * 100;
	}

	@Transactional
	public void getManualQACSV()
	{
		List<EventEntity> preview = queryApi.getByEventName("ManualQA");
//		List<EventEntity> manualQA = new ArrayList<EventEntity>();
//		for (EventEntity event : qc) 
//		{
//			String payload = event.getPayload();
//			String qcStatus = payload.substring(payload.indexOf("QCStatus")+9, payload.indexOf("</QCStatus"));
//			if (qcStatus.equals("QCFail(Overridden)")) {
//				boolean within = checkDate(event.getTime());
//				if (within)
//					manualQA.add(event);
//			}
//		}
		manualQa.writeManualQA(preview, startDate, endDate, REPORT_NAME);
	}

	@Transactional
	public void getAutoQCCSV()
	{
		List<EventEntity> passed = queryApi.getByEventName("AutoQC");
		List<EventEntity> valid = new ArrayList<EventEntity>();
		for (EventEntity event : passed) {
			boolean within = checkDate(event.getTime());
			logger.info("Current: " + event.getTime() + " Valid: " + within);
			if (within)
				valid.add(event);
		}
		autoQc.writeAutoQc(valid, startDate, endDate, REPORT_NAME);
	}

	@Transactional
	public void getTaskListCSV()
	{
		List<EventEntity> tasks = queryApi.getByEventName("TaskList");
		//NEEED TEST DATA TO SEND TO REPORT
		taskList.writeTaskList(tasks, startDate, endDate, REPORT_NAME);
	}	
	
	@Transactional
	public void getPurgeContentCSV()
	{
		List<EventEntity> title = queryApi.getByEventName("PurgeTitle");
		List<EventEntity> material = queryApi.getByEventName("DeleteMaterial");
		List<EventEntity> pack = queryApi.getByEventName("DeletePackage");
		List<EventEntity> manual = queryApi.getByEventName("ManualPurge");
		List<EventEntity> purged = new ArrayList<EventEntity>();
		purged.addAll(title);
		purged.addAll(material);
		purged.addAll(pack);
		purged.addAll(manual);
		logger.info("Purged: " + purged);
		List<EventEntity> valid = new ArrayList<EventEntity>();
		for (EventEntity event : purged) {
			boolean within = checkDate(event.getTime());
			logger.info("Current: " + event.getTime() + " Valid: " + within);
			if (within)
				valid.add(event);
		}
		purgeContent.writePurgeTitles(valid, startDate, endDate, REPORT_NAME);
	}

	@Transactional
	public void getComplianceEditCSV()
	{
		List<EventEntity> events = new ArrayList<EventEntity>();
		//NEED TEST DATA TO SEND REPORT
		compliance.writeCompliance(events);
	}
	
	@Transactional
	public void getExportCSV()
	{
		List<EventEntity> events = new ArrayList<EventEntity>();
		//NEED TEST DATA TO SEND TO REPORT
		export.writeExport(events);
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
		List<EventEntity> events = queryApi.getByNamespace("http://www.foxtel.com.au/ip/tc");
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
//
//	@Transactional
//	public String getAutoQCUI()
//	{
//		final TemplateCall call = templater.template("auto_qc");
//
//		call.set("autoQCd", queryApi.getEvents("http://www.foxtel.com.au/ip/qc", "AutoQCPassed"));
//		call.set("totalQCd", queryApi.getLength(queryApi.getEvents("http://www.foxtel.com.au/ip/qc", "AutoQCPassed")));
//		call.set("QCFailed", queryApi.getLength(queryApi.getEvents("http://www.foxtel.com.au/ip/qc", "autoqcfailed")));
//
//		return call.process();
//	}
//	
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
