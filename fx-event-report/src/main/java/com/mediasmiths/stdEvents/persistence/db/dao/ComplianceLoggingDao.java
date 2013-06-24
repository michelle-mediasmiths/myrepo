package com.mediasmiths.stdEvents.persistence.db.dao;

import com.google.inject.ImplementedBy;
import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.ComplianceLogging;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.persistence.db.impl.ComplianceLoggingDaoImpl;
import org.joda.time.DateTime;

import java.util.List;

@ImplementedBy(ComplianceLoggingDaoImpl.class)
public interface ComplianceLoggingDao extends Dao<ComplianceLogging, Long>
{

	public void complianceEvent(EventEntity event);
	List<ComplianceLogging> getComplianceByDate(DateTime start, DateTime end);
}
