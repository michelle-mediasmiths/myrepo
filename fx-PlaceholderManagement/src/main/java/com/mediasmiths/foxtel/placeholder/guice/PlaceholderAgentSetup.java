package com.mediasmiths.foxtel.placeholder.guice;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

public class PlaceholderAgentSetup implements GuiceSetup {

	@Override
	public void registerModules(List<Module> modules, PropertyFile config) {
		modules.add(new PlaceholderAgentModule());
	}

	@Override
	public void injectorCreated(Injector injector) {
	}

}
