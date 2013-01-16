package com.mediasmiths.foxtel.tc.ui;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang.StringUtils;
import org.datacontract.schemas._2004._07.rhozet.Job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mediasmiths.foxtel.carbonwfs.WfsClient;
import com.mediasmiths.foxtel.carbonwfs.WfsClientException;
import com.mediasmiths.foxtel.pathresolver.UnknownPathException;
import com.mediasmiths.foxtel.tc.JobBuilderException;
import com.mediasmiths.foxtel.tc.model.TCBuildJobXMLRequest;
import com.mediasmiths.foxtel.tc.model.TCStartRequest;
import com.mediasmiths.foxtel.tc.service.TCRestService;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.std.guice.thymeleaf.TemplateCall;
import com.mediasmiths.std.guice.thymeleaf.Templater;

@Singleton
public class TcUiImpl implements TcUi {

	@Inject
	private Templater templater;

	@Inject
	private TCRestService tcService;

	@Inject
	private WfsClient wfsClient;

	@Override
	@GET
	@Path("/")
	@Produces("text/html")
	public String getIndex() {
		TemplateCall call = templater.template("index");

		call.set("dashboard", wfsClient.getDashBoard());
		
		return call.process();
	}

	private static final long TICKS_AT_EPOCH = 621355968000000000L;
    private static final long TICKS_PER_MILLISECOND = 10000;
	
	@Override
	@GET
	@Path("/jobs.html")
	@Produces("text/html")
	public String getJobListing() {
		List<Job> listJobs = tcService.listJobs();
		
		Map<Long, Date> jobDates = new HashMap<Long,Date>();
		
		for(Job j : listJobs){
			//convert .NET 'ticks' base date to EPOCH based date
			jobDates.put(j.getStarted(), fromDotNETTicks(j.getStarted()));
			jobDates.put(j.getLastUpdate(), fromDotNETTicks(j.getLastUpdate()));
		}
		
		TemplateCall call = templater.template("jobs");
		call.set("jobs", listJobs);
		call.set("jobDates", jobDates);

		return call.process();
	}

	private Date fromDotNETTicks(Long ticks){
		return new Date((ticks.longValue() - TICKS_AT_EPOCH) / TICKS_PER_MILLISECOND);
	}
	
	@Override
	@GET
	@Path("/index.html")
	@Produces("text/html")
	public String getIndexHtml() {
		return getIndex();
	}

	@Override
	@GET
	@Path("/build.html")
	@Produces("text/html")
	public String getJobBuildPage() {

		TemplateCall call = templater.template("build");

		return call.process();
	}

	@Override
	@POST
	@Path("/build.html")
	@Produces("text/html")
	public String doBuild(@FormParam("packageID") String packageID,
			@FormParam("inputFile") String inputFile,
			@FormParam("outputFolder") String outputFolder) throws MayamClientException, JobBuilderException, UnknownPathException {
	
		if (StringUtils.isEmpty(packageID))
			throw new IllegalArgumentException("Must provide a package id!");
		if (StringUtils.isEmpty(inputFile))
			throw new IllegalArgumentException("Must provide an input file!");
		if (StringUtils.isEmpty(outputFolder))
			throw new IllegalArgumentException("Must provide an output location!");
		
		
		TCBuildJobXMLRequest req = new TCBuildJobXMLRequest();
		req.setPackageID(packageID);
		req.setInputFile(inputFile);
		req.setOutputFolder(outputFolder);
		req.setTxDate(new Date());
		String pcpXML = tcService.buildJobXMLForTranscode(req).getPcpXML();
		
		TemplateCall call = templater.template("built");

		call.set("pcpXml", pcpXML);
		
		return call.process();
	}

	@Override
	@POST
	@Path("/start.html")
	@Produces("text/html")
	public String doStart(@FormParam("pcpXml") String pcpXml,
			@FormParam("jobName") @DefaultValue("Job") String jobName,
			@FormParam("priority") @DefaultValue("5") Integer priority)
			throws MayamClientException, JobBuilderException, WfsClientException {
		
		if (StringUtils.isEmpty(pcpXml))
			throw new IllegalArgumentException("Must provide job xml!");
		
		
		TCStartRequest startRequest = new TCStartRequest();
		startRequest.setPcpXml(pcpXml);
		startRequest.setJobName(jobName);
		startRequest.setPriority(priority);
		
		UUID transcode = tcService.transcode(startRequest);
		
		TemplateCall call = templater.template("started");

		call.set("jobid", transcode.toString());
		
		return call.process();
		
	}

	@Override
	@GET
	@Path("job.html")
	@Produces("text/html")
	public String getJobInfo(@QueryParam("jobID") String jobid) {
		
		Job job = tcService.job(jobid);
		
		TemplateCall call = templater.template("job");

		call.set("job", job);
		call.set("started",fromDotNETTicks(job.getStarted()));
		call.set("updated",fromDotNETTicks(job.getLastUpdate()));
		
		return call.process();
	}

	@Override
	@GET
	@Path("/start.html")
	@Produces("text/html")
	public String getStartJobPage() {
		TemplateCall call = templater.template("start");
		
		return call.process();
	}

}
