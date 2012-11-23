package com.mediasmiths.mq.guice;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

public class MqListenersSetup implements GuiceSetup {

	@Override
	public void registerModules(List<Module> modules, PropertyFile config) {
		modules.add(new MqListenersModule());	
		modules.add(new MayamClientModule());
	}

	@Override
	public void injectorCreated(Injector injector) {
	}

}
