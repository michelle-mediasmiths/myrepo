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
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.Purge;

public class PurgeContentRpt
{
	public static final transient Logger logger = Logger.getLogger(PurgeContentRpt.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writePurgeTitles(List<EventEntity> events, Date startDate, Date endDate, String reportName)
	{
		List<Purge> purged = getPurgeList(events, startDate, endDate);
		logger.info("Events: " + events);
		logger.info("Purged: " + purged);
		ICsvBeanWriter beanWriter = null;
		try {
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "entityType", "title", "materialID", "channels", "isProtected", "extended", "purged", "expires"};
			final CellProcessor[] processors = getPurgeProcessor();
			beanWriter.writeHeader(header);
			
			Purge expiring = new Purge("Expiring", Integer.toString(queryApi.getLength(queryApi.getExpiring())));
			Purge purgeProtected = new Purge("Total Protected", Integer.toString(queryApi.getLength(queryApi.getPurgeProtected())));
			Purge purgePosponed = new Purge("Total Postponed", Integer.toString(queryApi.getLength(queryApi.getPurgePosponed())));
			Purge total = new Purge("Total Purged", Integer.toString(queryApi.getLength(queryApi.getTotalPurged())));
			purged.add(expiring);
			purged.add(purgeProtected);
			purged.add(purgePosponed);
			purged.add(total);
			
			for (Purge purge : purged)
			{
				beanWriter.write(purge, header, processors);
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
	
	public List<Purge> getPurgeList(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<Purge> purgeList = new ArrayList<Purge>();
		for (EventEntity event : events)
		{
			String payload = event.getPayload();
			Purge purge = new Purge();
			purge.setDateRange(startDate.toString() + " - " + endDate.toString());
			
			if (payload.contains("entityType"))
				purge.setEntityType(payload.substring(payload.indexOf("entityType")+11, payload.indexOf("</entityType")));
			if (payload.contains("title"))
				purge.setEntityType(payload.substring(payload.indexOf("title")+7, payload.indexOf("</title")));
			if (payload.contains("materialID"))
				purge.setEntityType(payload.substring(payload.indexOf("materialID")+11, payload.indexOf("</materialID")));
			if (payload.contains("channels"))
				purge.setEntityType(payload.substring(payload.indexOf("channels")+10, payload.indexOf("</channels")));
			if (payload.contains("isProtected"))
				purge.setEntityType(payload.substring(payload.indexOf("isProtected")+12, payload.indexOf("</isProtected")));
			if (payload.contains("extended"))
				purge.setEntityType(payload.substring(payload.indexOf("extended")+9, payload.indexOf("</extended")));
			if (payload.contains("purged"))
				purge.setEntityType(payload.substring(payload.indexOf("purged")+7, payload.indexOf("</purged")));
			if (payload.contains("expires"))
				purge.setEntityType(payload.substring(payload.indexOf("expires")+8, payload.indexOf("</expires")));

			purgeList.add(purge);
		}
		
		return purgeList;
	}
	
	public CellProcessor[] getPurgeProcessor()
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
}
