package com.mediasmiths.foxtel.wf.adapter.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.channels.config.ChannelConfigModule;
import com.mediasmiths.foxtel.ip.event.guice.EventServiceModule;
import com.mediasmiths.foxtel.tc.priorities.guice.TranscodePrioritiesModule;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.guice.MayamClientStubModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

import java.util.List;

public class WFAdapterSetup extends AbstractRESTGuiceSetup
{

	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new WFAdapterModule());
		modules.add(new EventServiceModule());
		modules.add(new ChannelConfigModule());
		
		if (config.getBoolean("stub.out.mayam", false))
		{
			modules.add(new MayamClientStubModule());
		}
		else
		{
			modules.add(new MayamClientModule(true));
		}
		
		modules.add(new TranscodePrioritiesModule());
	}

	@Override
	public void injectorWasCreated(Injector arg0)
	{
		// TODO Auto-generated method stub

	}

}
