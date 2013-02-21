package com.mediasmiths.foxtel.mpa.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.agent.WatchedFilesConfigModule;
import com.mediasmiths.foxtel.agent.guice.WatchFolderLocationsModule;
import com.mediasmiths.foxtel.channels.config.ChannelConfigModule;
import com.mediasmiths.foxtel.ip.event.guice.EventServiceModule;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.guice.MayamClientStubModule;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

import java.util.List;

public class MediaPickupSetup implements GuiceSetup
{

	@Override
	public void registerModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new WatchFolderLocationsModule());
		modules.add(new WatchedFilesConfigModule());
		modules.add(new ChannelConfigModule());
		
		if (config.getBoolean("stub.out.mayam", false))
		{
			modules.add(new MayamClientStubModule());
		}
		else
		{
			modules.add(new MayamClientModule());
		}
		
		modules.add(new MediaPickupModule());
		modules.add(new EventServiceModule());
	}

	@Override
	public void injectorCreated(Injector injector)
	{
	}

}
