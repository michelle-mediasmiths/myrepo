package com.mediasmiths.foxtel.tc.reporting;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.foxtel.ip.common.events.TranscodeReportData;
import com.mediasmiths.foxtel.ip.event.EventService;
import org.apache.log4j.Logger;

public class PersistenceImpl implements Persistence
{
	private static final String TC_EVENT_NAMESPACE = "http://www.foxtel.com.au/ip/tc";

	private final static Logger log = Logger.getLogger(PersistenceImpl.class);

	@Inject
	EventService eventService;

	@Override
	public void saveTranscodeReportData(final TranscodeReportData trd)
	{
		log.debug("Saving transcode report data");
		eventService.saveEvent(TC_EVENT_NAMESPACE, EventNames.TRANSCODE_REPORT_DATA, trd);
	}
}
