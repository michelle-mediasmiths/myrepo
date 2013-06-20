package com.mediasmiths.stdEvents.reporting.rest;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.mayam.MayamClientImpl;
import com.mediasmiths.std.guice.database.annotation.Transactional;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AutoQC;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ManualQAEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Purge;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

public class ReportUIImpl implements ReportUI
{
	@Inject
	private ThymeleafTemplater templater;
	@Inject
	private QueryAPIImpl queryApi;
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
	protected MayamClientImpl mayamClient;

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
		else if (rptType.equals("TaskList"))
		{
			logger.info("Generating Task List Report");
			getTaskListCSV(startDate, endDate, reportName);
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

		List<EventEntity> materials = queryApi.getEventsWindowDateRange("http://www.foxtel.com.au/ip/content",
		                                                                EventNames.PROGRAMME_CONTENT_AVAILABLE,
				MAX, start, end);
										
		materials.addAll(queryApi.getEventsWindowDateRange("http://www.foxtel.com.au/ip/content",
		                                                   EventNames.MARKETING_CONTENT_AVAILABLE,
				MAX, start, end));
		
		acquisition.writeAcquisitionDelivery(materials, start, end, reportName);

		logger.debug("<<<getAcquisitionReportCSV");
	}

	@Transactional(readOnly = true)
	private void getManualQACSV(final DateTime start, final DateTime end, final String reportName)
	{
		logger.debug(">>>getManualQACSV");
//		List<EventEntity> preview = queryApi.getEventsWindowDateRange("http://www.foxtel.com.au/ip/preview", EventNames.MANUAL_QA,
//				MAX, start, end);

		List<ManualQAEntity> preview = queryApi.getManualQAInDateRange(start,end);
		manualQa.writeManualQA(preview, start, end, reportName);
		logger.debug("<<<getManualQACSV");
	}

	@Transactional(readOnly = true)
	private void getAutoQCCSV(final DateTime start, final DateTime end, final String reportName)
	{
		
		List<AutoQC> autoQcItems = getAutoQcInDateRange(start, end);
		autoQc.writeAutoQc(autoQcItems, start, end, reportName);
	}

	private List<AutoQC> getAutoQcInDateRange(DateTime start, DateTime end)
	{
		return queryApi.getAutoQcInDateRange(start, end);
	}

	@Transactional(readOnly = true)
	private void getPurgeContentCSV(final DateTime start, final DateTime end, final String reportName)
	{

		List<Purge> purgeEntities = getPurgeEntitiesInDateRange(start, end);
		purgeContent.writePurgeTitles(purgeEntities, start, end, reportName);
	}


	private List<Purge> getPurgeEntitiesInDateRange(DateTime start, DateTime end)
	{
		return queryApi.getPurgeEventsInDateRange(start, end);
	}

	@Transactional(readOnly = true)
	private void getComplianceEditCSV(final DateTime start, final DateTime end, final String reportName)
	{
		List<EventEntity> events = queryApi.getByEventNameWindowDateRange(EventNames.COMPLIANCE_LOGGING_MARKER, MAX, start, end);
		compliance.writeCompliance(events, start, end, reportName);
	}

	@Transactional(readOnly = true)
	private void getExportCSV(final DateTime start, final DateTime end, final String reportName)
	{
		List<EventEntity> events = queryApi.getByEventNameWindowDateRange(EventNames.CAPTION_PROXY_SUCCESS, MAX, start, end);
		events.addAll(queryApi.getByEventNameWindowDateRange("ComplianceProxySuccess", MAX, start, end));
		events.addAll(queryApi.getByEventNameWindowDateRange(EventNames.CLASSIFICATION_PROXY_SUCCESS, MAX, start, end));
		export.writeExport(events, start, end, reportName);
	}

	@Transactional(readOnly = true)
	private void getWatchFolderStorageCSV(final DateTime start, final DateTime end, final String reportName)
	{
		List<EventEntity> events = queryApi.getByEventNameWindowDateRange(EventNames.FILE_PICK_UP_NOTIFICATION, MAX, start, end);
		watchFolder.writeWatchFolder(events, start, end, reportName);
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
	
	@Transactional(readOnly = true)
	private void getTaskListCSV(final DateTime start, final DateTime end, final String reportName)
	{
		logger.debug(">>>getTaskListCSV");

		List<AttributeMap> tasks = mayamClient.getTasksInDateRange(start.toDate(), end.toDate());

		String startDate = start.toString(dateFormatter);
		String endDate = end.toString(dateFormatter);
		logger.info("dates readable start: " + startDate + " end: " + endDate);
		logger.info("List size: " + tasks.size());
		logger.debug(String.format(
				"Requesting task list report for date range: %s to %s; report name will be: %s ",
				start.toString(),
				end.toString(),
				reportName));
		taskList.writeTaskList(tasks, start, end, reportName);

		logger.debug("<<<getTaskListCSV");
	}
}
