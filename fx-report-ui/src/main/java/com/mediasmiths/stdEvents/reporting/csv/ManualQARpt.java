package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.foxtel.ip.common.events.ManualQANotification;
import com.mediasmiths.foxtel.ip.common.events.PreviewFailed;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ManualQARpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(ManualQARpt.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	

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
	
	public List<ManualQANotification> getReportList(List<EventEntity> events, DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>getReportList");
		
		List<ManualQANotification> manualQAs = new ArrayList<ManualQANotification>();
		List<EventEntity> pfa = queryApi.getByEventNameWindowDateRange(EventNames.PREVIEW_FURTHER_ANALYSIS, 500, startDate, endDate);
		List<PreviewFailed> previews = new ArrayList<PreviewFailed>();
		
		String startF = startDate.toString(dateFormatter);
		String endF = endDate.toString(dateFormatter);
		
		for (EventEntity event :pfa)
		{
			PreviewFailed preview = (PreviewFailed) unmarshallEvent(event);
			previews.add(preview);
		}
		
		for (EventEntity event : events)
		{
			ManualQANotification qa = (ManualQANotification) unmarshallEvent(event);
			
			qa.setDateRange(startF + " - " + endF);
			
			for (PreviewFailed preview : previews)
			{
				if (preview.getAssetId() != null)
				{
					if (preview.getAssetId().equals(qa.getMaterialID()))
					{
						logger.debug("matching preview further analysis event found " + preview.getAssetId());
						qa.setTimeEscalated(preview.getDate());
					}
				}
			}
			
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
				new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), 
				new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), new Optional(), new Optional()
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
