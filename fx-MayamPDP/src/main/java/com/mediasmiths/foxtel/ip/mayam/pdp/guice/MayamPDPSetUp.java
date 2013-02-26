package com.mediasmiths.foxtel.ip.mayam.pdp.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.ip.mayam.pdp.MayamPDP;
import com.mediasmiths.foxtel.ip.mayam.pdp.Stub;
import com.mediasmiths.std.guice.serviceregistry.rest.RestResourceRegistry;

public class MayamPDPSetUp extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(MayamPDP.class).to(Stub.class);
		RestResourceRegistry.register(MayamPDP.class);
	}


}
