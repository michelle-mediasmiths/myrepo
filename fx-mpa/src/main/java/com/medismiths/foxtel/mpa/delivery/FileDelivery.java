package com.medismiths.foxtel.mpa.delivery;

import java.io.File;

import org.apache.log4j.Logger;

import com.mediasmiths.foxtel.generated.MediaExchange.Programme;

/**
 * 
 * see : 2.1.3.1 TNS File Delivery – Programme Content
 * 
 */
public class FileDelivery
{

	private static Logger logger = Logger.getLogger(FileDelivery.class);

	/**
	 * Called once both an asset and corresponding xml arrive
	 * 
	 * TODO: allow for additional xml with blackspot metadata
	 * 
	 * @param asset
	 * @param programmeXml
	 * @param programme
	 */
	public void onAssetAndXMLArrival(File xmlFile, File mediaFile, Programme programme)
	{

		// TODO: find master ID and aggregator

		/*try
		{
			// TODO: query master ID exists & Set aggregator

		}
		catch (DeliveryException de)
		{
			logger.error(
					String.format(
							"File delivery failed for asset %s and xml %s failed",
							asset.getAbsolutePath(),
							programmeXml.getAbsolutePath()),
					de);
			new ErrorHandling().handleFileDeliveryError(de, asset, programme, programmeXml);

		}
		*/

		// TODO: Move and rename asset & XML
		// TODO: notify
	}

}
