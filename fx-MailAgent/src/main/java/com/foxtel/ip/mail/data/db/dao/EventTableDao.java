package com.foxtel.ip.mail.data.db.dao;

import java.util.List;

import com.foxtel.ip.mail.data.db.entity.EventTableEntity;
import com.mediasmiths.std.guice.database.dao.Dao;

public interface EventTableDao extends Dao<EventTableEntity, Long>
{
	public List<EventTableEntity> findByNamespace(String namespace);

	public List<EventTableEntity> findByEventName(String eventName);

	public List<EventTableEntity> findUnique(String namespace, String eventName);

	EventTableEntity getFirstID();

}