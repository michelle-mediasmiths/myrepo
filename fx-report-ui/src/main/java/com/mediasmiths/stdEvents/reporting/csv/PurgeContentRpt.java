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
	public static final transient Logger logger = Logger.getLogger(CsvImpl.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writePurgeTitles(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<Purge> purged = getPurgeList(events, startDate, endDate);
		logger.info("Events: " + events);
		logger.info("Purged: " + purged);
		ICsvBeanWriter beanWriter = null;
		try {
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "purgedContentCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "channel", "mediaID", "purgeStatus", "protectedStatus", "extendedStatus", "size"};
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
			if (payload.contains("titleID"))
				purge.setMediaID(payload.substring(payload.indexOf("titleID")+9, payload.indexOf('"', (payload.indexOf("titleID")+9))));
			if (payload.contains("PurgeStatus"))
				purge.setPurgeStatus(payload.substring(payload.indexOf("PurgeStatus")+12, payload.indexOf("</PurgeStatus")));
			if (payload.contains("ProtectedStatus"))
				purge.setProtectedStatus(payload.substring(payload.indexOf("ProtectedStatus")+16, payload.indexOf("</ProtectedStatus")));
			if (payload.contains("ExtendedStatus"))
				purge.setExtendedStatus(payload.substring(payload.indexOf("ExtendedStatus")+15, payload.indexOf("</ExtendedStatus")));
			purgeList.add(purge);
			
			List<EventEntity> channelEvents = queryApi.getByEventName("CreateOrUpdateTitle");
			for (EventEntity channelEvent : channelEvents)
			{
				String str = channelEvent.getPayload();
				String channelTitle = str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9)));
				if (payload.contains("titleID")) {
					String curTitle =  payload.substring(payload.indexOf("titleID")+9, payload.indexOf('"', (payload.indexOf("titleID")+9)));
					if (curTitle.equals(channelTitle)) {
						if (str.contains("channelName"))
							purge.setChannel(str.substring(str.indexOf("channelName")+13, str.indexOf('"',(str.indexOf("channelName")+13)))); 
					}
				}
			}
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
				new Optional()
		};
		return processors;
	}
}
