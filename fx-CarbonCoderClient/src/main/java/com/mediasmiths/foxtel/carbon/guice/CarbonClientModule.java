package com.mediasmiths.foxtel.carbon.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.foxtel.carbon.CarbonClient;
import com.mediasmiths.foxtel.carbon.CarbonClientImpl;

public class CarbonClientModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(CarbonClient.class).to(CarbonClientImpl.class);
	}

}
