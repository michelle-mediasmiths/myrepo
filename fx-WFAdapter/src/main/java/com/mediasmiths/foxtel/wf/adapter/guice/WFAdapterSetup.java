package com.mediasmiths.foxtel.wf.adapter.guice;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.guice.MayamClientStubModule;
import com.mediasmiths.mayam.guice.SecurityModule;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

public class WFAdapterSetup extends AbstractRESTGuiceSetup
{

	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new WFAdapterModule());
		
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
	public void injectorWasCreated(Injector arg0)
	{
		// TODO Auto-generated method stub

	}

}
