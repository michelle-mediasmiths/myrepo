package com.mediasmiths.foxtel.tc.rest.impl;

import com.google.inject.Singleton;
import com.mediasmiths.foxtel.tc.rest.api.TCJobInfo;
import com.mediasmiths.foxtel.tc.rest.api.TCJobResult;
import org.apache.commons.lang.StringUtils;
import org.datacontract.schemas._2004._07.rhozet.DataObject;
import org.datacontract.schemas._2004._07.rhozet.Job;
import org.datacontract.schemas._2004._07.rhozet.JobStatus;
import org.datacontract.schemas._2004._07.rhozet.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Marshals Rhozet's data model to our TCJobInfo data model
 */
@Singleton
public class CarbonMarshaller
{
	public TCJobInfo marshal(final Job job)
	{
		// Marshal to a TCJobInfo
		TCJobInfo info = new TCJobInfo();

		info.id = job.getGuid();
		info.result = marshal(job.getStatus());
		info.errorDetail = getErrorMessages(job);
		info.priority = job.getPriority();

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


	private TCJobResult marshal(JobStatus status)
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
				throw new RuntimeException("Unknown job status: " + status);
		}
	}
}
