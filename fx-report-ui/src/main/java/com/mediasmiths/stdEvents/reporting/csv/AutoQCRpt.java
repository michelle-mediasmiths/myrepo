package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.AutoQCResultNotification;
import com.mediasmiths.foxtel.ip.common.events.report.AutoQC;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.AutoQCRT;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;

public class AutoQCRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(AutoQCRpt.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeAutoQc(List<EventEntity> passed, DateTime startDate, DateTime endDate, String reportName)
	{
		List<AutoQCResultNotification> autoQcs = getReportList(passed, startDate, endDate);
		
		createCsv(autoQcs, reportName);
	}
	
	public List<AutoQCResultNotification> getReportList(List<EventEntity> events, DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>getReportList");
		
		List<AutoQCResultNotification> aqcs = new ArrayList<AutoQCResultNotification>();
		
		for (EventEntity event : events)
		{
			AutoQCResultNotification aqc = (AutoQCResultNotification) unmarshallEvent(event);
			
			String startF = startDate.toString(dateFormatter);
			String endF = endDate.toString(dateFormatter);
			
			aqc.setDateRange(new StringBuilder().append(startF).append(" - ").append(endF).toString());
			
			OrderStatus order = queryApi.getOrderStatusById(aqc.getMaterialID());
			if (order.getTitle() != null)
			{
				aqc.setChannels(order.getTitle().getChannels().toString());
			}
			aqcs.add(aqc);
		}
		
		logger.debug("<<<getReportList");
		return aqcs;
	}
	
	private void createCsv(List<AutoQCResultNotification> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "title", "materialID", "channels", "contentType", "operator", "taskStatus", "qcStatus", "taskStart", "taskFinish", "warningTime", "manualOverride", "failureParameter", "titleLength"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (AutoQCResultNotification title : titles)
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
	
	private AutoQC addStats(String name, String value)
	{
		AutoQC autoQc = new AutoQC();
		autoQc.setTitle(name);
		autoQc.setMaterialID(value);
		return autoQc;
	}
}