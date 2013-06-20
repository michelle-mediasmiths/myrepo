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


	protected String contentTypeToHumanString(String contentType)
	{

		if ("PG".equals(contentType))
		{
			return "Programme";
		}
		else if ("TM".equals(contentType))
		{
			return "Unmatched";
		}
		else if ("CP".equals(contentType))
		{
			return "Publicity";
		}
		else if ("CU".equals(contentType))
		{
			return "Edit Clip";
		}
		else if ("PE".equals(contentType))
		{
			return "Associated";
		}
		else
		{
			logger.warn("Unknown asset type :" + contentType);
			return contentType;
		}
	}

	public Object unmarshallEvent(EventEntity event)
	{
		logger.debug(">>>unmarshallEvent");
		Object object = null;
		String payload = event.getPayload();
		logger.debug("Unmarshalling payload " + payload);
		
		try
		{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
			object = JAXB_SERIALISER.deserialise(payload);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		logger.debug("<<<unmarshallEvent");
		return object;
	}
	
	public Object unmarshallReport(final EventEntity event)
	{
		logger.info(">>>unmarshallReport");
		Object obj = null;
		String payload = event.getPayload();
		logger.info("Unmarshalling payload " + payload);

		try
		{
			JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.report.ObjectFactory.class);
			obj = JAXB_SERIALISER.deserialise(payload);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		logger.debug("<<<unmarshallReport");
		return obj;
	}
}
