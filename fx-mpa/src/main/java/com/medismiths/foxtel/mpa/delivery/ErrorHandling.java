package com.medismiths.foxtel.mpa.delivery;

import java.io.File;

import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.medismiths.foxtel.mpa.delivery.exception.DeliveryException;

/**
 * 
 * @see 2.1.3.2 TNS File Delivery – Programme Content – Error Handling
 * 
 */
public class ErrorHandling
{
	public void handleFileDeliveryError(DeliveryException e, File asset, Material programme, File programmeXml)
	{
		// TODO: move asset and xml to quarantine

		// TODO: email notification
	}
}
