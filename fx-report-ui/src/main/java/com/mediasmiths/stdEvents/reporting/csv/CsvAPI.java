package com.mediasmiths.stdEvents.reporting.csv;

import java.util.List;

import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

public interface CsvAPI
{	
	public void writeOrderStatus(List<EventEntity> delivered, List<EventEntity> outstanding, List<EventEntity> overdue, List<EventEntity> unmatched);
	
	public void writeLateOrderStatus(List<EventEntity> outstanding, List<EventEntity> unmatched);
	
	public void writeAcquisitionDelivery(List<EventEntity> file, List<EventEntity> tape);
	
	public void writeFileTapeIngest(List<EventEntity> completed, List<EventEntity> failed, List<EventEntity> unmatched);
	
	public void writeAutoQc(List<EventEntity> passed);
	
	public void writePurgeTitles(List<EventEntity> purged);
}
