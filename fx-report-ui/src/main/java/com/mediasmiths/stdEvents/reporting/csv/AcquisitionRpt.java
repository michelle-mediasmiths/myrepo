package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import com.mediasmiths.stdEvents.report.entity.AcquisitionDelivery;

public class AcquisitionRpt
{
	public static final transient Logger logger = Logger.getLogger(CsvImpl.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeAcquisitionDelivery(List<EventEntity> materials)
	{
		List<AcquisitionDelivery> titles = getAcquisitionDeliveryList(materials);
		
		ICsvBeanWriter beanWriter = null;
		try{
			beanWriter = new CsvBeanWriter(new FileWriter(REPORT_LOC + "acquisitionDeliveryCsv.csv"), CsvPreference.STANDARD_PREFERENCE);
			final String[] header = {"dateRange", "materialID", "channels", "aggregatorID", "tape", "file", "format", "fileSize", "duration"};
			final CellProcessor[] processors = getAcquisitionProcessor();
			beanWriter.writeHeader(header);
			
			AcquisitionDelivery noFile = new AcquisitionDelivery("No Delivered By File", Integer.toString(queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File"))));
			AcquisitionDelivery noTape = new AcquisitionDelivery("No Delivered By Tape", Integer.toString(queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"))));
			titles.add(noFile);
			titles.add(noTape);
			
			int total = (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"))) + (queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File")));
			logger.info("total: " + total);
			double perByTape = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "Tape"));
			double perByFile = queryApi.getTotal(queryApi.getByMedia("http://www.foxtel.com.au/ip/content", "ProgrammeContentAvailable", "File"));
			perByTape = (perByTape/total)*100;
			perByFile = (perByFile/total)*100;
			logger.info("Tape: " + perByTape + "File: " + perByFile);
			
			AcquisitionDelivery perFile = new AcquisitionDelivery("% By File", Double.toString(perByFile));
			AcquisitionDelivery perTape = new AcquisitionDelivery("% By Tape", Double.toString(perByTape));
			titles.add(perFile);
			titles.add(perTape);
			
			for (AcquisitionDelivery title : titles)
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
	
	public List<AcquisitionDelivery> getAcquisitionDeliveryList(List<EventEntity> events)
	{
		List<AcquisitionDelivery> titleList = new ArrayList<AcquisitionDelivery>();
		for(EventEntity event : events)
		{
			String payload = event.getPayload();
			AcquisitionDelivery title = new AcquisitionDelivery();
			if (payload.contains("materialId"))
			{
				String materialId = payload.substring(payload.indexOf("materialId") +11, payload.indexOf("</materialId"));
				title.setMaterialID(materialId);
			}
			
			List<EventEntity> channelEvents = queryApi.getByEventName("CreateOrUpdateTitle");
			for (EventEntity channelEvent : channelEvents)
			{
				String str = channelEvent.getPayload();
				String channelTitle = str.substring(str.indexOf("titleID")+9, str.indexOf('"', (str.indexOf("titleID")+9)));
				logger.info("Channel: " + channelTitle);
				if (payload.contains("materialId")) {
					String curTitle =  payload.substring(payload.indexOf("materialId") +11, payload.indexOf("</materialId"));
					logger.info("Current: " + curTitle);
					if (curTitle.equals(channelTitle)) 
						title.setChannels(str.substring(str.indexOf("channelName")+13, str.indexOf('"',(str.indexOf("channelName")+13)))); 
				}
			}
			
			List<EventEntity> aggregatorEvents = queryApi.getByEventName("AddOrUpdateMaterial");
			for (EventEntity aggregatorEvent : aggregatorEvents)
			{
				String str = aggregatorEvent.getPayload();
				String aggregatorTitle = str.substring(str.indexOf("materialID")+12, str.indexOf('"', (str.indexOf("materialID")+12)));
				logger.info("agg:" + aggregatorTitle);
				if (payload.contains("materialId")) {
					String curTitle =  payload.substring(payload.indexOf("materialId") +11, payload.indexOf("</materialId"));
					logger.info("Current " + curTitle);
					if (curTitle.equals(aggregatorTitle))
						title.setAggregatorID(str.substring(str.indexOf("aggregatorID")+14, str.indexOf('"',(str.indexOf("aggregatorID")+14))));
				}
			}
			
			if (payload.contains("Tape"))
					title.setTape("1");
			if (payload.contains("File"))
					title.setFile("1");
		
		if (payload.contains("Format"))
			title.setFormat(payload.substring(payload.indexOf("Format") +7, payload.indexOf("</Format")));
			
		if (payload.contains("Duration"))
		{
			String duration = payload.substring(payload.indexOf("Duration") +9, payload.indexOf("</Duration"));
			title.setDuration(duration);
		}
		
		titleList.add(title);	
		}
		return titleList;
	}
	
	public CellProcessor[] getAcquisitionProcessor()
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
