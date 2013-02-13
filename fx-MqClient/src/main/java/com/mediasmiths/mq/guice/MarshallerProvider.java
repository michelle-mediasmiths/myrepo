package com.mediasmiths.mq.guice;

import javax.xml.bind.Marshaller;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class MarshallerProvider implements Provider<Marshaller>
{

	@Inject
	@Named("wfe.marshaller")
	private Marshaller wfeMarshaller;

	@Override
	public Marshaller get()
	{
		return wfeMarshaller;
	}

}
