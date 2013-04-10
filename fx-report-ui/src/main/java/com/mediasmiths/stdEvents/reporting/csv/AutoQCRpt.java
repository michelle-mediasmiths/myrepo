package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.AutoQCResultNotification;
import com.mediasmiths.foxtel.ip.common.events.report.AutoQC;
import com.mediasmiths.foxtel.ip.common.events.report.OrderStatus;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.AutoQCRT;

public class AutoQCRpt
{
	public static final transient Logger logger = Logger.getLogger(AutoQCRpt.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeAutoQc(List<EventEntity> passed, Date startDate, Date endDate, String reportName)
	{
		List<AutoQC> autoQcs = getReportList(passed, startDate, endDate);
		createCsv(autoQcs, reportName);
	}
	
	private Object unmarshall(EventEntity event)
	{
		Object title = null;
		String payload = event.getPayload();
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
	
	public List<AutoQC> getReportList(List<EventEntity> events, Date startDate, Date endDate)
	{
		logger.info("Creating autoQC list");
		List<AutoQC> autoQcs = new ArrayList<AutoQC>();
		for (EventEntity event : events)
		{
			AutoQCResultNotification result = (AutoQCResultNotification) unmarshall(event);
			AutoQC autoQc = new AutoQC();
			autoQc.setDateRange(startDate + " - " + endDate);
			autoQc.setMaterialID(result.getAssetId());
			autoQc.setTitle(result.getTitle());
			autoQc.setContentType("Programme");
			
			logger.info("event.eventName: " + event.getEventName());
			if(event.getEventName().equals("AutoQCPassed")) {
				autoQc.setQcStatus("QC_PASS");
				autoQc.setTaskStatus("FINISHED");
			}
			if(event.getEventName().equals("QcFailedReorder")) {
				autoQc.setQcStatus("QC_FAILED_REORDER");
				autoQc.setTaskStatus("FINISHED_FAILED");
			}
			else if(event.getEventName().equals("QcProblemWithTCMedia")) {
				autoQc.setQcStatus("UNKNOWN");
				autoQc.setTaskStatus("FINISHED_FAILED");
			}
			else if(event.getEventName().equals("CerifyQcError")) {
				autoQc.setQcStatus("UNKNOWN");
				autoQc.setTaskStatus("FINISHED_FAILED");
			}
			
			autoQcs.add(autoQc);
		}	
		return autoQcs;
	}
	
	private void createCsv(List<AutoQC> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "title", "materialID", "channels", "contentType", "operator", "taskStatus", "qcStatus", "taskStart", "taskFinish", "warningTime", "manualOverride", "failureParameter", "titleLength"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (AutoQC title : titles)
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
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional(),
				new Optional()
		};
		return processors;
	}
}

//ICsvBeanWriter beanWriter = null;
//try {
//	beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
//	final String[] header = {"dateRange", "title", "materialID", "channels", "contentType", "operator", "taskStatus", "qcStatus", "taskStart", "taskFinish", "warningTime", "manualOverride", "failureParameter", "titleLength"};
//	final CellProcessor[] processors = getAutoQCProcessor();
//	beanWriter.writeHeader(header);
//	
//	AutoQCRT totalPass = new AutoQCRT("Total OC'd", Integer.toString(autoQcs.size()));
//	AutoQCRT failed = new AutoQCRT("Failed QC", Integer.toString(queryApi.getLength(queryApi.getFailedQc())));
//	AutoQCRT overridden = new AutoQCRT("Operator Overridden", Integer.toString(queryApi.getLength(queryApi.getOperatorOverridden())));
//	autoQcs.add(totalPass);
//	autoQcs.add(failed);
//	autoQcs.add(overridden);
//	
//	AutoQCRT avConc = new AutoQCRT("Average Concurrant Titles", null);
//	AutoQCRT maxConc = new AutoQCRT("Max Concurrant Titles", null);
//	autoQcs.add(avConc);
//	autoQcs.add(maxConc);
//	
//	for (AutoQCRT autoQc : autoQcs)
//	{
//		beanWriter.write(autoQc, header, processors);
//	}
//}
//catch (IOException e)
//{
//	e.printStackTrace();
//}
//finally {
//	if (beanWriter != null)
//	{
//		try
//		{
//			beanWriter.close();
//		}
//		catch(IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
//}
//
//AutoQCRT autoQc = new AutoQCRT();
//autoQc.setDateRange(startDate.toString() + " - " + endDate.toString());
//String payload = event.getPayload();
//if (payload.contains("materialID"))
//	autoQc.setMaterialID(payload.substring(payload.indexOf("materialID") +11, payload.indexOf("</materialID")));
//if (payload.contains("title"))
//	autoQc.setTitle(payload.substring(payload.indexOf("title")+6, payload.indexOf("</title")));
//if (payload.contains("channels")) 
//	autoQc.setChannels(payload.substring(payload.indexOf("channels")+9, payload.indexOf("</channels")));
//if (payload.contains("contentType"))
//	autoQc.setContentType(payload.substring(payload.indexOf("contentType")+12, payload.indexOf("</contentType")));
//if (payload.contains("operator"))
//	autoQc.setOperator(payload.substring(payload.indexOf("operator")+9, payload.indexOf("</operator")));
//if (payload.contains("taskStatus"))
//	autoQc.setTaskStatus(payload.substring(payload.indexOf("taskStatus")+11, payload.indexOf("</taskStatus")));
//if (payload.contains("qcStatus"))
//	autoQc.setQcStatus(payload.substring(payload.indexOf("qcStatus")+9, payload.indexOf("</qcStatus")));
//autoQc.setTaskStart(new Date(event.getTime()).toString());
//if (payload.contains("taskFinish"))
//	autoQc.setTaskFinish(payload.substring(payload.indexOf("taskFinish")+11, payload.indexOf("</taskFinish")));
//if (payload.contains("warningTime"))
//	autoQc.setWarningTime(payload.substring(payload.indexOf("warningTime")+12, payload.indexOf("</warningTime")));
//if ((autoQc.getQcStatus() != null) && (autoQc.getQcStatus().contains("Overridden")))
//	autoQc.setManualOverride("1");
//if (payload.contains("failureParameter"))
//	autoQc.setFailureParameter(payload.substring(payload.indexOf("failureParameter")+17, payload.indexOf("</failureParameter")));
//if (payload.contains("titleLength"))
//	autoQc.setTitleLength(payload.substring(payload.indexOf("titleLength")+12, payload.indexOf("</titleLength")));
	
////	if (qcStatus.equals("QCPass"))
////	autoQc.setTaskStatus("Completed");
////else if (qcStatus.equals("QCNotDone"))
////	autoQc.setTaskStatus("Processing");
////else if (qcStatus.equals("QCFail"))
////	autoQc.setTaskStatus("Completed/ Failed");
////else if (qcStatus.equals("QCFail(Overridden)"))
////	autoQc.setTaskStatus("Completed/Failed");
//
////}
////
////autoQc.setTaskStart(new Date(event.getTime()).toString());
////
////List<EventEntity> channelEvents = queryApi.getByEventName("CreateOrUpdateTitle");
////for (EventEntity channelEvent : channelEvents)
////{
////String str = channelEvent.getPayload();
////String channelTitle = str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9)));
////if (payload.contains("AssetID")) {
////	String curTitle =  payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID"));
////	if (curTitle.equals(channelTitle)) {
////		if (str.contains("channelName"))
////			autoQc.setChannels(str.substring(str.indexOf("channelName")+13, str.indexOf('"',(str.indexOf("channelName")+13)))); 
////	}
////}
////}
////
////List<EventEntity> lengthEvents = queryApi.getByNamespace("http://www.foxtel.com.au/ip/content");
////for (EventEntity lengthEvent : lengthEvents)
////{
////String str = lengthEvent.getPayload();
////String lengthTitle = str.substring(str.indexOf("materialId") +11, str.indexOf("</materialId"));
////if (payload.contains("AssetID")) {
////	String curTitle = payload.substring(payload.indexOf("AssetID") +8, payload.indexOf("</AssetID"));
////	if (curTitle.equals(lengthTitle)) {
////		if (str.contains("Duration")) 
////			autoQc.setTitleLength(str.substring(str.indexOf("Duration") +9, str.indexOf("</Duration")));
////		if (lengthEvent.getEventName().equals("UnmatchedContentAvailable"))
////			autoQc.setContentType("Unmatched");
////		else
////			autoQc.setContentType("Programme");
////	}
////}
////}
//	
//	
//}
