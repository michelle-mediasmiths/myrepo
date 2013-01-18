package com.mediasmiths.stdEvents.persistence.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.persistence.db.dao.QueryDao;
import com.mediasmiths.stdEvents.persistence.db.impl.ContentPickupDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.DeliveryDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.IPEventDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.PlaceholderMessageDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.QCDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.TranscodeDaoImpl;

import java.util.HashMap;
import java.util.Map;

/**
 *  Event Report settings
 */
public class EventReportConfig extends AbstractModule
{
	@Override
	protected void configure()
	{
	}

	@Provides
	@Named("event.reporter.eventtypemap")
	/**
	 * @return a Map that defines a type for each handled namespace
	 */
	Map<String, Class<? extends EventMarshaller>> providesEventTypeMap ()
	{
		Map<String, Class<? extends EventMarshaller>> storeFormat = new HashMap<String, Class<? extends EventMarshaller>>();
		storeFormat.put("http://www.foxtel.com.au/ip/bms", PlaceholderMessageDaoImpl.class);
		storeFormat.put("http://www.foxtel.com.au/ip/content", ContentPickupDaoImpl.class);
		storeFormat.put("http://www.foxtel.com.au/ip/tc", TranscodeDaoImpl.class);
		storeFormat.put("http://www.foxtel.com.au/ip/qc", QCDaoImpl.class);
		storeFormat.put("http://www.foxtel.com.au/ip/delivery", DeliveryDaoImpl.class);
		storeFormat.put("http://www.foxtel.com.au/ip/system", IPEventDaoImpl.class);

		return storeFormat;
	}

	@Provides
	@Named("event.reporter.eventnamemap")
	Map<String, Class<? extends QueryDao<?>>> providesNameTypeMapping()
	{
		Map<String, Class<? extends QueryDao<?>>> getDao = new HashMap<String, Class<? extends QueryDao<?>>>();
		getDao.put("placeholderMessage", PlaceholderMessageDaoImpl.class);
		getDao.put("contentPickup", ContentPickupDaoImpl.class);
		getDao.put("transcode", TranscodeDaoImpl.class);
		getDao.put("qc", QCDaoImpl.class);
		getDao.put("delivery", DeliveryDaoImpl.class);
		getDao.put("system", IPEventDaoImpl.class);
		return getDao;
	}
}
