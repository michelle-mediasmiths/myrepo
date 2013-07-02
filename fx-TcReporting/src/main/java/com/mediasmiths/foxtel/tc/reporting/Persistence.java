package com.mediasmiths.foxtel.tc.reporting;

import com.google.inject.ImplementedBy;
import com.mediasmiths.foxtel.ip.common.events.TranscodeReportData;

/**
 * Used saves tc report information
 *
 */
@ImplementedBy(PersistenceImpl.class)
public interface Persistence
{
	void saveTranscodeReportData(TranscodeReportData trd);
}
