package com.mediasmiths.stdEvents.reporting.csv;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;

public class TranscoderLoadRpt
{
	public static final transient Logger logger = Logger.getLogger(TranscoderLoadRpt.class);
	
	@Inject
	@Named("reportLoc")
	public String REPORT_LOC;
	
	@Inject
	private QueryAPI queryApi;
	
	public void writeTranscoderLoad(List<EventEntity> events, Date startDate, Date endDate, String reportName)
	{
		logger.debug(">>>writeTranscoderLoad");
	
	}
	
	public CellProcessor[] getTranscoderProcessor()
	{
		final CellProcessor[] processors = new CellProcessor[] {
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
