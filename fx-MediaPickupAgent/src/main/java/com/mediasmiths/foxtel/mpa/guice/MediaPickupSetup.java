package com.mediasmiths.foxtel.mpa.guice;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.guice.MayamClientStubModule;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

public class MediaPickupSetup implements GuiceSetup
{

	@Override
	public void registerModules(List<Module> modules, PropertyFile config)
	{

		if (config.getBoolean("stub.out.mayam", false))
		{
			modules.add(new MayamClientStubModule());
		}
		else
		{
			modules.add(new MayamClientModule());
		}
		
		modules.add(new MediaPickupModule());
	}

	@Override
	public void injectorCreated(Injector injector)
	{
	}

}
