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

		TemplateCall call = templater.template("ThymeleafSampleMail");
		ThymeleafEmailSender testEmailWorker= new ThymeleafEmailSender();
		testEmailWorker.sendThymeleafEmail("matthew.mcparland@mediasmiths.com", "ThymeleafSampleMailtester",call.process());
		return "Check Mail!";
		
	}

}
