package com.mediasmiths.mayam;

import java.net.URI;
import java.util.ArrayList;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mayam.wf.attributes.shared.AttributeMap;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.validation.MayamValidator;

public interface MayamClient
{

	public void shutdown();

	/* titles */
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title);

	public MayamClientErrorCode createTitle(Title title); // may be called when marketing material arrives with no placeholder

	public MayamClientErrorCode updateTitle(Material.Title title);

	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title);

	public MayamClientErrorCode purgeTitle(PurgeTitle title);

	public Title getTitle(String titleID, boolean includeVersionInfo) throws MayamClientException; // returns representation of title used in mediaexchange schema, includeVersionInfo is false when called as part of tx delivery as only information for the package being delivered is required

	public String getTitleOfPackage(String packageID) throws MayamClientException; // returns the title id a given package is associated with

	public boolean titleExists(String titleID) throws MayamClientException;

	/* material */
	public MayamClientErrorCode createMaterial(MaterialType material);

	public String createMaterial(String titleID, MarketingMaterialType material) throws MayamClientException;

	public MayamClientErrorCode updateMaterial(ProgrammeMaterialType material);

	public MayamClientErrorCode updateMaterial(MaterialType material);

	public MayamClientErrorCode deleteMaterial(DeleteMaterial deleteMaterial);

	public boolean materialExists(String materialID) throws MayamClientException;
	
	public Details getSupplierDetails(String materialID) throws MayamClientException; //called when producing companion xml for tx delivery

	// returns true if the specified material has not had media\essence ingested
	public boolean isMaterialPlaceholder(String materialID) throws MayamClientException;

	/* packages */
	public MayamClientErrorCode createPackage(PackageType txPackage);

	public MayamClientErrorCode updatePackage(PackageType txPackage);

	public MayamClientErrorCode updatePackage(ProgrammeMaterialType.Presentation.Package txPackage);

	public MayamClientErrorCode deletePackage(DeletePackage deletePackage);

	public boolean packageExists(String presentationID) throws MayamClientException;

	public boolean isMaterialForPackageProtected(String packageID) throws MayamClientException;

	public boolean isTitleOrDescendentsProtected(String titleID) throws MayamClientException;

	public MayamValidator getValidator();

	public ArrayList<String> getChannelLicenseTagsForMaterial(String materialID) throws MayamClientException;

	/**
	 * @param materialID
	 * @param location
	 * @return 
	 */
	public String pathToMaterial(String materialID) throws MayamClientException;

	PackageType getPackage(String packageID) throws MayamClientException;

	ProgrammeMaterialType.Presentation.Package getPresentationPackage(String packageID) throws MayamClientException;

	MaterialType getMaterial(String materialID) throws MayamClientException;

	public AttributeMap getTaskForAsset(MayamTaskListType type, String id) throws MayamClientException;

	public void saveTask(AttributeMap task) throws MayamClientException;

	public void failTaskForAsset(MayamTaskListType txDelivery, String id) throws MayamClientException;

	ProgrammeMaterialType getProgrammeMaterialType(String materialID) throws MayamClientException;

	String getMaterialIDofPackageID(String packageID) throws MayamClientException;

	public void createTxDeliveryFailureTask(String packageID, String failureReason) throws MayamClientException;

}