package com.mediasmiths.stdEvents.persistence.db.dao;

import com.google.inject.ImplementedBy;
import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ExtendedPublishing;
import com.mediasmiths.stdEvents.persistence.db.impl.ExtendedPublishingDaoImpl;
import org.joda.time.DateTime;

import java.util.List;

@ImplementedBy(ExtendedPublishingDaoImpl.class)
public interface ExtendedPublishingDao extends Dao<ExtendedPublishing, Long>
{
	void extendedPublishingEvent(EventEntity event);
	List<ExtendedPublishing> getExtendedPublishingInDateRange(DateTime start, DateTime end);
}
