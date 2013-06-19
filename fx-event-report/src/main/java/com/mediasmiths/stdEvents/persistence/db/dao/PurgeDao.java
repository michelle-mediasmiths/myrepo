package com.mediasmiths.stdEvents.persistence.db.dao;

import com.google.inject.ImplementedBy;
import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Purge;
import org.joda.time.DateTime;

import java.util.List;

@ImplementedBy(com.mediasmiths.stdEvents.persistence.db.impl.PurgeDaoImpl.class)
public interface PurgeDao extends Dao<Purge, String>
{
	void purgeMessage(EventEntity event);
	List<Purge> getPurgeInDateRange(DateTime start, DateTime end);
}
