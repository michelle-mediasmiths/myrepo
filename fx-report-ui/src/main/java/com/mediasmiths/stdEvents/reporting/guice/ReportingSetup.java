package com.mediasmiths.stdEvents.reporting.guice;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.stdEvents.persistence.guice.PersistenceDatabaseModule;

public class ReportingSetup extends AbstractRESTGuiceSetup
{

	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new ReportingModule());
		modules.add(new ThymeleafModule());
		modules.add(new PersistenceDatabaseModule());
	}

	@Override
	public void injectorWasCreated(Injector injector)
	{
		// TODO Auto-generated method stub
		
	}
	
}
