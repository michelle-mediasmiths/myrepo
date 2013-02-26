package com.mediasmiths.foxtel.ip.mayam.pdp.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.channels.config.ChannelConfigModule;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

import java.util.List;

public class PDPSetUp extends AbstractRESTGuiceSetup
{

	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new MayamPDPSetUp());
		modules.add(new ChannelConfigModule());
		modules.add(new MayamClientModule());		
	}

	@Override
	public void injectorWasCreated(Injector injector)
	{
	}

}
