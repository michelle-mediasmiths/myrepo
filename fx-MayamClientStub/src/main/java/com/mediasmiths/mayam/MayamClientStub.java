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
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
 
public class MayamClientStub implements MayamClient
{

	private final static Logger log = Logger.getLogger(MayamClientStub.class);

	public final static String NEW_TITLE_ID = "NEW_TITLE";
	public final static String EXISTING_TITLE_ID = "EXISTING_TITLE";
	public final static String EXISTING_AO_TITLE_ID = "EXISTING_AO_TITLE";
	public final static String ERROR_TITLE_ID = "ERROR_TITLE";
	public final static String PROTECTED_TITLE_ID = "PROTECTED_TITLE";

	public final static String NEW_MATERIAL_ID = "NEW_MATERIAL";
	public final static String EXISTING_MATERIAL_ID = "EXISTING_MATERIAL";
	public final static String PASSED_PREVIEW_MATERIAL_ID = "PASSED_PREVIEW";
	public final static String ERROR_MATERIAL_ID = "ERROR_MATERIAL";

	public final static String PLACEHOLDER_MATERIAL = "PLACEHOLDER_MATERIAL";

	public final static String NEW_PACKAGE_ID = "NEW_PACKAGE";
	public final static String EXISTING_PACKAGE_ID = "EXISTING_PACKAGE";
	public final static String ERROR_PACKAGE_ID = "ERROR_PACKAGE";
	public final static String PROTECTED_PACKAGE_ID = "PROTECTED_PACKAGE";

	private final static String [] CHANNELS = new String[] {"D3F","ARN","BIO","HIT","COM","CIN","FOX","HST","LHO","LST","LSY","FBO","AO","MEV","FKC","FOD","AED","SOH", "MO1", "MO2", "MO3", "MO5", "MO6", "MO7", "SHF", "SHH"};


