package com.mediasmiths.mq.guice;

import javax.xml.bind.Marshaller;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

public class MarshallerProvider implements Provider<JAXBSerialiser>
{

	@Inject
	@Named("wfe.serialiser")
	private JAXBSerialiser serialiser;

	@Override
	public JAXBSerialiser get()
	{
		return serialiser;
	}

}

