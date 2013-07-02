package com.mediasmiths.foxtel.tc.rest.impl;

import com.google.inject.Inject;
import com.mediasmiths.carbon.type.mutable.CarbonProject;
import com.mediasmiths.foxtel.carbonwfs.WfsClient;
import com.mediasmiths.foxtel.tc.reporting.TcReporting;
import com.mediasmiths.foxtel.tc.rest.api.TCJobInfo;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCRestService;
import com.mediasmiths.foxtel.tc.rest.impl.build.CarbonProjectBuilder;
import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.rhozet.Job;
import org.jdom2.output.XMLOutputter;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;

public class TCRestServiceImpl implements TCRestService
{
	private static final Logger log = Logger.getLogger(TCRestServiceImpl.class);

	@Inject
	WfsClient wfsClient;

	@Inject
	CarbonProjectBuilder projectBuilder;

	@Inject
	CarbonMarshaller marshaller = new CarbonMarshaller();

	@Inject
	TcReporting tcReporting;

	@Override
	public String ping()
	{
		return "OK";
	}

	@Override
	public String createJob(final TCJobParameters parameters) throws Exception
	{
		// Create the job XML
		final String xml = createPCPXML(parameters);

		// Create and start the job
		Job job = wfsClient.transcode(xml);
		final UUID id = UUID.fromString(job.getGuid());

		// Set the initial job priority
		setJobPriority(id.toString(), parameters.priority);

		tcReporting.recordStart(id, parameters, job);

		return id.toString();
	}

	@Override
	public String createPCPXML(final TCJobParameters parameters) throws Exception
	{
		CarbonProject project = projectBuilder.build(parameters);

		final String xml = serialise(project);

		return xml;
	}

	String serialise(CarbonProject project)
	{
		try
		{
			StringWriter sw = new StringWriter();

			new XMLOutputter().output(project.getElement(), sw);

			return sw.toString();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to serialise carbon project to pcp xml: " + e.getMessage(), e);
		}
	}

	@Override
	public TCJobInfo queryJob(final String guid) throws Exception
	{
		final UUID id = UUID.fromString(guid);

		// Query the job
		final Job job = wfsClient.getJob(id);

		tcReporting.recordStatus(id,job);

		return marshaller.marshal(job);
	}

	@Override
	public void setJobPriority(final String guid, final Integer newPriority) throws Exception
	{
		final UUID id = UUID.fromString(guid);

		wfsClient.updatejobPriority(id, newPriority);
	}

	@Override
	@DELETE
	@Path("/job/{guid}")
	public void deleteJob(@PathParam("guid") String guid) throws Exception
	{
		log.info(String.format("Received delete request for job %s", guid));

		UUID id = UUID.fromString(guid);

		final Job job = wfsClient.deleteJob(id);

		tcReporting.recordStatus(id,job);
	}
}
