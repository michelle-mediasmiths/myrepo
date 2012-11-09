package com.mediasmiths.foxtel.placeholder.guice;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.placeholder.validation.channels.ChannelValidatorModule;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.guice.MayamClientStubModule;
import com.mediasmiths.mayam.guice.SecurityModule;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

public class PlaceholderAgentSetup implements GuiceSetup {

	@Override
	public void registerModules(List<Module> modules, PropertyFile config) {
		modules.add(new PlaceholderAgentModule());
		modules.add(new ChannelValidatorModule());
		
		
		if (config.getBoolean("stub.out.mayam", false))
		{
			modules.add(new MayamClientStubModule());
		}
		else
		{
			modules.add(new MayamClientModule());
		}
	}

	@Override
	public void injectorCreated(Injector injector) {
	}

}
