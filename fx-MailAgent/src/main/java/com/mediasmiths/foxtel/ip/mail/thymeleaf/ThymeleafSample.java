package com.mediasmiths.foxtel.ip.mail.thymeleaf;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/thymeleaf")
public interface ThymeleafSample
{
	@GET
	@Path("/demo")
	public String createPopup();
	
	@GET
	@Path("/sendMail")
	public String sendMail();
}
