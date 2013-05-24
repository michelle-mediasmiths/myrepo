package com.mediasmiths.stdEvents.persistence.db.dao;

import java.util.List;

import org.joda.time.DateTime;

import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

public interface EventEntityDao extends Dao<EventEntity, Long>
{
	public List<EventEntity> findByNamespace(String namespace);

	public List<EventEntity> findByEventName(String eventName);

	public List<EventEntity> findUnique(String namespace, String eventName);
		
	public void saveFile (String eventString);

	public List<EventEntity> findUniquePaginatedDate(
			String namespace,
			String eventname,
			int start,
			int max,
			DateTime startDate,
			DateTime end);

	public List<EventEntity> eventnamePaginatedDate(String eventname, int start, int max, DateTime startDate, DateTime endDate);

}
