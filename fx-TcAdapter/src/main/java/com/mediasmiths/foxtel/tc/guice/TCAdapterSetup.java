package com.mediasmiths.foxtel.tc.guice;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.carbonwfs.guice.WfsClientModule;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.guice.MayamClientStubModule;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

public class TCAdapterSetup extends AbstractRESTGuiceSetup
{

	private final static Logger log = Logger.getLogger(TCAdapterSetup.class);


	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		log.info("Adding modules");
		
		if (config.getBoolean("stub.out.mayam", false))
		{
			modules.add(new MayamClientStubModule());
		}
		else
		{
			modules.add(new MayamClientModule());
		}
		
		modules.add(new TCAdapterModule());
		modules.add(new WfsClientModule());
		modules.add(new ThymeleafModule());
		
	}


	@Override
	public void injectorWasCreated(Injector arg0)
	{
		
	}

}
