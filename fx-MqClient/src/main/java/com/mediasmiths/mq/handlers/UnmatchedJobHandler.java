package com.mediasmiths.mq.handlers;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.Job.JobSubType;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mayam.MayamTaskListType;

public class UnmatchedJobHandler extends JobHandler
{
	private final static Logger log = Logger.getLogger(IngestJobHandler.class);

	public void process(Job jobMessage)
	{
		String assetId = jobMessage.getAssetId();
		JobSubType jobSubType = jobMessage.getJobSubType();
		JobStatus jobStatus = jobMessage.getJobStatus();

		log.trace(String.format("assetId %s jobSubType %s", assetId, jobSubType.toString()));

		if (jobStatus.equals(JobStatus.FINISHED))
		{
			if (assetId != null)
			{
				AttributeMap material;
				try
				{
					material = materialController.getMaterialByAssetId(assetId);
				}
				catch (MayamClientException e1)
				{
					log.error("error fetching asset "+ assetId,e1);
					return;
				}

				String contentMaterialType = material.getAttribute(Attribute.CONT_MAT_TYPE);
				
				//check content type is unmatched 
				if (contentMaterialType != null && contentMaterialType.equals(MayamContentTypes.UNMATCHED))
				{
					//attempt to set the source as import or ingest
					material.setAttribute(Attribute.OP_TYPE, jobSubType.toString());
					
					try
					{
						tasksClient.assetApi().updateAsset(material);
					}
					catch (RemoteException e)
					{
						log.error("Error setting OP_TYPE on unmatched material",e);
					}

					//create qc task for material
					String houseID = material.getAttributeAsString(Attribute.HOUSE_ID);
					try
					{
						taskController.createQCTaskForMaterial(houseID, null);
					}
					catch (MayamClientException e)
					{
						log.error("Error creating qc task for asset " + assetId,e);
					}
				}
			}
		}
	}

	@Override
	public String getName()
	{
		return "Unmatched Job";
	}

}
