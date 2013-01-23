package com.mediasmiths.foxtel.tc.service;

import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

public class CarbonProjectBuilder
{
	public static void main(String[] args) throws Exception
	{
		JAXBSerialiser ser = JAXBSerialiser.getInstance(CarbonJobParameters.class);
		ser.setPrettyOutput(true);

		System.out.println(ser.serialise(new CarbonJobParameters()));
	}
}
