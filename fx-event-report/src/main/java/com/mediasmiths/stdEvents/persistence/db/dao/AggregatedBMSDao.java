package com.mediasmiths.stdEvents.persistence.db.dao;

import java.util.List;

import org.joda.time.DateTime;

import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AggregatedBMS;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

public interface AggregatedBMSDao extends Dao<AggregatedBMS, Long>
{
	public List<AggregatedBMS> findByAnId(String column, String id);
	
	public List<AggregatedBMS> completionDateNotNull();
	
	public Object unmarshall(String payload);
	
	public void updateBMS (EventEntity event);

	public List<AggregatedBMS> withinDate(DateTime start, DateTime end);
}
