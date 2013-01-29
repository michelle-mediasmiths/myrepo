package com.mediasmiths.foxtel.tc.rest.impl;

import com.google.inject.Inject;
import com.mediasmiths.carbon.type.mutable.CarbonProject;
import com.mediasmiths.foxtel.carbonwfs.WfsClient;
import com.mediasmiths.foxtel.tc.rest.api.TCJobInfo;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCRestService;
import com.mediasmiths.foxtel.tc.rest.impl.build.CarbonProjectBuilder;
import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.rhozet.Job;
import org.jdom2.output.XMLOutputter;

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
		final UUID id = wfsClient.transcode(xml);

		// Set the initial job priority
		setJobPriority(id.toString(), parameters.priority);

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

		return marshaller.marshal(job);
	}

	@Override
	public void setJobPriority(final String guid, final Integer newPriority) throws Exception
	{
		final UUID id = UUID.fromString(guid);

		wfsClient.updatejobPriority(id, newPriority);
	}
}
