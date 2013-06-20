package com.mediasmiths.stdEvents.reporting.csv;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.foxtel.ip.common.events.TcEvent;
import com.mediasmiths.foxtel.ip.common.events.report.Export;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
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

public class ExportRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(ExportRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPIImpl queryApi;
	
	public void writeExport(List<EventEntity> events, DateTime startDate, DateTime endDate, String reportName)
	{
		List<Export> exports = getReportList(events, startDate , endDate);
		
		int compliance=0;
		int captioning=0;
		int publicity=0;
		
		for (Export export : exports)
		{
			if (export.getExportType().equals("compliance"))
			{
				compliance ++;
			}
			else if (export.getExportType().equals("caption"))
			{
				captioning ++;
			}
			else if (export.getExportType().equals("classification"))
			{
				publicity ++;
			}
		}
		
		exports.add(addStats("No. of Compliance", Integer.toString(compliance)));
		exports.add(addStats("No. of Captioning", Integer.toString(captioning)));
		exports.add(addStats("No. of Publicity", Integer.toString(publicity)));
		
		createCsv(exports, reportName);
	}
	
	public List<Export> getReportList(List<EventEntity> events, DateTime startDate, DateTime endDate)
	{
		logger.debug(">>>getReportList");
		
		List<Export> exports = new ArrayList<Export>();
		
		String startF = startDate.toString(dateFormatter);
		String endF = endDate.toString(dateFormatter);
		
		for (EventEntity event : events)
		{
			TcEvent tc = (TcEvent) unmarshallEvent(event);
			Export export = new Export();
			
			export.setDateRange(new StringBuilder().append(startF).append(" - ").append(endF).toString());
			
			if (event.getEventName().equals(EventNames.CAPTION_PROXY_SUCCESS))
			{
				export.setMaterialID(tc.getPackageID());
				export.setExportType("caption");
			}
			else if (event.getEventName().equals("ComplianceProxySuccess"))
			{
				export.setMaterialID(tc.getAssetID());
				export.setExportType("compliance");
			}
			else if (event.getEventName().equals(EventNames.CLASSIFICATION_PROXY_SUCCESS))
			{
				export.setMaterialID(tc.getAssetID());
				export.setExportType("classification");
			}
			
			if (export.getMaterialID() != null)
			{
				OrderStatus order = queryApi.getOrderStatusById(export.getMaterialID());
				if (order != null)
				{
					logger.debug("matching title found " + export.getMaterialID());
					if (order.getTitle() != null)
					{
						export.setChannels(order.getTitle().getChannels().toString());
						export.setTitle(order.getTitle().getTitle());
					}
				}
			}
			exports.add(export);
		}
		
		logger.debug("<<<getReportList");
		return exports;
	}
	
	private void createCsv(List<Export> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "title", "materialID", "channels", "taskStatus", "exportType", "titleLength"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (Export title : titles)
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
				new Optional()
		};
		return processors;
	}
	
	private Export addStats (String name, String value)
	{
		Export export = new Export();
		export.setTitle(name);
		export.setMaterialID(value);
		return export;
	}
}

