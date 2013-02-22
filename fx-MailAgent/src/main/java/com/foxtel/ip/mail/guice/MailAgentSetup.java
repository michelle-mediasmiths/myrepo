package com.foxtel.ip.mail.guice;

import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.std.guice.web.rest.setup.AbstractRESTGuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

public class MailAgentSetup extends AbstractRESTGuiceSetup
{
	@Override
	public void addModules(List<Module> modules, PropertyFile config)
	{
		modules.add(new MailAgentModule());
		modules.add(new MailAgentDatabaseModule());

	}

	@Override
	public void injectorWasCreated(Injector injector)
	{
		// TODO Auto-generated method stub
	}

}