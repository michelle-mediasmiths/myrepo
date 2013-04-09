package com.mediasmiths.mayam.controllers;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package.Segmentation;
import com.mediasmiths.mayam.MayamClientException;


/**
 * Exists to allow injection of MayamPackageController into MayamMaterialController (gives Guice the means to resolve the circular dependency through proxying)
 *
 */
public interface PackageController
{

	/**
	 * Called when presentation information with an automation id is included in a material exchange message
	 * 
	 * The package with the given id may or may not exist on the pending tx package list
	 * 
	 * If it is on the pending tx package list then update its segmentation information
	 * 
	 * If it is not on the pending tx package list then create an entry.
	 * @throws MayamClientException 
	 * 
	 */
	void createOrUpdatePendingTxPackagesSegmentInfo(
			AttributeMap materialAttributes,
			String materialID,
			String packageID,
			Segmentation segmentation) throws MayamClientException;

}