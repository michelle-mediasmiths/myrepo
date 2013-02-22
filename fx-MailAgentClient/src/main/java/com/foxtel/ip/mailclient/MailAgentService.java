package com.foxtel.ip.mailclient;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/mailinteface")
public interface MailAgentService
{

	@POST
	@Path("/send_mail")
	@Consumes("application/xml")
	@Produces("text/html")
	public String sendMail(ServiceCallerEntity caller);

	@GET
	@Path("/")
	@Produces("text/html")
	public String index();

}
