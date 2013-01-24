package com.mediasmiths.foxtel.tc.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.carbonwfs.guice.WfsClientModule;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;
import org.apache.log4j.Logger;

import java.util.List;

public class TCAdapterSetup extends AbstractRESTGuiceSetup
{

	private final static Logger log = Logger.getLogger(TCAdapterSetup.class);


	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		log.info("Adding modules");

		modules.add(new TCAdapterModule());
		modules.add(new WfsClientModule());
		modules.add(new ThymeleafModule());

	}


	@Override
	public void injectorWasCreated(Injector arg0)
	{

	}

}
