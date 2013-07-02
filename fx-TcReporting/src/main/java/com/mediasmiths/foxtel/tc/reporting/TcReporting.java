package com.mediasmiths.foxtel.tc.reporting;

import com.google.inject.ImplementedBy;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import org.datacontract.schemas._2004._07.rhozet.Job;

import java.util.UUID;

/**
 * Interface for the recording of transcode status information for use when generating transcode load reports
 */
@ImplementedBy(TcReportingImpl.class)
public interface TcReporting
{
	/**
	 * Records the creation + start of a transcode job
	 * @param id    - the id of the job
	 * @param parameters - the parameters from which the job was created
	 * @param job
	 */
	public void recordStart(final UUID id, final TCJobParameters parameters, final Job job);

	public void recordStatus(final UUID id, final Job job);
}
