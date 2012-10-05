package com.mediasmiths.foxtel.qc.guice;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.cerify.CerifyModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;


public class QCAdapterSetup  extends AbstractRESTGuiceSetup {

	private final static Logger log = Logger.getLogger(QCAdapterSetup.class);
	
	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		log.info("Adding modules");
		modules.add(new QCAdapterModule());
		modules.add(new CerifyModule());
	}

	@Override
	public void injectorWasCreated(Injector arg0)
	{
		
	}

}
