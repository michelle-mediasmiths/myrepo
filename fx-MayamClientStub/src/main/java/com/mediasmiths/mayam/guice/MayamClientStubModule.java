package com.mediasmiths.mayam.guice;

import com.google.inject.AbstractModule;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientStub;
import com.mediasmiths.mayam.MayamValidatorStub;
import com.mediasmiths.mayam.validation.MayamValidator;

public class MayamClientStubModule extends AbstractModule 
{

	@Override
	protected void configure()
	{
		bind(MayamClient.class).to(MayamClientStub.class);
		bind(MayamValidator.class).to(MayamValidatorStub.class);
	}

}
