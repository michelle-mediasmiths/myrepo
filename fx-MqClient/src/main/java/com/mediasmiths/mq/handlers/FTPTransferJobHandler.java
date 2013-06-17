package com.mediasmiths.mq.handlers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.preview.PreviewEventUtil;
import org.apache.log4j.Logger;

import java.util.List;

public class FTPTransferJobHandler extends JobHandler
{

	private final static Logger log = Logger.getLogger(FTPTransferJobHandler.class);

	@Inject
	@Named("manualqa.destSvcID")
	private String qaService;

	@Inject
	private PreviewEventUtil preventEvent;

	@Override
	public void process(final Job jobMessage)
	{
		String assetId = jobMessage.getAssetId();
		Job.JobStatus jobStatus = jobMessage.getJobStatus();

		log.trace(String.format("assetId %s jobStatus %s", assetId, jobStatus.toString()));


		if (assetId == null)
		{
			log.error("null asset id in tranfer message");
			return; //no asset id cant do anything
		}

		if (!jobStatus.equals(Job.JobStatus.FINISHED))
		{
			log.info("Job was not finished, doing nothing");
			return;
		}

		final String destSvcId = jobMessage.getDestSvcId();

		if (destSvcId == null || !destSvcId.equals(qaService))
		{
			log.info("dest service was null or transfer was not to Manual QA service, doing nothing");
			return;
		}
		log.info("Transfer to Manual QA destination finished");

		final String user = jobMessage.getUser();
		log.debug("Transfer requested by user : " + user);

		log.debug("Searching for preview task for " + assetId);

		try
		{
			final List<AttributeMap> previewTasks = taskController.getOpenTasksForAsset(MayamTaskListType.PREVIEW,
			                                                                            Attribute.ASSET_ID,
			                                                                            assetId);
			if (previewTasks.size() > 0)
			{
				log.info("Item has preview task");
				if (previewTasks.size() > 1)
				{
					log.warn("Item has more than one preview task!");
				}

				for (AttributeMap task : previewTasks)
				{
					preventEvent.sendManualQANotification(task, Boolean.TRUE, user);
				}
			}
		}
		catch (MayamClientException e)
		{
			log.error("Error searching for preview tasks for asset " + assetId, e);
		}
	}


	@Override
	public String getName()
	{
		return "FTP Transfer Handler";
	}
}
