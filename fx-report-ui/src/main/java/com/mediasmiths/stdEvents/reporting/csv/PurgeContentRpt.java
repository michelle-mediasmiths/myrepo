package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import com.mediasmiths.foxtel.ip.common.events.PurgeMaterial;
import com.mediasmiths.foxtel.ip.common.events.PurgeMessage;
import com.mediasmiths.foxtel.ip.common.events.PurgePackage;
import com.mediasmiths.foxtel.ip.common.events.PurgeTitle;
import com.mediasmiths.foxtel.ip.common.events.report.PurgeContent;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.reporting.utils.ReportUtils;

public class PurgeContentRpt extends ReportUtils
{
	public static final transient Logger logger = Logger.getLogger(PurgeContentRpt.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writePurgeTitles(List<EventEntity> events, DateTime startDate, DateTime endDate, String reportName)
	{
		List<PurgeMessage> purged = getReportList(events, startDate, endDate);

		createCsv(purged, reportName);
	}
	
	public List<PurgeMessage> getReportList(final List<EventEntity> events, final DateTime startDate, final DateTime endDate)
	{
		logger.debug(">>>getReportList");
		
		final List<PurgeMessage> purged = new ArrayList<PurgeMessage>();
		
		for (EventEntity event : events)
		{
			PurgeMessage purge = new PurgeMessage();
			
			String startF = startDate.toString(dateFormatter);
			String endF = endDate.toString(dateFormatter);
			
			purge.setDateRange(new StringBuilder().append(startF).append(" - ").append(endF).toString());
			
			String titleID = null;
			
			if (event.getEventName().equals("PurgeTitle"))
			{
				PurgeTitle title = (PurgeTitle) unmarshallEvent(event);
				purge.setMaterialID(title.getTitleID());
				purge.setEntityType("Item");
				titleID = title.getTitleID();
			}
			else if (event.getEventName().equals("DeleteMaterial"))
			{
				PurgeMaterial material = (PurgeMaterial) unmarshallEvent(event);
				purge.setMaterialID(material.getMaterialID());
				purge.setEntityType("Sub-programme");
				titleID = material.getTitleID();
			}
			else if (event.getEventName().equals("DeletePackage"))
			{
				PurgePackage pack = (PurgePackage) unmarshallEvent(event);
				purge.setMaterialID(pack.getPackageID());
				purge.setEntityType("Tx-package");
				titleID = pack.getTitleID();
			}
			
			Title details = queryApi.getTitleById(titleID);
			purge.setTitle(details.getTitle());
			purge.setChannels(details.getChannels().toString());
			
			purged.add(purge);
		}
		
		logger.debug("<<<getReportList");
		return purged;
	}
	
	private void createCsv(List<PurgeMessage> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "entityType", "title", "materialID", "channels", "protected", "extended", "purged", "expires"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (PurgeMessage title : titles)
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
				new Optional()
		};
		return processors;
	}
	
	private PurgeContent addStats(String name, String value)
	{
		PurgeContent purge = new PurgeContent();
		purge.setTitle(name);
		purge.setMaterialID(value);
		return purge;
	}
}