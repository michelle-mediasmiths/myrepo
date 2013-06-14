package com.mediasmiths.stdEvents.persistence.db.dao;

import com.google.inject.ImplementedBy;
import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AggregatedBMS;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import org.joda.time.DateTime;

import java.util.List;

@ImplementedBy(com.mediasmiths.stdEvents.persistence.db.impl.AggregatedBMSDaoImpl.class)
public interface AggregatedBMSDao extends Dao<AggregatedBMS, Long>
{
	public List<AggregatedBMS> findByAnId(String column, String id);
	
	public List<AggregatedBMS> completionDateNotNull();
	
	public Object unmarshall(String payload);
	
	public void updateBMS (EventEntity event);

	public List<AggregatedBMS> withinDate(DateTime start, DateTime end);
}
