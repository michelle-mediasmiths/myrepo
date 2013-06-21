package com.mediasmiths.stdEvents.ui.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

import java.util.List;

public class EventSetup extends AbstractRESTGuiceSetup
{

	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new EventModule());	
		modules.add(new ThymeleafModule());
		modules.add(new EventDatabaseModule());
	}

	@Override
	public void injectorWasCreated(Injector injector)
	{
	}
}
