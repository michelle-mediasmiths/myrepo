package com.mediasmiths.stdEvents.reporting.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/query")
public interface ReportUI
{
	@POST
	@Path("/choose_report")
	public void chooseReport(@FormParam("start")String start, @FormParam("end")String end, @FormParam("name")String name, @FormParam("rpt")String rpt);
	
	@GET
	@Path("/popup")
	@Produces("text/html")
	public String getPopup();
}