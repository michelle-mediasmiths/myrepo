package com.mediasmiths.stdEvents.reporting.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AggregatedBMS;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
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

	@Inject
	@Named("windowMax")
	public int MAX;

	private static transient final Logger logger = Logger.getLogger(ReportUIImpl.class);

	private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd-MM-yyyy");

	@Override
	@Transactional(readOnly = true)
	public void chooseReport(final String start, final String end, final String reportName, final String rptType)
	{
		logger.debug(">>>chooseReport");
		logger.debug(String.format(
				"Received startdate: %s; enddate: %s; reportName: %s; rptType: %s",
				start,
				end,
				reportName,
				rptType));

		DateTime startDate = StringUtils.isBlank(start) ? new DateTime() : dateFormatter.parseDateTime(start);
		DateTime endDate = StringUtils.isBlank(end) ? new DateTime() : dateFormatter.parseDateTime(end);
		logger.debug(String.format("Start date formatted: %s; End date formatted: %s", startDate.toString(), endDate.toString()));

		if (rptType.equals("OrderStatus"))
		{
			logger.info("Generating OrderStatus Report");
			getOrderStatusCSV(startDate, endDate, reportName);
		}
		else if (rptType.equals("AcquisitionDelivery"))
		{
			logger.info("Generating AcquisitionDelivery Report");
			getAquisitionReportCSV(startDate, endDate, reportName);
		}
		else if (rptType.equals("ManualQA"))
		{
			logger.info("Generating ManualQA Report");
			getManualQACSV(startDate, endDate, reportName);
		}
		else if (rptType.equals("AutoQC"))
		{
			logger.info("Generating AutoQC Report");
			getAutoQCCSV(startDate, endDate, reportName);
		}
		else if (rptType.equals("PurgeContent"))
		{
			logger.info("Generating PurgeContent Report");
			getPurgeContentCSV(startDate, endDate, reportName);
		}
		else if (rptType.equals("ComplianceEdits"))
		{
			logger.info("Generating ComplianceEdits Report");
			getComplianceEditCSV(startDate, endDate, reportName);
		}
		else if (rptType.equals("Export"))
		{
			logger.info("Generating Export Report");
			getExportCSV(startDate, endDate, reportName);
		}
		else if (rptType.equals("Watchfolder"))
		{
			logger.info("Generating Watchfolder Report");
			getWatchFolderStorageCSV(startDate, endDate, reportName);
		}
		logger.debug("<<<chooseReport");
	}

	@Override
	@Transactional
	public String getPopup()
	{
		TemplateCall call = templater.template("pop-up");
		return call.process();
	}

	private List<Acquisition> populateAcquisitions()
	{
		List<Acquisition> acqs = new ArrayList<Acquisition>();
		List<EventEntity> acqEvents = queryApi.getByEventName("ProgrammeContentAvailable");
		acqEvents.addAll(queryApi.getByEventName("MarketingContentAvailable"));
		for (EventEntity event : acqEvents)
		{
			Acquisition acq = (Acquisition) unmarshallRpt(event);
			acqs.add(acq);
		}
		return acqs;
	}

	/**
	 * Returns all the records in the BMS table
	 * 
	 * @return
	 */
	private List<AggregatedBMS> populateBMS()
	{
		logger.debug(">>>populateBMS");
		return queryApi.getAllBMS();
	}

	@Transactional
	private String getReport()
	{
		TemplateCall call = templater.template("report");
		return call.process();
	}

	@Transactional
	private String getByNamespace(final String namespace)
	{
		final TemplateCall call = templater.template("search");
		call.set("events", queryApi.getByNamespace(namespace));
		return call.process();
	}

	@Transactional
	private String getByNamespaceWindow(final String namespace)
	{
		final TemplateCall call = templater.template("search");
		logger.info("ReportUIImpl max: " + MAX);
		call.set("events", queryApi.getByNamespaceWindow(namespace, MAX));
		return call.process();
	}

	@Transactional
	private String getByEventName(final String eventname)
	{
		final TemplateCall call = templater.template("search");
		call.set("events", queryApi.getByEventName(eventname));
		return call.process();
	}

	@Transactional
	private String getByNamespaceEventname(final String namespace, final String eventname)
	{
		final TemplateCall call = templater.template("search");
		call.set("events", queryApi.getEvents(namespace, eventname));
		return call.process();
	}

	private boolean checkDate(final Long eventTime, final Date start, final Date end)
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

	private List<EventEntity> getInDate(final List<EventEntity> events, final Date start, final Date end)
	{
		logger.debug(">>>getInDate");
		List<EventEntity> valid = new ArrayList<EventEntity>();
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

	@Transactional
	private String getById(final Long id)
	{
		final TemplateCall call = templater.template("search");
		call.set("events", queryApi.getById(id));
		return call.process();
	}

	private Object unmarshallRpt(final EventEntity event)
	{
		Object rpt = null;
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);
		try
		{
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

	@Transactional(readOnly = true)
	private void getOrderStatusCSV(final DateTime start, final DateTime end, final String reportName)
	{
		logger.debug(">>>getOrderStatusCSV");

		List<OrderStatus> orders = getOrdersInDateRange(start, end);

		String startDate = start.toString(dateFormatter);
		String endDate = end.toString(dateFormatter);
		logger.info("dates readable start: " + startDate + " end: " + endDate);
		logger.info("List size: " + orders.size());
		logger.debug(String.format(
				"Requesting order status report for date range: %s to %s; report name will be: %s ",
				start.toString(),
				end.toString(),
				reportName));
		orderStatus.writeOrderStatus(orders, start, end, reportName);

		logger.debug("<<<getOrderStatusCSV");
	}

	private List<OrderStatus> getOrdersInDateRange(DateTime start, DateTime end)
	{
		return queryApi.getOrdersInDateRange(start, end);
	}

	@Transactional(readOnly = true)
	private void getAquisitionReportCSV(final DateTime start, final DateTime end, final String reportName)
	{
		logger.debug(">>>getAcquisitionReportCSV");
		logger.debug("start: " + start + " end: " + end);

		List<EventEntity> materials = queryApi.getEventsWindowDateRange("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", 
				MAX, start, end);
										
		materials.addAll(queryApi.getEventsWindowDateRange("http://www.foxtel.com.au/ip/content", "MarketingContentAvailable", 
				MAX, start, end));
		acquisition.writeAcquisitionDelivery(materials, start, end, reportName);

		logger.debug("<<<getAcquisitionReportCSV");
	}

	@Transactional(readOnly = true)
	private void getManualQACSV(final DateTime start, final DateTime end, final String reportName)
	{
		logger.debug(">>>getManualQACSV");
		List<EventEntity> preview = queryApi.getEventsWindowDateRange("http://www.foxtel.com.au/ip/preview", "ManualQA", 
				MAX, start, end);
		manualQa.writeManualQA(preview, start, end, reportName);
		logger.debug("<<<getManualQACSV");
	}

	@Transactional(readOnly = true)
	private void getAutoQCCSV(final DateTime start, final DateTime end, final String reportName)
	{
		List<EventEntity> events = getInDate(
				queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "AutoQCPassed", MAX),
				start.toDate(),
				end.toDate());
		events.addAll(getInDate(
				queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "QcFailedReOrder", MAX),
				start.toDate(),
				end.toDate()));
		events.addAll(getInDate(
				queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "QcProblemwithTcMedia", MAX),
				start.toDate(),
				end.toDate()));
		events.addAll(getInDate(
				queryApi.getEventsWindow("http://www.foxtel.com.au/ip/qc", "CerifyQCError", MAX),
				start.toDate(),
				end.toDate()));
		autoQc.writeAutoQc(events, start, end, reportName);
	}

	@Transactional(readOnly = true)
	private void getPurgeContentCSV(final DateTime start, final DateTime end, final String reportName)
	{
		List<AggregatedBMS> bms = populateBMS();
		List<EventEntity> purged = getInDate(
				queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "PurgeTitle", MAX),
				start.toDate(),
				end.toDate());
		purged.addAll(getInDate(
				queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeleteMaterial", MAX),
				start.toDate(),
				end.toDate()));
		purged.addAll(getInDate(
				queryApi.getEventsWindow("http://www.foxtel.com.au/ip/bms", "DeletePackage", MAX),
				start.toDate(),
				end.toDate()));
		purgeContent.writePurgeTitles(purged, bms, start, end, reportName);
	}

	@Transactional(readOnly = true)
	private void getComplianceEditCSV(final DateTime start, final DateTime end, final String reportName)
	{
		//TODO - IMPLEMENT
		/*List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("ComplianceLoggingMarker", MAX));
		compliance.writeCompliance(events, startDate, endDate, REPORT_NAME);*/
	}

	@Transactional(readOnly = true)
	private void getExportCSV(final DateTime start, final DateTime end, final String reportName)
	{
		//TODO - IMPLEMENT
		/*populateTitles();
		populatePackages();
		List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("CaptionProxySuccess", MAX));
		events.addAll(getInDate(queryApi.getByEventNameWindow("ComplianceProxySuccess", MAX)));
		events.addAll(getInDate(queryApi.getByEventNameWindow("ClassificationProxySuccess", MAX)));
		export.writeExport(events, startDate, endDate, REPORT_NAME);*/
	}

	@Transactional(readOnly = true)
	private void getWatchFolderStorageCSV(final DateTime start, final DateTime end, final String reportName)
	{
		//TODO - IMPLEMENT
		/*List<EventEntity> events = getInDate(queryApi.getByEventNameWindow("FilePickUp", MAX));
		watchFolder.writeWatchFolder(events, startDate, endDate, REPORT_NAME);*/
	}

	@Transactional(readOnly = true)
	private void getTranscoderLoadCSV(final DateTime start, final DateTime end, final String reportName)
	{
		//TODO - IMPLEMENT
		/*List<EventEntity> events = queryApi.getByNamespaceWindow("http:www.foxtel.com.au/ip/tc", MAX);
		List<EventEntity> valid = new ArrayList<EventEntity>();
		for (EventEntity event : events)
		{
			boolean within = checkDate(event.getTime());
			if (within)
				valid.add(event);
		}
		transcoderLoad.writeTranscoderLoad(valid, startDate, endDate, REPORT_NAME);*/
	}
}
