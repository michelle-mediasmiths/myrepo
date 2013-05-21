package com.mediasmiths.stdEvents.reporting.utils;

import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;

public abstract class ReportUtils
{
	private final static transient Logger logger = Logger.getLogger(ReportUtils.class);
	
	public static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd-MM-yyyy");

	public Object unmarshall(EventEntity event)
	{
		Object object = null;
		String payload = event.getPayload();
		logger.debug("Unmarshalling payload " + payload);
		
		try
		{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
			logger.debug("Deserialising payload");
			object = JAXB_SERIALISER.deserialise(payload);
			logger.debug("Object created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return object;
	}
}
