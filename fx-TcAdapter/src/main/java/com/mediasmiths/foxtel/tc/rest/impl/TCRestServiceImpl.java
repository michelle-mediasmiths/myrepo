package com.mediasmiths.foxtel.tc.rest.impl;

import com.google.inject.Inject;
import com.mediasmiths.carbon.type.mutable.CarbonProject;
import com.mediasmiths.foxtel.carbonwfs.WfsClient;
import com.mediasmiths.foxtel.tc.rest.api.TCJobInfo;
import com.mediasmiths.foxtel.tc.rest.api.TCJobParameters;
import com.mediasmiths.foxtel.tc.rest.api.TCJobResult;
import com.mediasmiths.foxtel.tc.rest.api.TCRestService;
import com.mediasmiths.foxtel.tc.service.CarbonProjectBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.rhozet.DataObject;
import org.datacontract.schemas._2004._07.rhozet.Job;
import org.datacontract.schemas._2004._07.rhozet.JobStatus;
import org.datacontract.schemas._2004._07.rhozet.Task;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
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
		final String xml = createPCPXML(parameters);

		final UUID id = wfsClient.transcode(xml);

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
		final UUID id = UUID.fromString(guid);

		// Query the job
		final Job job = wfsClient.getJob(id);

		// Marshal to a TCJobInfo
		TCJobInfo info = new TCJobInfo();

		info.id = job.getGuid();
		info.result = translateState(job.getStatus());
		info.errorDetail = getErrorMessages(job);

		return info;
	}

	private String getErrorMessages(final Job job)
	{
		List<String> errorPropertes = new ArrayList<String>();
		for (Task t : job.getTask().getValue().getTask())
		{
			for (DataObject property : t.getProperty().getValue().getDataObject())
			{
				if (property.getName().getValue().equals("Error"))
				{
					errorPropertes.add((property.getValue().getValue()));
				}
			}
		}

		return StringUtils.join(errorPropertes, "\r\n");
	}


	protected TCJobResult translateState(JobStatus status)
	{
		switch (status)
		{
			case QUEUED:
			case PAUSED:
			case ACTIVE:
			case INACTIVE:
			case PAUSING:
				return null; // Job has not completed
			case FATAL:
				return TCJobResult.FAILURE;
			case COMPLETED:
				return TCJobResult.SUCCESS;
			case ABORT:
				return TCJobResult.FAILURE;

			default:
				throw new RuntimeException("Unknown job status: " + job.getStatus());
		}
	}

	@Override
	public void setJobPriority(final String guid, final Integer newPriority) throws Exception
	{
		final UUID id = UUID.fromString(guid);

		wfsClient.updatejobPriority(id, newPriority);
	}
}
