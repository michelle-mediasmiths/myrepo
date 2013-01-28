package com.mediasmiths.stdEvents.reporting.guice;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;
import com.mediasmiths.stdEvents.coreEntity.db.marshaller.EventMarshaller;
import com.mediasmiths.stdEvents.events.rest.api.EventAPI;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import com.mediasmiths.stdEvents.persistence.db.dao.EventingDao;
import com.mediasmiths.stdEvents.persistence.db.impl.ContentPickupDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.DeliveryDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.EventEntityDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.EventingDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.PlaceholderMessageDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.QCDaoImpl;
import com.mediasmiths.stdEvents.persistence.db.impl.TranscodeDaoImpl;
import com.mediasmiths.stdEvents.persistence.rest.impl.EventAPIImpl;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import com.mediasmiths.stdEvents.report.jasper.JasperAPI;
import com.mediasmiths.stdEvents.report.jasper.JasperImpl;
import com.mediasmiths.stdEvents.reporting.csv.CsvAPI;
import com.mediasmiths.stdEvents.reporting.csv.CsvImpl;
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
		bind(JasperAPI.class).to(JasperImpl.class);
		bind(CsvAPI.class).to(CsvImpl.class);
		
		Multibinder<EventMarshaller> marshallerBinding = Multibinder.newSetBinder(binder(), EventMarshaller.class);
		marshallerBinding.addBinding().to(PlaceholderMessageDaoImpl.class);
		marshallerBinding.addBinding().to(ContentPickupDaoImpl.class);
		marshallerBinding.addBinding().to(QCDaoImpl.class);
		marshallerBinding.addBinding().to(TranscodeDaoImpl.class);
		marshallerBinding.addBinding().to(DeliveryDaoImpl.class);
		
		RestResourceRegistry.register(ReportUI.class);
		
//		Properties props = new Properties();
//		try {
//			props.load(new FileReader("/Users/alisonboal/Documents/forge/PersistenceParent/fx-report-ui/service.properties"));
//			Names.bindProperties(binder(), props);
//		}
//		catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
	}
}
