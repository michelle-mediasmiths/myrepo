package com.mediasmiths.stdEvents.ui.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.ui.rest.EventUI;
import com.mediasmiths.stdEvents.ui.rest.EventUIImpl;
import com.mediasmiths.stdEvents.ui.rss.NotificationServiceAPI;
import com.mediasmiths.stdEvents.ui.rss.NotificationServiceImpl;
import com.mediasmiths.stdEvents.persistence.db.dao.AggregatedBMSDao;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import com.mediasmiths.stdEvents.persistence.db.dao.EventingDao;
import com.mediasmiths.stdEvents.persistence.db.impl.AggregatedBMSDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.EventEntityDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.EventingDaoImpl;
import com.mediasmiths.stdEvents.persistence.rest.impl.EventAPIImpl;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;

public class EventModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(EventUI.class).to(EventUIImpl.class);
		bind(QueryAPI.class).to(QueryAPIImpl.class);
		bind(NotificationServiceAPI.class).to(NotificationServiceImpl.class);
		bind(EventEntityDao.class).to(EventEntityDaoImpl.class);
		bind(EventAPI.class).to(EventAPIImpl.class);

		bind(EventingDao.class).to(EventingDaoImpl.class);
		bind(AggregatedBMSDao.class).to(AggregatedBMSDaoImpl.class);
				
		RestResourceRegistry.register(EventUI.class);
		RestResourceRegistry.register(EventAPI.class);

		bind(EventUIImpl.class).asEagerSingleton();
	}
}
