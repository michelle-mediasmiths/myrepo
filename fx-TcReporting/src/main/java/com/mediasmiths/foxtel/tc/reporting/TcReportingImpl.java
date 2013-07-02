package com.mediasmiths.foxtel.tc.reporting;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.TranscodeReportData;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.rhozet.Job;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class TcReportingImpl implements TcReporting
{
	private final static Logger log = Logger.getLogger(TcReportingImpl.class);

	private static final long TICKS_PER_MILLISECOND = 10000l;
	private static final long JAN_1ST_1970 = 621355968000000000l;

	final DatatypeFactory dataTypeFactory;

	@Inject
	Persistence persistence;


	public TcReportingImpl() throws DatatypeConfigurationException
	{
		dataTypeFactory = DatatypeFactory.newInstance();
	}


	@Override
	public void recordStart(final UUID uuid, final TCJobParameters parameters, final Job job)
	{
		try
		{

			final String jobID = uuid.toString();
			final Long createdTimeticks = job.getCreated();
			final Date createdTime = fromDotNetTicks(createdTimeticks);
			final String sourcefmt = parameters.resolution.name();
			final String destFMT = parameters.purpose.name();
			final Integer priority = parameters.priority;
			final String status = job.getStatus().value();

			log.debug(String.format("Recording transcode job creation for reporting. Job ID %s created time %s source format %s destination format %s priority %s status %s",
			                        jobID,
			                        createdTime,
			                        sourcefmt,
			                        destFMT,
			                        priority,
			                        status));

			TranscodeReportData trd = new TranscodeReportData();

			trd.setJobID(jobID);
			trd.setCreated(fromDate(createdTime));
			trd.setSourceFormat(sourcefmt);
			trd.setDestinationFormat(destFMT);
			trd.setPriority(priority);
			trd.setStatus(status);

			persistence.saveTranscodeReportData(trd);
		}
		catch (Exception e)
		{
			log.error("Error sending transcode report data", e);
		}
	}


	@Override
	public void recordStatus(final UUID uuid, final Job job)
	{
		try
		{
			final String jobID = uuid.toString();
			final Date createdTime = fromDotNetTicks(job.getCreated());
			final Date updateTime = fromDotNetTicks(job.getLastUpdate());
			final Date startTime = fromDotNetTicks(job.getStarted());
			final String status = job.getStatus().value();
			final Integer priority = job.getPriority();


			log.debug(String.format("Recording transcode job status for reporting. Job ID %s, created time %s, updatedTime %s, startTime %s, status %s, priority %s",
			                        jobID,
			                        createdTime,
			                        updateTime,
			                        startTime,
			                        status,
			                        priority));

			TranscodeReportData trd = new TranscodeReportData();

			trd.setJobID(jobID);
			trd.setCreated(fromDate(createdTime));
			trd.setUpdated(fromDate(updateTime));
			trd.setStarted(fromDate(startTime));
			trd.setPriority(priority);
			trd.setStatus(status);

			persistence.saveTranscodeReportData(trd);
		}
		catch (Exception e)
		{
			log.error("Error sending transcode report data", e);
		}
	}


	/**
	 * Converts .net 'ticks' (nanoseconds since Jan 1 0001) to java.util.Date
	 *
	 * @param ticks
	 *
	 * @return null if ticks is null
	 */
	private Date fromDotNetTicks(Long ticks)
	{
		if (log.isTraceEnabled())
			log.trace(String.format("Converting %s ticks to java Date", ticks));

		Date ret;


		if (ticks != null && ticks != 0l)
		{
			Long millisSinceEpoch = (ticks - JAN_1ST_1970) / TICKS_PER_MILLISECOND;
			ret = new Date(millisSinceEpoch);
		}
		else
		{
			ret = null;
		}

		if (log.isDebugEnabled())
			log.debug(String.format("Converted %s ticks to date %s", ticks, ret));

		return ret;
	}


	public XMLGregorianCalendar fromDate(Date from)
	{
		if (from == null)
		{
			return null;
		}

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(from);
		return dataTypeFactory.newXMLGregorianCalendar(c);
	}
}
