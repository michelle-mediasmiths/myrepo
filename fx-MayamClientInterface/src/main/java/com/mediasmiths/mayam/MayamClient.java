package com.mediasmiths.mayam;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PurgeTitle;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.materialexport.MaterialExport;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.outputruzz.RuzzIF;
import com.mediasmiths.foxtel.generated.ruzz.DetailType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface MayamClient
{

	/**
	 * Creates a title, called by placeholder management agent
	 *
	 * @param title
	 *
	 * @return
	 */
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title);

	/**
	 * Creates a title using the information in a marketing material message
	 * <p/>
	 * called by media pickup agent
	 *
	 * @param title
	 *
	 * @return
	 */
	public MayamClientErrorCode createTitle(Title title); // may be called when marketing material arrives with no placeholder

	/**
	 * updates title information from a material exchange message
	 *
	 * @param title
	 *
	 * @return
	 */
	public MayamClientErrorCode updateTitle(Material.Title title);

	/**
	 * updates a title from a placeholder management message
	 *
	 * @param title
	 *
	 * @return
	 */
	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title);

	/**
	 * actions a purge title request (called by placeholder management agent)
	 *
	 * @param title
	 *
	 * @return
	 */
	public MayamClientErrorCode purgeTitle(PurgeTitle title);

	/**
	 * returns true if a title with the given id exists
	 *
	 * @param titleID
	 *
	 * @return
	 *
	 * @throws MayamClientException
	 */
	public boolean titleExists(String titleID) throws MayamClientException;

	/**
	 * Create a material using the placeholder management MaterialType
	 *
	 * @param material
	 * @param titleID
	 *
	 * @return
	 */
	public MayamClientErrorCode createMaterial(MaterialType material, String titleID);

	/**
	 * Create a material using the material exchange MarketingMaterialType
	 *
	 * @param titleID
	 * @param material
	 *
	 * @return
	 *
	 * @throws MayamClientException
	 */
	public String createMaterial(String titleID,
	                             MarketingMaterialType material,
	                             Material.Details details,
	                             Material.Title title) throws MayamClientException;

	/**
	 * update a material using the material exchange ProgrammeMaterialType
	 *
	 * @param material
	 *
	 * @return
	 */
	public boolean updateMaterial(ProgrammeMaterialType material,
	                              Material.Details details,
	                              Material.Title title) throws MayamClientException;

	/**
	 * update a material using the placeholder management MaterialType
	 *
	 * @param material
	 *
	 * @return
	 */
	public MayamClientErrorCode updateMaterial(MaterialType material);

	/**
	 * actions a placeholder management delete material message
	 *
	 * @param deleteMaterial
	 *
	 * @return
	 */
	public MayamClientErrorCode deleteMaterial(DeleteMaterial deleteMaterial);

	/**
	 * returns true if a material with the given id exists
	 *
	 * @param materialID
	 *
	 * @return
	 *
	 * @throws MayamClientException
	 */
	public boolean materialExists(String materialID) throws MayamClientException;

	/**
	 * returns true if the specified material has not had media\essence ingested
	 *
	 * @param materialID
	 *
	 * @return
	 *
	 * @throws MayamClientException
	 */
	public boolean isMaterialPlaceholder(String materialID) throws MayamClientException;

	/**
	 * Creates a tx package from the placeholder management PackageType
	 *
	 * @param txPackage
	 *
	 * @return
	 */
	public MayamClientErrorCode createPackage(PackageType txPackage);

	/**
	 * updates a tx package using the placeholder management PackageType
	 *
	 * @param txPackage
	 *
	 * @return
	 */
	public MayamClientErrorCode updatePackage(PackageType txPackage);

	/**
	 * actions a placeholder management delete package message
	 *
	 * @param deletePackage
	 *
	 * @return
	 */
	public MayamClientErrorCode deletePackage(DeletePackage deletePackage);

	public boolean isTitleOrDescendentsProtected(String titleID) throws MayamClientException;

	/**
	 * Returns a list of channels that have licences for this material
	 *
	 * @param materialID
	 *
	 * @return
	 *
	 * @throws MayamClientException
	 */
	public ArrayList<String> getChannelLicenseTagsForMaterial(String materialID) throws MayamClientException;

	public Set<String> getChannelGroupsForTitle(String titleID) throws MayamClientException;

	/**
	 * Returns the file path to the high resolution media attatched to a material
	 *
	 * @param materialID
	 *
	 * @return
	 */
	public String pathToMaterial(String materialID, boolean acceptNonPreferredLocations) throws MayamClientException;

	/**
	 * Return a single task of a given type for the specified asset
	 *
	 * @param type
	 * @param assetid
	 *
	 * @return
	 *
	 * @throws MayamClientException
	 */
	public AttributeMap getOnlyTaskForAsset(MayamTaskListType type, String assetid) throws MayamClientException;

	/**
	 * Save the given task
	 *
	 * @param task
	 *
	 * @throws MayamClientException
	 */
	public void saveTask(AttributeMap task) throws MayamClientException;

	public long createWFEErrorTaskNoAsset(String id, String title, String message) throws MayamClientException;

	public long createWFEErrorTaskNoAsset(String id,
	                                      String title,
	                                      String message,
	                                      boolean isAOContent) throws MayamClientException;

	public long createWFEErrorTaskForPackage(String packageID, String message);

	public long createWFEErrorTaskForMaterial(String materialID, String message);

	public long createWFEErrorTaskForTitle(String titleID, String message);

	/**
	 * returns the Programme representation of a given package
	 *
	 * @param packageID
	 *
	 * @return
	 *
	 * @throws MayamClientException
	 */
	Programme getProgramme(String packageID) throws MayamClientException;

	/**
	 * returns a string containing material and package metadata used to accompany compliance and publicity exports
	 */
	String getTextualMetatadaForMaterialExport(String materialId) throws MayamClientException;

	/**
	 * @param packageID
	 *
	 * @return
	 *
	 * @throws MayamClientException
	 */
	RuzzIF getRuzzProgramme(String packageID) throws MayamClientException;

	/**
	 * Returns the material id that a given package is associated with
	 *
	 * @param packageID
	 *
	 * @return
	 *
	 * @throws MayamClientException
	 */
	String getMaterialIDofPackageID(String packageID) throws MayamClientException;

	/**
	 * updates a material based on the ruzz ingest DetailsType
	 *
	 * @param details
	 * @param materialID
	 *
	 * @throws MayamClientException
	 */
	public void updateMaterial(DetailType details, String materialID) throws MayamClientException;

	boolean packageExists(String presentationID) throws MayamClientException;

	public boolean isPackageAO(String packageID) throws MayamClientException;

	public void autoQcFailedForMaterial(String assetId, long taskID) throws MayamClientException;

	public void autoQcPassedForMaterial(String assetId, long taskID) throws MayamClientException;

	public void attachFileToMaterial(String materialID, String absolutePath, String serviceHandle) throws MayamClientException;

	public AttributeMap getMaterialAttributes(String materialID) throws MayamClientException;

	public AttributeMap getPackageAttributes(String packageID) throws MayamClientException;

	/**
	 * @param materialID
	 *
	 * @return the last delivery version used to update an item, or -1 if it has not been updated by a material exchange message
	 */
	public int getLastDeliveryVersionForMaterial(String materialID);

	public boolean materialHasPassedPreview(String materialID);

	public boolean attemptAutoMatch(String siteID, String fileName);

	public long createWFEErrorTaskForUnmatched(String aggregator, String fileName);

	public boolean autoQcRequiredForTXTask(Long taskID) throws MayamClientException;

	public boolean isPackageSD(String packageID) throws MayamClientException;

	public void txDeliveryCompleted(String packageID, long taskID) throws MayamClientException;

	public void txDeliveryFailed(String packageID, long taskID, String stage) throws MayamClientException;

	public TaskState getTaskState(long taskID) throws MayamClientException;

	public boolean titleIsAO(String titleID) throws MayamClientException;

	public void exportCompleted(long taskID) throws MayamClientException;

	void exportFailed(long taskID, String reason) throws MayamClientException;

	public void addMaterialToPurgeCandidateList(String materialID, int daysUntilPurge) throws MayamClientException;

	public List<AttributeMap> getAllPurgeCandidatesPendingDeletion() throws MayamClientException;

	public boolean deletePurgeCandidates() throws MayamClientException;

	public AttributeMap getTask(long taskId);

	public void setNaturalBreaks(String materialID, String naturalBreaks) throws MayamClientException;

	public void ruzzQCMessagesDetected(String materialID) throws MayamClientException;

	public void autoQcErrorForMaterial(String assetId, long taskID) throws MayamClientException;

	Set<String> getChannelGroupsForItem(AttributeMap itemAttributes) throws MayamClientException;

	Set<String> getChannelGroupsForItem(String materialId) throws MayamClientException;

	Set<String> getChannelGroupsForPackage(String packageId) throws MayamClientException;

	SegmentList getTxPackage(String presentationID, String materialID) throws PackageNotFoundException, MayamClientException;

	SegmentList getTxPackage(String presentationID) throws PackageNotFoundException, MayamClientException;

	List<String> getDataFilesUrls(String materialAssetID) throws MayamClientException;

	MaterialExport getMaterialExport(String packageId, String filename) throws MayamClientException;

	AttributeMap getTitle(String titleId);

	/**
	 * Lists purge candidate task that perhaps should not exist
	 *
	 * @return
	 */
	List<AttributeMap> getSuspectPurgePendingList() throws MayamClientException;

	/**
	 * Cancels purge candidate tasks that perhaps should not exist (see getSuspectPurgePendingList)
	 */
	void cancelSuspectPurgeCandidates() throws MayamClientException;
}
