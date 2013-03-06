package com.mediasmiths.foxtel.ip.mail.data.db.dao;

import java.util.List;

import com.mediasmiths.foxtel.ip.mail.data.db.entity.EventingTableEntity;
import com.mediasmiths.std.guice.database.dao.Dao;

public interface EventingTableDao extends Dao<EventingTableEntity, Long>
{
	public List<EventingTableEntity> findByNamespace(String namespace);

	public List<EventingTableEntity> findByEventName(String eventName);

	public List<EventingTableEntity> findUnique(String namespace, String eventName);

	EventingTableEntity getFirstID();

}
