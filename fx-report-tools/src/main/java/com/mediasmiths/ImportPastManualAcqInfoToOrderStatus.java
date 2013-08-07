package com.mediasmiths;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.ip.common.events.EventNames;
import com.mediasmiths.foxtel.ip.common.events.report.Acquisition;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import com.mediasmiths.stdEvents.coreEntity.db.entity.EventEntity;
import com.mediasmiths.stdEvents.coreEntity.db.entity.OrderStatus;
import com.mediasmiths.stdEvents.events.rest.api.QueryAPI;
import com.mediasmiths.stdEvents.persistence.db.dao.EventEntityDao;
import com.mediasmiths.stdEvents.persistence.db.dao.OrderDao;
import com.mediasmiths.stdEvents.persistence.guice.PersistenceDatabaseModule;
import com.mediasmiths.stdEvents.persistence.rest.impl.QueryAPIImpl;
import org.apache.log4j.Logger;

import java.util.List;

import static java.lang.System.exit;

public class ImportPastManualAcqInfoToOrderStatus
{

	@Inject
	private OrderDao orderStatusDao;

	@Inject
	private EventEntityDao events;

	private final static Logger logger = Logger.getLogger(ImportPastManualAcqInfoToOrderStatus.class);

	private final JAXBSerialiser serialiser = JAXBSerialiser.getInstance(com.mediasmiths.foxtel.ip.common.events.report.ObjectFactory.class);


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
		injector.getInstance(ImportPastManualAcqInfoToOrderStatus.class).process();
	}


	private void process()
	{
		int offset = 0;
		int limit = 1000;
		while (true)
		{
			List<EventEntity> programmeContentAvailable = events.getByNamePaged(EventNames.PROGRAMME_CONTENT_AVAILABLE,
			                                                                    offset,
			                                                                    limit);

//			programmeContentAvailable.addAll(events.getByNamePaged(EventNames.MARKETING_CONTENT_AVAILABLE, offset, limit));

			logger.info("IMPORT: Found " + programmeContentAvailable.size() + " events");

			if(programmeContentAvailable.size()==0){
				break;
			}

			for (EventEntity e : programmeContentAvailable)
			{
				try
				{
//					logger.info(new Date(e.getTime()).toGMTString() + " " + e.getPayload());

					Acquisition acq = (Acquisition) serialiser.deserialise(e.getPayload());


					final String materialID = acq.getMaterialID();
					final String format = acq.getFormat();
					final String filesize = acq.getFilesize();


					final OrderStatus orderStatus = orderStatusDao.getById(materialID);
					if (orderStatus != null)
					{

						boolean updated = false;

						if (orderStatus.getFileSize() == null && filesize != null)
						{
							try
							{
								Long lfileSize = Long.valueOf(filesize);
								logger.info("IMPORT: Setting filesize "+lfileSize + " for "+materialID);
								orderStatus.setFileSize(lfileSize);
								updated = true;
							}
							catch (NumberFormatException nfe)
							{
								logger.error("IMPORT: cannot use file size " + acq.getFilesize());
							}
						}

						if (orderStatus.getFormat() == null && format != null)
						{
							logger.info("IMPORT: Setting format "+format + " for "+materialID);
							orderStatus.setFormat(format);
							updated = true;
						}

						if (updated)
						{
							logger.info("IMPORT: updating orderstatus for material "+materialID);
							orderStatusDao.update(orderStatus);
							Thread.currentThread().sleep(250l);
						}
					}
					else
					{
						logger.info("IMPORT: no order status for material id : " + materialID);
					}
				}
				catch (Exception ex)
				{
					logger.error("IMPORT: error processing event " + e.getId(), ex);
				}
			}
			offset += limit;
		}
	}
}