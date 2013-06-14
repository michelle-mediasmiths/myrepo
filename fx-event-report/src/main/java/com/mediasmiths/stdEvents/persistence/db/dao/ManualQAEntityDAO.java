package com.mediasmiths.stdEvents.persistence.db.dao;

import com.google.inject.ImplementedBy;
import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ManualQAEntity;
import org.joda.time.DateTime;

import java.util.List;

@ImplementedBy(com.mediasmiths.stdEvents.persistence.db.impl.ManualQCEntityDAOImpl.class)
public interface ManualQAEntityDAO extends Dao<ManualQAEntity, String>
{
	void manualQCMessage(EventEntity event);
	List<ManualQAEntity> getManualQAInDateRange(DateTime start, DateTime end);
}
