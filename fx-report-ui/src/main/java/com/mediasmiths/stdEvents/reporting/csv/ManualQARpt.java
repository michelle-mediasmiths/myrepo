package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.ManualQANotification;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.foxtel.ip.common.events.report.ManualQA;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.ManualQART;
import com.mediasmiths.stdEvents.reporting.rest.ReportUIImpl;

public class ManualQARpt
{
	public static final transient Logger logger = Logger.getLogger(ManualQARpt.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	private static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd-MM-yyyy");

	public void writeManualQA(List<EventEntity> events, DateTime startDate, DateTime endDate, String reportName)
	{
		logger.debug(">>>writeManualQA");
		List<ManualQANotification> manualQAs = getReportList(events, startDate, endDate);

		int total=0;
		int failed=0;
		int reordered=0;
		int escalated=0;
		
		for (ManualQANotification qa : manualQAs)
		{
			total ++;
			if (qa.getTaskStatus().contains("FAILED"))
			{
				failed ++;
			}
			if (qa.getReordered().contains("1"))
			{
				reordered ++;
			}
			if (qa.getEscalated().contains("1"))
			{
				escalated ++;
			}
		}
		
		manualQAs.add(addStats("Total QA'd", Integer.toString(total)));
		manualQAs.add(addStats("Total Failed QA", Integer.toString(failed)));
		manualQAs.add(addStats("Total Needs Reordered", Integer.toString(reordered)));
		manualQAs.add(addStats("Total Escalated", Integer.toString(escalated)));
		
		createCsv(manualQAs, reportName);
		logger.debug("<<<writeManualQA");
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
	
	public List<ManualQANotification> getReportList(List<EventEntity> events, DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>getReportList");
		
		List<ManualQANotification> manualQAs = new ArrayList<ManualQANotification>();
		
		for (EventEntity event : events)
		{
			ManualQANotification qa = (ManualQANotification) unmarshall(event);
			
			String startF = startDate.toString(dateFormatter);
			String endF = endDate.toString(dateFormatter);
			
			qa.setDateRange(startF + " - " + endF);
			
			manualQAs.add(qa);
		}
		logger.debug("<<<getReportList");	
		return manualQAs;
	}
				
	private void createCsv(List<ManualQANotification> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "title", "materialID", "channels", "operator", "aggregatorID", "taskStatus", "previewStatus", "hrPreview", "hrPreviewRequested", "escalated", "timeEscalated", "titleLength", "reordered"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (ManualQANotification title : titles)
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
	
	private ManualQANotification addStats(String name, String value)
	{
		ManualQANotification qa = new ManualQANotification();
		qa.setTitle(name);
		qa.setMaterialID(value);
		return qa;
	}
}
