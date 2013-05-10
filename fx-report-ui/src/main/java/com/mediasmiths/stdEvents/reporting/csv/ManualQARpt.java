package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
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
import com.mediasmiths.foxtel.ip.common.events.report.OrderStatus;
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
	
	private ReportUIImpl report = new ReportUIImpl();
	
	private List<String> channels = new ArrayList<String>();
	
	private int total=0;
	private int escalated=0;
	private int failed=0;
	private int reordered=0;
	private int hr=0;
	
	public void writeManualQA(List<EventEntity> events, List<Acquisition> acqs, DateTime startDate, DateTime endDate, String reportName)
	{
		List<ManualQA> manualQAs = getReportList(events, acqs, startDate, endDate);
		//setStats(manualQAs);
		
		manualQAs.add(addStats("Total QA'd", Integer.toString(total)));
		manualQAs.add(addStats("Total Escalated", Integer.toString(escalated)));
		manualQAs.add(addStats("Total Failed QA", Integer.toString(failed)));
		manualQAs.add(addStats("Total Needs Reordered", Integer.toString(reordered)));
		manualQAs.add(addStats("Total QA'd HR", Integer.toString(hr)));
		
		createCsv(manualQAs, reportName);
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
	
	public List<ManualQA> getReportList(List<EventEntity> events, List<Acquisition> acqs, DateTime startDate, DateTime endDate)
	{
		logger.info("Creating manualQA list");
		List<ManualQA> manualQAList = new ArrayList<ManualQA>();
		
		for (EventEntity event : events)
		{
			ManualQANotification notification = (ManualQANotification) unmarshall(event);
			ManualQA manualQA = new ManualQA();
			
			manualQA.setDateRange(startDate + " - " + endDate);
			manualQA.setTitle(notification.getTitle());
			manualQA.setMaterialID(notification.getMaterialID());
			manualQA.setChannels(notification.getChannels());
			manualQA.setAggregatorID(notification.getAggregatorID());
			manualQA.setTaskStatus(notification.getTaskStatus());
			manualQA.setPreviewStatus(notification.getPreviewStatus());
			manualQA.setEscalated(notification.getEscalated());
			manualQA.setReordered(notification.getReordered());
			
			for (Acquisition acq : acqs) {
				if (acq.getMaterialID().equals(manualQA.getMaterialID()))
					manualQA.setTitleLength(acq.getTitleLength());
			}
			
			manualQAList.add(manualQA);
		}		
		return manualQAList;
	}
				
	private void createCsv(List<ManualQA> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "title", "materialID", "channels", "operator", "aggregatorID", "taskStatus", "previewStatus", "hrPreview", "hrPreviewRequested", "escalated", "timeEscalated", "titleLength", "reordered"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (ManualQA title : titles)
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
	
	private void setStats(List<ManualQA> events)
	{
		for (ManualQA event : events)
		{
			if (event.getTaskStatus().contains("FINISHED"))
				total ++;
			if ((event.getEscalated().equals("1")) || event.getEscalated().equals("2"))
				escalated ++;
			if (event.getTaskStatus().contains("FAILED"))
				failed ++;
			if (event.getReordered().equals("1"))
				reordered ++;
			if (event.getHrPreview().equals("1"))
				hr ++;
		}
	}
	
	private ManualQA addStats(String name, String value)
	{
		ManualQA qa = new ManualQA();
		qa.setTitle(name);
		qa.setMaterialID(value);
		return qa;
	}
}
