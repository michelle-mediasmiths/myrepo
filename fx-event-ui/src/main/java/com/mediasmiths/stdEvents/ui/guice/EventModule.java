package com.mediasmiths.stdEvents.ui.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.ui.rest.EventUI;
import com.mediasmiths.stdEvents.ui.rest.EventUIImpl;
import com.mediasmiths.stdEvents.ui.rss.NotificationServiceAPI;
import com.mediasmiths.stdEvents.ui.rss.NotificationServiceImpl;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import com.mediasmiths.stdEvents.persistence.db.dao.EventingDao;
import com.mediasmiths.stdEvents.persistence.db.impl.ContentPickupDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.DeliveryDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.EventEntityDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.EventingDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.IPEventDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.InfrastructureDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.ManualPurgeDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.PlaceholderMessageDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.PreviewEventDetailDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.QCDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.TranscodeDaoImpl;
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

		Multibinder<EventMarshaller> marshallerBinding = Multibinder.newSetBinder(binder(), EventMarshaller.class);
		marshallerBinding.addBinding().to(PlaceholderMessageDaoImpl.class);
		marshallerBinding.addBinding().to(ContentPickupDaoImpl.class);
		marshallerBinding.addBinding().to(QCDaoImpl.class);
		marshallerBinding.addBinding().to(TranscodeDaoImpl.class);
		marshallerBinding.addBinding().to(DeliveryDaoImpl.class);
		marshallerBinding.addBinding().to(IPEventDaoImpl.class);	
		marshallerBinding.addBinding().to(InfrastructureDaoImpl.class);
		marshallerBinding.addBinding().to(PreviewEventDetailDaoImpl.class);

		RestResourceRegistry.register(EventUI.class);
		RestResourceRegistry.register(EventAPI.class);
	}
}
