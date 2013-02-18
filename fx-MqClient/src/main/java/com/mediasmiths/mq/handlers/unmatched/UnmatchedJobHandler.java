package com.mediasmiths.mq.handlers.unmatched;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jasypt.encryption.pbe.CleanablePasswordBased;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.FileFormatInfo;
import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.Job.JobSubType;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.MayamContentTypes;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.JobHandler;
import com.mediasmiths.mq.handlers.ingest.IngestJobHandler;

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
					AttributeMap updateMap = taskController.updateMapForAsset(material);
					updateMap.setAttribute(Attribute.OP_TYPE, jobSubType.toString());
					
					String fileName = getFileName((String)material.getAttribute(Attribute.ASSET_ID));
					updateMap.setAttribute(Attribute.ASSET_TITLE, fileName);
					
					try
					{
						tasksClient.assetApi().updateAsset(updateMap);
					}
					catch (RemoteException e)
					{
						log.error("Error setting OP_TYPE on unmatched material",e);
					}

					//create qc task for material
					String houseID = material.getAttributeAsString(Attribute.HOUSE_ID);
					try
					{
						taskController.createQCTaskForMaterial(houseID, null,null, material);
					}
					catch (MayamClientException e)
					{
						log.error("Error creating qc task for asset " + assetId,e);
					}
				}
			}
		}
	}

	private String getFileName(String assetID)
	{
		String path;
		try
		{
			path = materialController.getAssetPath(assetID);
		}
		catch (MayamClientException e)
		{
			log.error("error getting path to file for asset "+ assetID);
			return "";
		}
		
		return FilenameUtils.getName(path);
		
	}

	@Override
	public String getName()
	{
		return "Unmatched Job";
	}

}
