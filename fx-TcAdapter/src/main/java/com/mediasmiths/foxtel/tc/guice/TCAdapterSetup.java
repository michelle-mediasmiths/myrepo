package com.mediasmiths.foxtel.tc.guice;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.carbon.guice.CarbonClientModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

public class TCAdapterSetup extends AbstractRESTGuiceSetup
{

	private final static Logger log = Logger.getLogger(TCAdapterSetup.class);

	@Override
	public void injectorCreated(Injector injector)
	{
	}

	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		log.info("Adding modules");
		modules.add(new TCAdapterModule());
		modules.add(new CarbonClientModule());

	}

}
