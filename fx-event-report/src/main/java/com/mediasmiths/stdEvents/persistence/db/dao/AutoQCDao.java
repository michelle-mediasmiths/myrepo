package com.mediasmiths.stdEvents.persistence.db.dao;

import java.util.List;

import org.joda.time.DateTime;

import com.google.inject.ImplementedBy;
import com.mediasmiths.stdEvents.coreEntity.db.entity.AutoQC;
import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

@ImplementedBy(com.mediasmiths.stdEvents.persistence.db.impl.AutoQCDaoImpl.class)
public interface AutoQCDao extends Dao<AutoQC, String>
{
	void autoQCMessage(EventEntity event);

	List<AutoQC> getAutoQcInDateRange(DateTime start, DateTime end);
}
