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
import com.mediasmiths.foxtel.ip.common.events.report.ComplianceLogging;
import com.mediasmiths.foxtel.ip.common.events.report.OrderStatus;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.ComplianceLoggingRT;

public class ComplianceRpt
{
	public static final transient Logger logger = Logger.getLogger(ComplianceRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeCompliance(List<EventEntity> events, Date startDate, Date endDate, String reportName)
	{
		List<ComplianceLogging> comps = getReportList(events, startDate, endDate);
		createCsv(comps, reportName);
	}
	
	private ComplianceLogging unmarshall(EventEntity event)
	{
		Object title = new ComplianceLogging();
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);

		try
		{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.report.ObjectFactory.class);
			logger.info("Deserialising payload");
			title = JAXB_SERIALISER.deserialise(payload);
			logger.info("Object created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return (ComplianceLogging)title;
	}
	
	private List<ComplianceLogging> getReportList(List<EventEntity> events, Date startDate, Date endDate)
	{
		logger.info("Creating complianceLogging report");
		List<ComplianceLogging> comps = new ArrayList<ComplianceLogging>();
		for (EventEntity event : events)
		{
			ComplianceLogging comp = unmarshall(event);
			comp.setDateRange(startDate + " - " + endDate);
			
			comps.add(comp);
		}
		return comps;
	}
	
	private void createCsv(List<ComplianceLogging> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "title", "materialID", "channels", "taskStatus", "taskStart", "taskFinish", "externalCompliance"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (ComplianceLogging title : titles)
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
				new Optional()
		};
		return processors;
	}
}

//ICsvBeanWriter beanWriter = null;
//try{
//	beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "complianceEditCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
//	final String[] header = {"dateRange", "title", "materialID", "channel", "taskStatus", "taskStart", "taskFinish", "externalCompliance"};
//	final CellProcessor[] processors = getComplianceProcessor();
//	beanWriter.writeHeader(header);
//	
//	ComplianceLoggingRT total = new ComplianceLoggingRT("Number of Titles", null);
//	ComplianceLoggingRT time = new ComplianceLoggingRT("Average Completion Time", null);
//	comps.add(total);
//	comps.add(time);
//	
//	for (ComplianceLoggingRT comp : comps)
//	{
//		beanWriter.write(comp, header, processors);
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
//String payload = event.getPayload();
//ComplianceLoggingRT comp = new ComplianceLoggingRT();
//
//comp.setDateRange(startDate + " - " + endDate);
//if (payload.contains("title"))
//	comp.setTitle(payload.substring(payload.indexOf("title")+6, payload.indexOf("</title")));
//if (payload.contains("materialID"))
//	comp.setMaterialID(payload.substring(payload.indexOf("materialID")+11, payload.indexOf("</materialID")));
//if (payload.contains("channel"))
//	comp.setChannel(payload.substring(payload.indexOf("channel")+8, payload.indexOf("</channel")));
//if (payload.contains("taskStatus"))
//	comp.setTaskStatus(payload.substring(payload.indexOf("taskStatus")+11, payload.indexOf("</taskStatus")));
//if (payload.contains("taskStart"))
//	comp.setTaskStart(payload.substring(payload.indexOf("taskStart")+10, payload.indexOf("</taskStart")));
//if (payload.contains("taskFinish"))
//	comp.setTaskFinish(payload.substring(payload.indexOf("taskFinish")+11, payload.indexOf("</taskFinish")));
//if (payload.contains("externalCompliance"))
//	comp.setExternalCompliance(payload.substring(payload.indexOf("externalCompliance")+19, payload.indexOf("</externalCompliance")));
