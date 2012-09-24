package com.mediasmiths.mayam;

import com.mediasmiths.mayam.controllers.MayamTaskController;

public class AlertImpl implements AlertInterface{

	public AlertImpl() {
		
	}
	
	@Override
	public void sendAlert(String destination, String subject, Object contents) {
		// TODO: Send email alerts
		
	}

	@Override
	public long createAlert(String assetID, MayamAssetType assetType, MayamTaskController controller) throws MayamClientException {
		long taskID = 0;
		try {
			taskID = controller.createTask(assetID, assetType, MayamTaskListType.GENERIC_TASK_ERROR);
		} catch (MayamClientException e) {
			throw new MayamClientException(MayamClientErrorCode.MAYAM_EXCEPTION);
		}
		return taskID;
	}

}
