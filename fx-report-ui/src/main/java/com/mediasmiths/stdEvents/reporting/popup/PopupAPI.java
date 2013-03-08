package com.mediasmiths.stdEvents.reporting.popup;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/popup")
public interface PopupAPI
{
	@GET
	@Path("/demo")
	public String createPopup();
}
