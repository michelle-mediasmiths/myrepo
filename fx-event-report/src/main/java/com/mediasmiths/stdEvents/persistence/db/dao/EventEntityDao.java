package com.mediasmiths.stdEvents.persistence.db.dao;

import com.google.inject.ImplementedBy;
import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import org.joda.time.DateTime;

import java.util.List;

@ImplementedBy( com.mediasmiths.stdEvents.persistence.db.impl.EventEntityDaoImpl.class)
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

	public List<EventEntity> getByNamePaged(String eventName, int start, int max);

}
