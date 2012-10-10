package com.mediasmiths.foxtel.mayam.adapter.guice;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

public class MayamAdapterSetup extends AbstractRESTGuiceSetup
{

	@Override
	public void addModules(List<Module> modules, PropertyFile props)
	{
		modules.add(new MayamAdapterModule());
		modules.add(new MayamClientModule());

	}

	@Override
	public void injectorWasCreated(Injector arg0)
	{
		// TODO Auto-generated method stub

	}

}
