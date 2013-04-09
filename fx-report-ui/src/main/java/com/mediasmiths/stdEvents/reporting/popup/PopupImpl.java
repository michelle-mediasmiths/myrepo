package com.mediasmiths.stdEvents.reporting.popup;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.google.inject.Inject;
import com.mediasmiths.std.guice.thymeleaf.ThymeleafTemplater;
import com.mediasmiths.std.guice.web.rest.templating.TemplateCall;

public class PopupImpl implements PopupAPI
{
	@Inject
	private ThymeleafTemplater templater;

	@GET
	@Path("/demo")
	public String createPopup()
	{
		TemplateCall call = templater.template("popup");
		return call.process();
	}
}
