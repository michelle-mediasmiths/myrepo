package com.mediasmiths.foxtel.ibmshelper.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

import java.util.List;

public class IbmsGuiceSetup extends AbstractRESTGuiceSetup
{

	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new IbmsGuiceModule());
		modules.add(new IbmsGuiceDatabaseModule());

	}

	@Override
	public void injectorWasCreated(Injector injector)
	{
		// TODO Auto-generated method stub
	}

}
