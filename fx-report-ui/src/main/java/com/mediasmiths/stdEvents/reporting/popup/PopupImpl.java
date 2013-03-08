package com.mediasmiths.stdEvents.reporting.popup;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.google.inject.Inject;
import com.mediasmiths.std.guice.thymeleaf.TemplateCall;
import com.mediasmiths.std.guice.thymeleaf.Templater;

public class PopupImpl implements PopupAPI
{
	@Inject
	private Templater templater;

	@GET
	@Path("/demo")
	public String createPopup()
	{
		TemplateCall call = templater.template("popup");
		return call.process();
	}
}
