package com.mediasmiths.mq.handlers.ingest;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mediasmiths.mayam.MayamAssetType;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.attributes.shared.type.QcStatus;
import com.mediasmiths.mayam.MayamTaskListType;
import com.mediasmiths.mq.handlers.TaskUpdateHandler;



public class IngestCompleteHandler extends TaskUpdateHandler
{

	private final static Logger log = Logger.getLogger(IngestCompleteHandler.class);
	
	@Override
	public String getName()
	{
		return "IngestTaskUpdate";
	}


	@Override
	public void onTaskUpdate(AttributeMap currentAttributes, AttributeMap before, AttributeMap after)
	{
		AssetType assetType = currentAttributes.getAttribute(Attribute.ASSET_TYPE);

		if (!assetType.equals(MayamAssetType.MATERIAL.getAssetType()))
		{
			return; 
		}

		String assetID = currentAttributes.getAttribute(Attribute.ASSET_ID);
		String houseID = currentAttributes.getAttribute(Attribute.HOUSE_ID);
		
		QcStatus qcStatus = currentAttributes.getAttribute(Attribute.QC_STATUS);

		if ((null != qcStatus) && !qcStatus.equals(QcStatus.TBD))
		{
			log.debug("asset already has a non TBD qc status");
			return; 
		}

		boolean readyForQCBefore = isMaterialsReadyForQC(before);

		if (readyForQCBefore)
		{
			log.debug("asset was already ready for qc before this attribute update.");
			return; 
		}

		boolean readyForQCAfter = isMaterialsReadyForQC(currentAttributes);

		if (!readyForQCAfter)
		{
			log.debug("asset is not ready for QC yet, continue to wait for asset update.");
			return; 
		}

		if ((!readyForQCBefore) && readyForQCAfter)
		{
			log.info(String.format("Asset %s (%s) was not ready for qc and now is", houseID, assetID));

			try
            {
                AttributeMap task = taskController.getOnlyOpenTaskForAssetByAssetID(MayamTaskListType.INGEST, assetID);
                if (task != null)
                {
                	TaskState taskState = (TaskState) task.getAttribute(Attribute.TASK_STATE);
					if ((null != taskState) && (!taskState.equals(TaskState.FINISHED)))
					{
						log.info(String.format("Ingest task is set to finished status for asset %s", assetID));
                        task.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
                        taskController.saveTask(task);
					}
                        
                }
                else
                {
                        log.debug("Could not find Ingest task for asset " + assetID);
                }
            }
            catch (MayamClientException e)
            {
                    log.error("Exception thrown while trying to get Ingest Task for asset " + assetID, e);
            }

		}
		else
		{
			log.error("did not expect to reach this line, check logic in IngestCompleteHandler.process");
		}

	}

	private boolean isMaterialsReadyForQC(AttributeMap attributes)
	{
		MediaStatus hr = attributes.getAttribute(Attribute.MEDST_HR);
        MediaStatus lr = attributes.getAttribute(Attribute.MEDST_LR);

        log.debug("MEDST_HR : " + hr);

        if (hr == null)
        {
            log.debug("MEDST_HR is null, material is not ready for qc");
            return false;
        }
        else
        {
            log.debug("MEDST_HR is " + hr.toString());
            if (!hr.equals(MediaStatus.READY))
            {
                    log.debug("MEDST_HR is not READY, material is not ready for qc");
                    return false;
            }
            else
            {
                    log.debug("MEDST_HR is READY");
            }
        }

        log.debug("MEDST_LR : " + lr);

        if (lr == null)
        {
            log.debug("MEDST_LR is null, material is not ready for qc");
            return false;
        }
        else
        {
            log.debug("MEDST_LR is " + lr.toString());
            if (!lr.equals(MediaStatus.READY))
            {
                    log.debug("MEDST_LR is not READY, material is not ready for qc");
                    return false;
            }
            else
            {
                    log.debug("MEDST_LR is READY, material is ready for qc");
            }

        }

        String assetID = attributes.getAttribute(Attribute.ASSET_ID);

        try
        {
            String assetPath = materialController.getAssetPath(assetID, false);
            log.debug(String.format("asset is at %s material is ready for qc", assetPath));
            return true;
        }
        catch (MayamClientException e)
        {
            if (e.getErrorcode().equals(MayamClientErrorCode.FILE_NOT_IN_PREFERRED_LOCATION))
            {
                    log.debug("Asset is not in preferred location (hires), not ready for qc", e);
                    return false;
            }
            else
            {
                    log.error("unexpected exception queurying assets location", e);
                    return false;
            }
        }

	}

	@Override
	public MayamTaskListType getTaskType()
	{
		return MayamTaskListType.INGEST;
	}

}
