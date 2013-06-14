package com.mediasmiths;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.persistence.db.dao.ManualQAEntityDAO;
import com.mediasmiths.stdEvents.persistence.guice.PersistenceDatabaseModule;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;

import java.util.Date;
import java.util.List;
public class ImportPastManualQAMessages
{

	@Inject
	private ManualQAEntityDAO manualQADao;

	@Inject
	private QueryAPI queryAPI;


	public static void main(String[] args)
	{

		final Injector injector = GuiceInjectorBootstrap.createInjector(new GuiceSetup()
		{
			@Override
			public void registerModules(final List<Module> modules, final PropertyFile config)
			{
				modules.add(new PersistenceDatabaseModule());
				modules.add(new AbstractModule()
				{

					@Override
					protected void configure()
					{
						bind(QueryAPI.class).to(QueryAPIImpl.class);
					}
				});

			}


			@Override
			public void injectorCreated(final Injector injector)
			{
			}
		});
		injector.getInstance(ImportPastManualQAMessages.class).process();
	}

	private void process()
	{
		List<EventEntity> preview = queryAPI.getEvents("http://www.foxtel.com.au/ip/preview", EventNames.MANUAL_QA);

		System.out.println("Found " + preview.size() + " events");

		for (EventEntity e : preview)
		{
			System.out.println(new Date(e.getTime()).toGMTString() + " " + e.getPayload());
			manualQADao.manualQCMessage(e);
		}
	}
}
