package com.mediasmiths.examples;

import com.mediasmiths.foxtel.ip.common.events.TcFailureNotification;
import com.mediasmiths.foxtel.ip.common.events.TcPassedNotification;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

public class examples
{

	public static void main(String args[])
	{

		JAXBSerialiser jax_b = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.events");

		TcPassedNotification tc1 = new TcPassedNotification();
		tc1.setTaskID("444");
		tc1.setPackageID("Example444");
		tc1.setAssetID("asset 444");
		tc1.setTitle("Title 444");

		TcFailureNotification tc2 = new TcFailureNotification();
		tc2.setTaskID("444");
		tc2.setPackageID("Example444");
		tc2.setAssetID("asset 444");
		tc2.setTitle("Title 444");

		TcFailureNotification tc3 = new TcFailureNotification();
		tc3.setTaskID("444");
		tc3.setPackageID("Example444");
		tc3.setAssetID("asset 444");
		tc3.setTitle("Title 444");

		TcFailureNotification tc4 = new TcFailureNotification();
		tc4.setTaskID("444");
		tc4.setPackageID("Example444");
		tc4.setAssetID("asset 444");
		tc4.setTitle("Title 444");


		try
		{
			System.out.println(jax_b.serialise(tc1));
			System.out.println(jax_b.serialise(tc2));

			System.out.println(jax_b.serialise(tc3));

			System.out.println(jax_b.serialise(tc4));

		}
		catch (Throwable e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}


}
