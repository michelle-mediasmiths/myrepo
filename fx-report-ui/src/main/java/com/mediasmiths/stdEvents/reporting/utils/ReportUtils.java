package com.mediasmiths.stdEvents.reporting.utils;

import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.coreEntity.db.entity.Title;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public abstract class ReportUtils
{
	protected static final String dateAndTimeFormatString = "dd-MM-yyyy-HH:mm:ss";
	protected static final DateTimeFormatter dateAndTimeFormatter = DateTimeFormat.forPattern(dateAndTimeFormatString);
	protected static final DateFormat dateAndTimeFormat = new SimpleDateFormat(dateAndTimeFormatString);
	private final static transient Logger logger = Logger.getLogger(ReportUtils.class);

	protected static final String dateOnlyFormatString = "dd-MM-yyyy";
	protected static final DateTimeFormatter dateOnlyFormatter = DateTimeFormat.forPattern(dateOnlyFormatString);
	protected static final DateFormat dateOnlyFormat = new SimpleDateFormat(dateOnlyFormatString);

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

	protected void putTitleAndChannels(final String[] header,
	                                   final Map<String, Object> map,
	                                   final Title title,
	                                   int titleIndex,
	                                   int channelIndex)
	{
		if (title != null)
		{
			map.put(header[titleIndex], title.getTitle());
			putChannelListToCSVMap(header, channelIndex, map, title.getChannels());
		}
		else
		{
			map.put(header[titleIndex], null);
			map.put(header[channelIndex], null);
		}
	}

	protected void putChannelListToCSVMap(final String[] header, int index, final Map<String, Object> map, List<String> channels)
	{
		if (channels != null)
		{
			LinkedHashSet<String> channelS = new LinkedHashSet<String>(channels);
			map.put(header[index], StringUtils.join(channelS, ';'));
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
	                                        final Long dateInMillis,
	                                        DateFormat format){
		Date d = null;

		if(dateInMillis != null){
			d = new Date(dateInMillis);
		}

		putFormattedDateInCSVMap(header,index,map,d,format);
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


	protected void putFormattedTimeInCSVMap(final String[] header,
	                                        final int index,
	                                        final Map<String, Object> map,
	                                        final Long time)
	{

		if (time == null)
		{
			map.put(header[index], null);
		}
		else
		{
			map.put(header[index], getDDHHMMSSStringForMillis(time));
		}
	}


	protected String getDDHHMMSSStringForMillis(Long millis)
	{

		if (millis == null)
		{
			return "";
		}
		else
		{

			Period period = new Duration(millis.longValue()).toPeriod();
			return String.format("%02d:%02d:%02d:%02d",
			                     period.getDays(),
			                     period.getHours(),
			                     period.getMinutes(),
			                     period.getSeconds());
		}
	}

	public Object unmarshallEvent(EventEntity event)
	{
		logger.trace(">>>unmarshallEvent");
		Object object = null;
		String payload = event.getPayload();
		logger.debug("Unmarshalling payload " + payload);
		
		object = JAXB_SERIALISER.deserialise(payload);

		logger.trace("<<<unmarshallEvent");
		return object;
	}

	JAXBSerialiser JAXB_SERIALISER = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);
	
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


	protected String getDateRangeString(final DateTime start, final DateTime end)
	{
		final String startDate = start.toString(dateOnlyFormatter);
		final String endDate = end.toString(dateOnlyFormatter);
		return String.format("%s - %s", startDate, endDate);
	}

	protected String getHHMMSSmmm(Period period){
		return String.format("%02d:%02d:%02d:%03d", period.getHours(), period.getMinutes(), period.getSeconds(),period.getMillis());
	}


	// fills in some information not returned by the sql query, perhaps the query could be modified to include this information but we will see how this goes
	protected void fillTransientOrderStatusFields(List<com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus> orders,
	                                              DateTime start,
	                                              DateTime end)
	{
		Interval interval = new Interval(start, end);

		for (OrderStatus orderStatus : orders)
		{
			if (orderStatus.getRequiredBy() != null)
			{

				DateTime requiredByDate = new DateTime(orderStatus.getRequiredBy());

				if (orderStatus.getCompleted() != null)
				{
					// have required by and completed date
					DateTime completedDate = new DateTime(orderStatus.getCompleted());

					// determine if complete in date range
					if (interval.contains(completedDate))
					{
						orderStatus.setComplete(Boolean.TRUE);
					}
					else
					{
						orderStatus.setComplete(Boolean.FALSE);
					}

					// determine if overdue in date range
					if (completedDate.isAfter(requiredByDate))
					{
						orderStatus.setOverdue(Boolean.TRUE);
					}
					else
					{
						orderStatus.setOverdue(Boolean.FALSE);
					}

				}
				else
				{
					// no completed date
					orderStatus.setComplete(Boolean.FALSE);

					// determine if overdue in date range
					if (end.isAfter(requiredByDate))
					{
						orderStatus.setOverdue(Boolean.TRUE);
					}
					else
					{
						orderStatus.setOverdue(Boolean.FALSE);
					}
				}
			}
			else
			{
				// no required by date so cant be overdue
				orderStatus.setOverdue(Boolean.FALSE);

				if (orderStatus.getCompleted() != null)
				{
					// have completed date
					DateTime completedDate = new DateTime(orderStatus.getCompleted());

					// determine if complete in date range
					if (interval.contains(completedDate))
					{
						orderStatus.setComplete(Boolean.TRUE);
					}
					else
					{
						orderStatus.setComplete(Boolean.FALSE);
					}
				}
				else
				{
					// no completed date
					orderStatus.setComplete(Boolean.FALSE);
				}
			}
		}

	}
}