	@Override
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title)
	{
		return createTitle(title.getTitleID());
	}

	@Override
	public MayamClientErrorCode createTitle(Title title)
	{
		return createTitle(title.getTitleID());
	}

	private MayamClientErrorCode createTitle(String titleID)
	{
		if (titleID.equals(NEW_TITLE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (titleID.equals(EXISTING_TITLE_ID) || titleID.equals(PROTECTED_TITLE_ID)|| titleID.equals(EXISTING_AO_TITLE_ID))
		{
			return MayamClientErrorCode.TITLE_CREATION_FAILED;
		}
		else if (titleID.equals(ERROR_TITLE_ID))
		{
			return MayamClientErrorCode.FAILURE;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public MayamClientErrorCode updateTitle(Title title)
	{
		return updateTitle(title.getTitleID());
	}

	@Override
	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title)
	{
		return updateTitle(title.getTitleID());
	}

	private MayamClientErrorCode updateTitle(String titleID)
	{

		if (titleID.equals(NEW_TITLE_ID))
		{
			return MayamClientErrorCode.TITLE_FIND_FAILED;
		}
		else if (titleID.equals(EXISTING_TITLE_ID) || titleID.equals(PROTECTED_TITLE_ID)|| titleID.equals(EXISTING_AO_TITLE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (titleID.equals(ERROR_TITLE_ID))
		{
			return MayamClientErrorCode.TASK_UPDATE_FAILED;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public MayamClientErrorCode purgeTitle(PurgeTitle title)
	{

		String titleID = title.getTitleID();

		if (titleID.equals(NEW_TITLE_ID))
		{
			return MayamClientErrorCode.TITLE_FIND_FAILED;
		}
		else if (titleID.equals(EXISTING_TITLE_ID) || titleID.equals(PROTECTED_TITLE_ID)|| titleID.equals(EXISTING_AO_TITLE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (titleID.equals(PROTECTED_TITLE_ID))
		{
			return MayamClientErrorCode.TITLE_DELETE_FAILED;
		}
		else if (titleID.equals(ERROR_TITLE_ID))
		{
			return MayamClientErrorCode.TITLE_DELETE_FAILED;
		}

		return MayamClientErrorCode.SUCCESS;

	}

	@Override
	public boolean titleExists(String titleID) throws MayamClientException
	{
		if (titleID.equals(NEW_TITLE_ID))
		{
			return false;
		}
		else if (titleID.equals(EXISTING_TITLE_ID) || titleID.equals(PROTECTED_TITLE_ID)|| titleID.equals(EXISTING_AO_TITLE_ID))
		{
			return true;
		}
		else if (titleID.equals(ERROR_TITLE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
		}

		return true;
	}

	private MayamClientErrorCode createMaterial(String materialID)
	{
		if (materialID.equals(NEW_MATERIAL_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (materialID.equals(EXISTING_MATERIAL_ID) || materialID.startsWith(PLACEHOLDER_MATERIAL))
		{
			return MayamClientErrorCode.MATERIAL_CREATION_FAILED;
		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			return MayamClientErrorCode.MATERIAL_CREATION_FAILED;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public MayamClientErrorCode createMaterial(MaterialType material, String titleID)
	{
		return createMaterial(material.getMaterialID());
	}

	@Override
	public String createMaterial(String titleID, MarketingMaterialType material, Material.Details details, Material.Title title) throws MayamClientException
	{

		if (titleID.equals(NEW_TITLE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
		}
		else if (titleID.equals(EXISTING_TITLE_ID) || titleID.equals(PROTECTED_TITLE_ID)|| titleID.equals(EXISTING_AO_TITLE_ID))
		{
			return EXISTING_MATERIAL_ID;
		}
		else if (titleID.equals(ERROR_TITLE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
		}

		return EXISTING_MATERIAL_ID;
	}

	private MayamClientErrorCode updateMaterial(String materialID)
	{
		if (materialID.equals(NEW_MATERIAL_ID))
		{
			return MayamClientErrorCode.MATERIAL_FIND_FAILED;
		}
		else if (materialID.equals(EXISTING_MATERIAL_ID) || materialID.startsWith(PLACEHOLDER_MATERIAL))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			return MayamClientErrorCode.MATERIAL_UPDATE_FAILED;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public boolean updateMaterial(ProgrammeMaterialType material, Material.Details details, Material.Title title) throws MayamClientException
	{
		updateMaterial(material.getMaterialID());
		return isMaterialPlaceholder(material.getMaterialID());
	}

	@Override
	public MayamClientErrorCode updateMaterial(MaterialType material)
	{
		return updateMaterial(material.getMaterialID());
	}

	@Override
	public MayamClientErrorCode deleteMaterial(DeleteMaterial deleteMaterial)
	{
		String materialID = deleteMaterial.getMaterial().getMaterialID();

		if (materialID.equals(NEW_MATERIAL_ID))
		{
			return MayamClientErrorCode.MATERIAL_FIND_FAILED;
		}
		else if (materialID.equals(EXISTING_MATERIAL_ID) || materialID.startsWith(PLACEHOLDER_MATERIAL))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			return MayamClientErrorCode.FAILURE;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public boolean materialExists(String materialID) throws MayamClientException
	{
		if (materialID.equals(NEW_MATERIAL_ID))
		{
			return false;
		}
		else if (materialID.equals(EXISTING_MATERIAL_ID) || materialID.startsWith(PLACEHOLDER_MATERIAL) || materialID.startsWith(PASSED_PREVIEW_MATERIAL_ID))
		{
			return true;
		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}

		return true;
	}

	@Override
	public boolean isMaterialPlaceholder(String materialID) throws MayamClientException
	{
		if (materialID.startsWith(PLACEHOLDER_MATERIAL))
		{
			return true;
		}
		else if (materialID.equals(NEW_MATERIAL_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}
		else if (materialID.equals(ERROR_MATERIAL_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.FAILURE);
		}

		return false;
	}

	private MayamClientErrorCode createPackage(String packageID)
	{
		if (packageID.equals(NEW_PACKAGE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (packageID.equals(EXISTING_PACKAGE_ID) || packageID.equals(PROTECTED_PACKAGE_ID))
		{
			return MayamClientErrorCode.PACKAGE_CREATION_FAILED;
		}
		else if (packageID.equals(ERROR_PACKAGE_ID))
		{
			return MayamClientErrorCode.FAILURE;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	private MayamClientErrorCode updatePackage(String packageID)
	{
		if (packageID.equals(NEW_PACKAGE_ID))
		{
			return MayamClientErrorCode.PACKAGE_FIND_FAILED;
		}
		else if (packageID.equals(EXISTING_PACKAGE_ID) || packageID.equals(PROTECTED_PACKAGE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (packageID.equals(ERROR_PACKAGE_ID))
		{
			return MayamClientErrorCode.FAILURE;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	@Override
	public MayamClientErrorCode createPackage(PackageType txPackage)
	{
		return createPackage(txPackage.getPresentationID());
	}

	@Override
	public MayamClientErrorCode updatePackage(PackageType txPackage)
	{
		return updatePackage(txPackage.getPresentationID());
	}


	@Override
	public MayamClientErrorCode deletePackage(DeletePackage deletePackage)
	{
		String packageID = deletePackage.getPackage().getPresentationID();

		if (packageID.equals(NEW_PACKAGE_ID))
		{
			return MayamClientErrorCode.PACKAGE_FIND_FAILED;
		}
		else if (packageID.equals(EXISTING_PACKAGE_ID) || packageID.equals(PROTECTED_PACKAGE_ID))
		{
			return MayamClientErrorCode.SUCCESS;
		}
		else if (packageID.equals(PROTECTED_PACKAGE_ID))
		{
			return MayamClientErrorCode.PACKAGE_DELETE_FAILED;
		}
		else if (packageID.equals(ERROR_PACKAGE_ID))
		{
			return MayamClientErrorCode.FAILURE;
		}

		return MayamClientErrorCode.SUCCESS;
	}

	public boolean packageExists(String presentationID) throws MayamClientException
	{
		if (presentationID.equals(NEW_PACKAGE_ID))
		{
			return false;
		}
		else if (presentationID.equals(EXISTING_PACKAGE_ID) || presentationID.equals(PROTECTED_PACKAGE_ID))
		{
			return true;
		}
		else if (presentationID.equals(ERROR_PACKAGE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.PACKAGE_FIND_FAILED);
		}

		return true;
	}

	@Override
	public boolean isTitleOrDescendentsProtected(String titleID) throws MayamClientException
	{
		if (titleID.equals(PROTECTED_TITLE_ID))
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	@Override
	public ArrayList<String> getChannelLicenseTagsForMaterial(String materialID) throws MayamClientException
	{
		if (materialExists(materialID))
		{
			ArrayList<String> list = new ArrayList<String>();
			list.add(CHANNELS[0]);
			return list;
		}
		else
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}

	}


	@Override
	public String pathToMaterial(String materialID, boolean acceptNonPreffered) throws MayamClientException
	{
		// TODO implement!
		
		return "/storage/mam/hires01/mediasmithstemp/input/TestMedia.mxf";
	}


	@Override
	public AttributeMap getOnlyTaskForAsset(MayamTaskListType type, String id) throws MayamClientException
	{
		log.info("returning empty attriburte map from mayam client stub");
		return new AttributeMap();
	}

	@Override
	public String getMaterialIDofPackageID(String packageID) throws MayamClientException
	{
		if (packageID.equals(ERROR_PACKAGE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.FAILURE);
		}
		else if (packageID.equals(NEW_PACKAGE_ID))
		{
			throw new MayamClientException(MayamClientErrorCode.PACKAGE_FIND_FAILED);
		}

		return EXISTING_MATERIAL_ID;
	}


	@Override
	public void updateMaterial(DetailType details, String materialID)
	{
		updateMaterial(materialID);
	}

	@Override
	public long createWFEErrorTaskNoAsset(String id, String title, String message) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public long createWFEErrorTaskNoAsset(String id, String title, String message, boolean isAO) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Programme getProgramme(String packageID) throws MayamClientException
	{
		//TODO bulk out stub
		return new Programme();
	}

	@Override
	public RuzzIF getRuzzProgramme(String packageID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean titleIsAO(String titleID) throws MayamClientException
	{
		if (EXISTING_AO_TITLE_ID.equals(titleID))
		{
			return true;
		}

		return false;
	}


	@Override
	public boolean isPackageAO(String packageID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void autoQcFailedForMaterial(String assetId, long taskID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void autoQcPassedForMaterial(String assetId, long taskID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void attachFileToMaterial(String materialID, String absolutePath, String serviceHandle) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public AttributeMap getMaterialAttributes(String materialID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeMap getPackageAttributes(String packageID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLastDeliveryVersionForMaterial(String materialID)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean materialHasPassedPreview(String materialID)
	{
		
		if(materialID.startsWith(PASSED_PREVIEW_MATERIAL_ID)){
			return true;
		}
		
		return false;
	}

	@Override
	public long createWFEErrorTaskForPackage(String packageID, String message)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long createWFEErrorTaskForMaterial(String materialID, String message)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long createWFEErrorTaskForTitle(String titleID, String message)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean attemptAutoMatch(String siteID, String fileName)
	{
		if(siteID.equals(PLACEHOLDER_MATERIAL)){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public boolean autoQcRequiredForTXTask(Long taskID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPackageSD(String packageID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void txDeliveryCompleted(String packageID, long taskID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void txDeliveryFailed(String packageID, long taskID, String stage) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}


	@Override
	public TaskState getTaskState(long taskID) throws MayamClientException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void exportCompleted(long taskID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exportFailed(long taskID, String reason) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addMaterialToPurgeCandidateList(String materialID, int daysUntilPurge) throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AttributeMap> getAllPurgeCandidatesPendingDeletion() throws MayamClientException
	{
		return new ArrayList<AttributeMap>();
	}

	@Override
	public boolean deletePurgeCandidates() throws MayamClientException
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public AttributeMap getTask(long taskId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNaturalBreaks(String materialID, String naturalBreaks) throws MayamClientException
	{
		if(materialID.equals(NEW_MATERIAL_ID)){
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}
		else if(materialID.equals(ERROR_MATERIAL_ID)){
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_UPDATE_FAILED);
		}
		
	}

	@Override
	public void ruzzQCMessagesDetected(String materialID) throws MayamClientException
	{
	}

	@Override
	public void autoQcErrorForMaterial(String assetId, long taskID)throws MayamClientException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getChannelGroupsForTitle(String titleID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getChannelGroupsForItem(AttributeMap itemAttributes) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getChannelGroupsForItem(String materialId) throws MayamClientException
	{
		Set<String> groups = new HashSet<String>();
		groups.add("GE");
		return groups;
	}


	@Override
	public Set<String> getChannelGroupsForPackage(final String packageId) throws MayamClientException
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}


	@Override
	public SegmentList getTxPackage(String presentationID, String materialID)
			throws PackageNotFoundException,
			MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SegmentList getTxPackage(String presentationID) throws PackageNotFoundException, MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTextualMetatadaForMaterialExport(String materialId) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getDataFilesUrls(String materialAssetID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public MaterialExport getMaterialExport(String packageId, String filename) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeMap getTitle(String titleId) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<AttributeMap> getSuspectPurgePendingList() throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void cancelSuspectPurgeCandidates() throws MayamClientException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public List<AttributeMap> getTasksInDateRange(Date start, Date end) {
		// TODO Auto-generated method stub
		return null;
	}
}
