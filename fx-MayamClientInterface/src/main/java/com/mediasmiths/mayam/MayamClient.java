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
import com.mediasmiths.foxtel.generated.ruzz.DetailType;
import com.mediasmiths.mayam.validation.MayamValidator;

public interface MayamClient
{

	public void shutdown();

	/**
	 * Creates a title, called by placeholder management agent
	 * @param title
	 * @return
	 */
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title);

	/**
	 * Creates a title using the information in a marketing material message
	 * 
	 * called by media pickup agent
	 * 
	 * @param title
	 * @return
	 */
	public MayamClientErrorCode createTitle(Title title); // may be called when marketing material arrives with no placeholder

	/**
	 * updates title information from a material exchange message
	 * @param title
	 * @return
	 */
	public MayamClientErrorCode updateTitle(Material.Title title);

	/**
	 * updates a title from a placeholder management message
	 * @param title
	 * @return
	 */
	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title);

	/**
	 * actions a purge title request (called by placeholder management agent)
	 * @param title
	 * @return
	 */
	public MayamClientErrorCode purgeTitle(PurgeTitle title);

	/**
	 * 
	 * returns representation of title used in mediaexchange schema, includeVersionInfo is false when called as part of tx delivery as only information for the package being delivered is required
	 * 
	 * @param titleID
	 * @param includeVersionInfo
	 * @return
	 * @throws MayamClientException
	 */
	public Title getTitle(String titleID, boolean includeVersionInfo) throws MayamClientException;

	/**
	 * returns the title id a given package is associated with
	 * 
	 * @param packageID
	 * @return
	 * @throws MayamClientException
	 */
	public String getTitleOfPackage(String packageID) throws MayamClientException;

	/**
	 * returns true if a title with the given id exists
	 * @param titleID
	 * @return
	 * @throws MayamClientException
	 */
	public boolean titleExists(String titleID) throws MayamClientException;

	/**
	 * Create a material using the placeholder management MaterialType
	 * @param material
	 * @param titleID
	 * @return
	 */
	public MayamClientErrorCode createMaterial(MaterialType material, String titleID);
	
	/**
	 * Create a material using the material exchange MarketingMaterialType
	 * @param titleID
	 * @param material
	 * @return
	 * @throws MayamClientException
	 */
	public String createMaterial(String titleID, MarketingMaterialType material) throws MayamClientException;

	/**
	 * update a material using the material exchange ProgrammeMaterialType
	 * @param material
	 * @return
	 */
	public MayamClientErrorCode updateMaterial(ProgrammeMaterialType material);
	/**
	 * update a material using the placeholder management MaterialType
	 * @param material
	 * @return
	 */
	public MayamClientErrorCode updateMaterial(MaterialType material);

	/**
	 * actions a placeholder management delete material message
	 * @param deleteMaterial
	 * @return
	 */
	public MayamClientErrorCode deleteMaterial(DeleteMaterial deleteMaterial);

	/**
	 * returns true if a material with the given id exists
	 * @param materialID
	 * @return
	 * @throws MayamClientException
	 */
	public boolean materialExists(String materialID) throws MayamClientException;
	
	public Details getSupplierDetails(String materialID) throws MayamClientException; //called when producing companion xml for tx delivery

	/**
	 * returns true if the specified material has not had media\essence ingested
	 * @param materialID
	 * @return
	 * @throws MayamClientException
	 */
	public boolean isMaterialPlaceholder(String materialID) throws MayamClientException;

	/**
	 * Creates a tx package from the placeholder management PackageType	
	 * @param txPackage
	 * @return
	 */
	public MayamClientErrorCode createPackage(PackageType txPackage);

	/**
	 * updates a tx package using the placeholder management PackageType
	 * @param txPackage
	 * @return
	 */
	public MayamClientErrorCode updatePackage(PackageType txPackage);
	
	/**
	 * updates a tx package using the material exchange ProgrammeMaterialType.Presentation.Package type
	 * @param txPackage
	 * @param materialID 
	 * @return
	 */
	public MayamClientErrorCode updatePackage(ProgrammeMaterialType.Presentation.Package txPackage, String materialID);

	/**
	 * actions a placeholder management delete package message
	 * @param deletePackage
	 * @return
	 */
	public MayamClientErrorCode deletePackage(DeletePackage deletePackage);

	/**
	 * returns true if a package with the given presenetationID exists for the given material
	 * @param presentationID
	 * @return
	 * @throws MayamClientException
	 */
	public boolean packageExistsForMaterial(String presentationID, String materialID) throws MayamClientException;

	/**
	 * returns true if a package with the given presenetationID exists for the given title
	 * @param presentationID
	 * @return
	 * @throws MayamClientException
	 */
	public boolean packageExistsForTitle(String presentationID, String titleID) throws MayamClientException;

	public boolean isMaterialForPackageProtected(String packageID, String titleID) throws MayamClientException;

	public boolean isTitleOrDescendentsProtected(String titleID) throws MayamClientException;

	public MayamValidator getValidator();

	/**
	 * Returns a list of channels that have licences for this material
	 * @param materialID
	 * @return
	 * @throws MayamClientException
	 */
	public ArrayList<String> getChannelLicenseTagsForMaterial(String materialID) throws MayamClientException;

	/**
	 * 
	 * Returns the file path to the high resolution media attatched to a material 
	 * 
	 * @param materialID
	 * @param location
	 * @return 
	 */
	public String pathToMaterial(String materialID) throws MayamClientException;

	/**
	 * Return the placeholder management system representation of a package
	 * @param packageID
	 * @return
	 * @throws MayamClientException
	 */
	PackageType getPackage(String packageID) throws MayamClientException;

	/**
	 * Return the material exchange representation of a package
	 * @param packageID
	 * @return
	 * @throws MayamClientException
	 */
	ProgrammeMaterialType.Presentation.Package getPresentationPackage(String packageID) throws MayamClientException;

	/**
	 * Returns the MaterialType representation of a given material
	 * @param materialID
	 * @return
	 * @throws MayamClientException
	 */
	MaterialType getMaterial(String materialID) throws MayamClientException;

	/**
	 * Return a single task of a given type for the specified asset
	 * @param type
	 * @param id
	 * @return
	 * @throws MayamClientException
	 */
	public AttributeMap getTaskForAsset(MayamTaskListType type, String assetid) throws MayamClientException;

	/**
	 * Save the given task
	 * @param task
	 * @throws MayamClientException
	 */
	public void saveTask(AttributeMap task) throws MayamClientException;

	/**
	 * Called to indicate that a given task for an asset should be considered failed or erronous
	 * @param txDelivery
	 * @param id
	 * @throws MayamClientException
	 */
	public void failTaskForAsset(MayamTaskListType taskType, String id) throws MayamClientException;

	/**
	 * returns the ProgrammeMaterial representation of a given material
	 * @param materialID
	 * @return
	 * @throws MayamClientException
	 */
	ProgrammeMaterialType getProgrammeMaterialType(String materialID) throws MayamClientException;

	/**
	 * Returns the material id that a given package is associated with
	 * @param packageID
	 * @return
	 * @throws MayamClientException
	 */
	String getMaterialIDofPackageID(String packageID) throws MayamClientException;

	/**
	 * called by wfapapter when it learns of a tx delivery failure
	 * @param packageID
	 * @param failureReason
	 * @throws MayamClientException
	 */
	
	public void createTxDeliveryFailureTask(String packageID, String failureReason) throws MayamClientException;

	/**
	 * updates a material based on the ruzz ingest DetailsType
	 * @param details
	 * @param materialID
	 * @throws MayamClientException
	 */
	public void updateMaterial(DetailType details, String materialID) throws MayamClientException;

}