package com.mediasmiths;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import com.mediasmiths.stdEvents.persistence.db.dao.ManualQAEntityDAO;
import com.mediasmiths.stdEvents.persistence.guice.PersistenceDatabaseModule;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Date;
import java.util.List;

import static java.lang.System.exit;

public class DumpAllEventsByEventName
{

	@Inject
	private ManualQAEntityDAO manualQADao;

	@Inject
	private QueryAPI queryAPI;

	@Inject
	private EventEntityDao events;

	private final static Logger logger = Logger.getLogger(DumpAllEventsByEventName.class);

	protected JAXBSerialiser serialiser = JAXBSerialiser.getInstance(EventEntity.class);


	public static void main(String[] args)
	{

		String jdbc = "jdbc:oracle:thin:oracle/oracle@192.168.2.62:1521/orcl";
		String user = "oracle";
		String password = "oracle";
		if (args == null || args.length < 3)
		{
			logger.error("arg0 missing, falling back to " + jdbc + " with user " + user + " and password " + password);
		}
		else
		{
			jdbc = args[0];
			user = args[1];
			password = args[2];
		}

		if (args.length < 4)
		{
			logger.error("event name missing");
			exit(1);
		}

		String eventName=args[3];

		try
		{
			SetupJNDI.setUpJNDI(jdbc, user, password);
		}
		catch (Exception e)
		{
			logger.error(e); // Autogenerated
			exit(1);
		}

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
		injector.getInstance(DumpAllEventsByEventName.class).process(eventName);
	}


	private void process(String eventName)
	{
		int offset = 0;
		int limit = 1000;
		while (true)
		{
			List<EventEntity> preview = events.getByNamePaged(eventName,offset,limit);

			logger.info("Found " + preview.size() + " events");

			if (preview.size() == 0)
			{
				return;
			}

			for (EventEntity e : preview)
			{
				try
				{
					logger.info(new Date(e.getTime()).toGMTString() + " " + e.getId());
					serialiser.serialise(e, new File(e.getTime() + ".xml"));
				}
				catch (Exception ex)
				{
					logger.error("error processing event " + e.getId(), ex);
				}
			}
			offset += limit;
		}
	}
}