package com.mediasmiths.stdEvents.reporting.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.report.entity.AcquisitionDelivery;
import com.mediasmiths.stdEvents.report.entity.AutoQC;
import com.mediasmiths.stdEvents.report.entity.ManualQA;
import com.mediasmiths.stdEvents.report.entity.OrderStatus;
import com.mediasmiths.stdEvents.report.entity.Purge;

public class CsvImpl implements CsvAPI
{
	public static final transient Logger logger = Logger.getLogger(CsvImpl.class);
	
	//Edit this variable to change where your reports get saved to
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	
//	public void writeLateOrderStatus(List<EventEntity> outstanding, List<EventEntity> unmatched)
//	{
//		List<OrderStatus> outstandingList = getTitleList(outstanding, "Outstanding");
//		List<OrderStatus> unmatchedList = getMaterialList(unmatched, "Unmatched");
//		List<OrderStatus> titles = new ArrayList<OrderStatus>();
//		titles.addAll(outstandingList);
//		titles.addAll(unmatchedList);
//		
//		createOrderStatusCsv(titles, "lateOrderStatusCsv");
//	}
	
	
	
	
	
	
	
//	public void writeFileTapeIngest(List<EventEntity> completed, List<EventEntity> failed, List<EventEntity> unmatched)
//	{
//		logger.info("Completed: " + completed);
//		List<OrderStatus> completedTitles = getMaterialList(completed, "Completed");
//		List<OrderStatus> failedTitles = getMaterialList(failed, "Failed");
//		List<OrderStatus> unmatchedTitles = getMaterialList(unmatched, "Unmatched");
//		logger.info("Completed: " + completedTitles);
//		List<OrderStatus> titles = new ArrayList<OrderStatus>();
//		titles.addAll(completedTitles);
//		titles.addAll(failedTitles);
//		titles.addAll(unmatchedTitles);
//		
//		createOrderStatusCsv(titles, "fileTapeIngestCsv");
//	}

	
	
	
	
	
	
	
	
	
	


	
	

	

	

}
