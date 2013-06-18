package com.mediasmiths.stdEvents.reporting.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import com.mediasmiths.stdEvents.persistence.db.dao.EventingDao;
import com.mediasmiths.stdEvents.persistence.db.impl.EventEntityDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.EventingDaoImpl;
import com.mediasmiths.stdEvents.persistence.rest.impl.EventAPIImpl;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.reporting.rest.ReportUI;
import com.mediasmiths.stdEvents.reporting.rest.ReportUIImpl;

public class ReportingModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(EventEntityDao.class).to(EventEntityDaoImpl.class);
		bind(EventingDao.class).to(EventingDaoImpl.class);
		bind(EventAPI.class).to(EventAPIImpl.class);
		bind(ReportUI.class).to(ReportUIImpl.class);
		bind(QueryAPI.class).to(QueryAPIImpl.class);

		RestResourceRegistry.register(ReportUI.class);
		bind(ReportUIImpl.class).asEagerSingleton();
	}

	private JAXBSerialiser jaxbSerialiser = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.ObjectFactory.class);

	@Provides
	public JAXBSerialiser getJaxbSerialiser()
	{
		return  jaxbSerialiser;
	}
}
