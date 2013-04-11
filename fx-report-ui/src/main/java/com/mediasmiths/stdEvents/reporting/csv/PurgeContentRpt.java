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
import com.mediasmiths.foxtel.ip.common.events.AddOrUpdateMaterial;
import com.mediasmiths.foxtel.ip.common.events.AddOrUpdatePackage;
import com.mediasmiths.foxtel.ip.common.events.CreateOrUpdateTitle;
import com.mediasmiths.foxtel.ip.common.events.PurgeMaterial;
import com.mediasmiths.foxtel.ip.common.events.PurgePackage;
import com.mediasmiths.foxtel.ip.common.events.PurgeTitle;
import com.mediasmiths.foxtel.ip.common.events.report.PurgeContent;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;

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
		List<PurgeContent> purged = getReportList(events, startDate, endDate);
		createCsv(purged, reportName);
	}
	
	private Object unmarshall(EventEntity event)
	{
		Object purge = null;
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);

		try
		{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
			logger.info("Deserialising payload");
			purge = JAXB_SERIALISER.deserialise(payload);
			logger.info("Object created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return purge;
	}
	
	public List<PurgeContent> getReportList(List<EventEntity> events, Date startDate, Date endDate)
	{
		List<EventEntity> titleEvents = queryApi.getByEventName("CreateOrUpdateTitle");
		List<CreateOrUpdateTitle> titles = new ArrayList<CreateOrUpdateTitle>();
		for (EventEntity event : titleEvents) {
			CreateOrUpdateTitle title = (CreateOrUpdateTitle) unmarshall(event);
			titles.add(title);
		}
		
//		List<EventEntity> materialEvents = queryApi.getByEventName("AddOrUpdateMaterial");
//		List<AddOrUpdateMaterial> materials = new ArrayList<AddOrUpdateMaterial>();
//		for (EventEntity event : materialEvents) {
//			AddOrUpdateMaterial material = (AddOrUpdateMaterial) unmarshall(event);
//			materials.add(material);
//		}
//		
//		List<EventEntity> packageEvents = queryApi.getByEventName("AddOrUpdatePackage");
//		List<AddOrUpdatePackage> packages = new ArrayList<AddOrUpdatePackage>();
//		for (EventEntity event : packageEvents) {
//			AddOrUpdatePackage pack = (AddOrUpdatePackage) unmarshall(event);
//			packages.add(pack);
//		}
		
		logger.info("Creating purgeContent list");
		List<PurgeContent> purgeList = new ArrayList<PurgeContent>();
		for (EventEntity event : events)
		{
			PurgeContent purge = new PurgeContent();
			purge.setDateRange(startDate + " - " + endDate);
			
			if (event.getEventName().equals("PurgeTitle"))
			{
				PurgeTitle title = (PurgeTitle) unmarshall(event);
				purge.setEntityType("Item");
				purge.setMaterialID(title.getTitleID());
				for (CreateOrUpdateTitle createTitle : titles) {
					if (purge.getMaterialID().equals(createTitle.getTitleID()))
						purge.setTitle(createTitle.getTitle());
				}
			}
			
			if (event.getEventName().equals("DeleteMaterial"))
			{
				PurgeMaterial material = (PurgeMaterial) unmarshall(event);
				purge.setEntityType("Subprogramme");
				purge.setMaterialID(material.getMaterialID());
			}
			
			if (event.getEventName().equals("DeletePackage"))
			{
				PurgePackage pack = (PurgePackage) unmarshall(event);
				purge.setEntityType("TX Package");
				purge.setMaterialID(pack.getPackageID());						
			}

			purgeList.add(purge);
		}
		return purgeList;
	}
	
	private void createCsv(List<PurgeContent> titles, String reportName)
	{
		ICsvBeanWriter beanWriter = null;
		try {
			logger.info("reportName: " + reportName);
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
			logger.info("Saving to: " + REPORT_LOC);
			final String[] header = {"dateRange", "entityType", "title", "materialID", "channels", "protected", "extended", "purged", "expires"};
			final CellProcessor[] processors = getProcessor();
			beanWriter.writeHeader(header);
				
			
			for (PurgeContent title : titles)
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
}

//logger.info("Events: " + events);
//logger.info("Purged: " + purged);
//ICsvBeanWriter beanWriter = null;
//try {
//	beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + reportName + ".csv"), CsvPreference.STANDARD_PREFERENCE);
//	final String[] header = {"dateRange", "entityType", "title", "materialID", "channels", "isProtected", "extended", "purged", "expires"};
//	final CellProcessor[] processors = getPurgeProcessor();
//	beanWriter.writeHeader(header);
//	
//	PurgeRT expiring = new PurgeRT("Expiring", Integer.toString(queryApi.getLength(queryApi.getExpiring())));
//	PurgeRT purgeProtected = new PurgeRT("Total Protected", Integer.toString(queryApi.getLength(queryApi.getPurgeProtected())));
//	PurgeRT purgePosponed = new PurgeRT("Total Postponed", Integer.toString(queryApi.getLength(queryApi.getPurgePosponed())));
//	PurgeRT total = new PurgeRT("Total Purged", Integer.toString(queryApi.getLength(queryApi.getTotalPurged())));
//	purged.add(expiring);
//	purged.add(purgeProtected);
//	purged.add(purgePosponed);
//	purged.add(total);
//	
//	for (PurgeRT purge : purged)
//	{
//		beanWriter.write(purge, header, processors);
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
//PurgeRT purge = new PurgeRT();
//purge.setDateRange(startDate.toString() + " - " + endDate.toString());
//
//if (payload.contains("entityType"))
//	purge.setEntityType(payload.substring(payload.indexOf("entityType")+11, payload.indexOf("</entityType")));
//if (payload.contains("title"))
//	purge.setEntityType(payload.substring(payload.indexOf("title")+7, payload.indexOf("</title")));
//if (payload.contains("materialID"))
//	purge.setEntityType(payload.substring(payload.indexOf("materialID")+11, payload.indexOf("</materialID")));
//if (payload.contains("channels"))
//	purge.setEntityType(payload.substring(payload.indexOf("channels")+10, payload.indexOf("</channels")));
//if (payload.contains("isProtected"))
//	purge.setEntityType(payload.substring(payload.indexOf("isProtected")+12, payload.indexOf("</isProtected")));
//if (payload.contains("extended"))
//	purge.setEntityType(payload.substring(payload.indexOf("extended")+9, payload.indexOf("</extended")));
//if (payload.contains("purged"))
//	purge.setEntityType(payload.substring(payload.indexOf("purged")+7, payload.indexOf("</purged")));
//if (payload.contains("expires"))
//	purge.setEntityType(payload.substring(payload.indexOf("expires")+8, payload.indexOf("</expires")));