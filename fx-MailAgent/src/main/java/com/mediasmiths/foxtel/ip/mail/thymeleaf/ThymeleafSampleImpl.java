package com.mediasmiths.foxtel.ip.mail.thymeleaf;

import com.google.inject.Inject;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;

public class ThymeleafSampleImpl implements ThymeleafSample
{
	@Inject
	private ThymeleafTemplater templater;
	
	@Override
	public String createPopup()
	{
		TemplateCall call = templater.template("ThymeleafSampleMail");
		return call.process();
	}

	@Override
	public String sendMail()
	{

		ThymeleafEmailSender testEmailWorker= new ThymeleafEmailSender(templater);
		testEmailWorker.run("ThymeleafSampleMail", "ThymeleafSampleMail");
		return "Check Mail!";
		
	}

}
