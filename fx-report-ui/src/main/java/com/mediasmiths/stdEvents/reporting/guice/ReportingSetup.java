package com.mediasmiths.stdEvents.reporting.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.channels.config.ChannelConfigModule;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

import java.util.List;

public class ReportingSetup extends AbstractRESTGuiceSetup
{

	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new ReportingModule());
		modules.add(new ThymeleafModule());
		modules.add(new EventsAndAccessDatabaseModule());
		modules.add(new MayamClientModule(false));
		modules.add(new ChannelConfigModule());
	}

	@Override
	public void injectorWasCreated(Injector injector)
	{
		// TODO Auto-generated method stub
		
	}
	
}
