package com.mediasmiths.foxtel.ip.mail.data.db.dao;

import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventTableEntity;
import com.mediasmiths.std.guice.database.dao.Dao;

import java.util.List;

public interface EventTableDao extends Dao<EventTableEntity, Long>
{
	public List<EventTableEntity> findByNamespace(String namespace);

	public List<EventTableEntity> findByEventName(String eventName);

	public List<EventTableEntity> findUnique(String namespace, String eventName);

	EventTableEntity getFirstID();

}
