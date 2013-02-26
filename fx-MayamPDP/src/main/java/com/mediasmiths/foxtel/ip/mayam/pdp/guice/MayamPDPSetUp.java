package com.mediasmiths.foxtel.ip.mayam.pdp.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.ip.mayam.pdp.MayamPDPImpl;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class MayamPDPSetUp extends AbstractModule
{

	@Override
	protected void configure()
	{

		RestResourceRegistry.register(MayamPDPImpl.class);
	}


}
