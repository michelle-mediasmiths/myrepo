package com.mediasmiths.foxtel.ip.mail;

import com.mediasmiths.foxtel.ip.common.email.MailTemplate;
import com.mediasmiths.foxtel.ip.common.events.TcFailureNotification;
import com.mediasmiths.foxtel.ip.mail.process.EventMailConfiguration;
import com.mediasmiths.foxtel.ip.mail.templater.EmailTemplateGenerator;
import com.mediasmiths.std.util.jaxb.JAXBSerialiser;

import java.io.File;
import java.util.List;

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
		List<EmailTemplateGenerator> t = conf.getTemplate(e, ns);

		System.out.println("Templates = " + t.size());

		TcFailureNotification tc= new TcFailureNotification();
		tc.setPackageID("77777");

		for (EmailTemplateGenerator etg : t)
		{
			//System.out.println(jaxb.serialise(etg));
			System.out.println(etg.getClass().getName());

			MailTemplate temp = etg.customiseTemplate(tc, "Hello cruel world");

			System.out.println(temp.getEmailaddresses());
			System.out.println(temp.getBody());

			System.out.println(temp.getSubject());

		}
	}
}
