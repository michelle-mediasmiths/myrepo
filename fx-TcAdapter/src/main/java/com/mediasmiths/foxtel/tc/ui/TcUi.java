package com.mediasmiths.foxtel.tc.ui;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.mediasmiths.foxtel.carbonwfs.WfsClientException;
import com.mediasmiths.foxtel.tc.JobBuilderException;
import com.mediasmiths.mayam.MayamClientException;

@Path("/")
public interface TcUi {
	@GET
	@Path("/")
	@Produces("text/html")
	public String getIndex();

	@GET
	@Path("/index.html")
	@Produces("text/html")
	public String getIndexHtml();

	
	@GET
	@Path("/jobs.html")
	@Produces("text/html")
	public String getJobListing();
	
	@GET
	@Path("/job/{jobID}")
	@Produces("text/html")
	public String getJobInfo(@PathParam("jobID") String jobid);
	
	@GET
	@Path("/build.html")
	@Produces("text/html")
	public String getJobBuildPage();
	
	@POST
	@Path("/build.html")
	@Produces("text/html")
	public String doBuild(
			@FormParam("packageID") String packageID,
			@FormParam("inputFile") String inputFile,
			@FormParam("outputFolder") String outputFolder) throws MayamClientException, JobBuilderException;
	
	@GET
	@Path("/start.html")
	@Produces("text/html")
	public String getStartJobPage();
	
	@POST
	@Path("/start.html")
	@Produces("text/html")
	public String doStart(
			@FormParam("pcpXml") String pcpXml,
			@FormParam("jobName") @DefaultValue("Job") String jobName,
			@FormParam("priority") @DefaultValue("5") Integer priority) throws MayamClientException, JobBuilderException, WfsClientException;
	
	
	
}
