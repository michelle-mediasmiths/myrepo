package com.mediasmiths.foxtel.fs.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;
import org.apache.log4j.Logger;

import java.util.List;


public class FSAdapterSetup  extends AbstractRESTGuiceSetup {

	private final static Logger log = Logger.getLogger(FSAdapterSetup.class);
	
	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		log.info("Adding modules");
		modules.add(new FSAdapterModule());
	}

	@Override
	public void injectorWasCreated(Injector arg0)
	{
		
	}

}
