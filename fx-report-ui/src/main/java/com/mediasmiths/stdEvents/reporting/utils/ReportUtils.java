package com.mediasmiths.stdEvents.reporting.utils;

import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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


	protected void putChannelListToCSVMap(final String[] header, int index, final Map<String, Object> map, List<String> channels)
	{
		if (channels != null)
		{
			map.put(header[index], StringUtils.join(channels, ';'));
		}
		else
		{
			map.put(header[index], null);
		}
	}


	protected void putPossibleNullBooleanInCSVMap(final String[] header,
	                                              int index,
	                                              final Map<String, Object> map,
	                                              final Boolean value)
	{
		if (value != null && value)
		{
			map.put(header[index], "1");
		}
		else
		{
			map.put(header[index], "0");
		}
	}


	protected void putFormattedDateInCSVMap(final String[] header,
	                                        int index,
	                                        final Map<String, Object> map,
	                                        final Date date,
	                                        DateFormat format)
	{

		if (date == null)
		{
			map.put(header[index], null);
		}
		else
		{
			map.put(header[index], format.format(date));
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
