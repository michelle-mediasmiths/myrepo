package com.mediasmiths.stdEvents.reporting.csv;

import java.util.List;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

public interface CsvAPI
{	
	
	//public void writeLateOrderStatus(List<EventEntity> outstanding, List<EventEntity> unmatched);
	
	//public void writeAcquisitionDelivery(List<EventEntity> materials);
	
	//public void writeFileTapeIngest(List<EventEntity> completed, List<EventEntity> failed, List<EventEntity> unmatched);
	
	public void writeManualQA(List<EventEntity> events);
	
	public void writeAutoQc(List<EventEntity> passed);
	
	public void writePurgeTitles(List<EventEntity> purged);
}
