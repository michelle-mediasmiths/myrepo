package com.mediasmiths.stdEvents.persistence.db.dao;

import com.google.inject.ImplementedBy;
import com.mediasmiths.std.guice.database.dao.Dao;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.TranscodeJob;
import org.joda.time.DateTime;

import java.util.List;

@ImplementedBy(com.mediasmiths.stdEvents.persistence.db.impl.TranscodeJobDaoImpl.class)
public interface TranscodeJobDao extends Dao<TranscodeJob, String>
{
	void transcodeReportMessage(EventEntity event);

	List<TranscodeJob> getTranscodeJobsInDateRange(DateTime start, DateTime end);
}
