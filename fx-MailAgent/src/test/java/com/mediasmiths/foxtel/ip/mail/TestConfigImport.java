package com.mediasmiths.foxtel.ip.mail;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcNotification;
import com.mediasmiths.foxtel.ip.mail.process.EventMailConfiguration;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;
import org.junit.Ignore;

import java.io.File;

@Ignore //surefire will try and run this file as a unit test as it has "Test" in the name
public class TestConfigImport
{

	static EventMailConfiguration conf;
	static JAXBSerialiser jaxb;

	public static void main(String[] args)
	{
		String configFilePath = "/tmp/config.xml";

		jaxb = JAXBSerialiser.getInstance("com.mediasmiths.foxtel.ip.common.email");

		try
		{
			conf = new EventMailConfiguration(new File(configFilePath),jaxb);

			lookUp("http://www.foxtel.com.au/ip/tc", "PublicityFailure");

			//System.out.println(jaxb.serialise(conf));
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	public static void lookUp(String ns, String e)
	{
		EmailTemplateGenerator t = conf.getTemplate(e, ns);

		TcNotification tc= new TcNotification();
		tc.setPackageID("77777");


			//System.out.println(jaxb.serialise(etg));
			System.out.println(t.getClass().getName());

			MailTemplate temp = t.customiseTemplate(tc, "Hello cruel world");

			System.out.println(temp.getEmailaddresses());
			System.out.println(temp.getBody());

			System.out.println(temp.getSubject());

		}

}
