package com.mediasmiths.foxtel.tc.rest.impl;

import com.google.inject.Inject;
import com.mediasmiths.carbon.type.mutable.CarbonProject;
import com.mediasmiths.foxtel.carbonwfs.WfsClient;
import com.mediasmiths.foxtel.tc.rest.api.TCJobInfo;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCRestService;
import com.mediasmiths.foxtel.tc.service.CarbonProjectBuilder;
import org.apache.log4j.Logger;
import org.jdom2.output.XMLOutputter;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
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

	@Override
	public String ping()
	{
		return "OK";
	}

	@Override
	public String createJob(final TCJobParameters parameters) throws Exception
	{
		CarbonProject project = projectBuilder.build(parameters);

		final String xml = createPCPXML(project);

		final UUID id = wfsClient.transcode(xml);

		return id.toString();
	}

	String createPCPXML(CarbonProject project)
	{
		try
		{
			StringWriter sw = new StringWriter();

			new XMLOutputter().output(project.getDocument(), sw);

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
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setJobPriority(final String jobid, final Integer newPriority) throws Exception
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
