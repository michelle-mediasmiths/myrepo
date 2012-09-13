package com.medismiths.foxtel.mpa;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

public class MediaPickupSetup implements GuiceSetup {

	@Override
	public void registerModules(List<Module> modules, PropertyFile config) {
		modules.add(new MediaPickupModule());

	}

	@Override
	public void injectorCreated(Injector injector) {
		// TODO Auto-generated method stub

	}

}
