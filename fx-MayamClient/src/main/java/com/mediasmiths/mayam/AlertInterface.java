package com.mediasmiths.mayam;

import com.mediasmiths.mayam.controllers.MayamTaskController;

public interface AlertInterface {
	public void sendAlert(String destination, String subject, Object contents);
	public long createAlert(String assetID, MayamAssetType assetType, MayamTaskController controller) throws MayamClientException;
}
