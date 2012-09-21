package com.mediasmiths.mayam;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.validation.MayamValidator;

public interface MayamClient {

	public void shutdown();

	/* titles */
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title);
	public MayamClientErrorCode createTitle(Title title); //may be called when marketing material arrives with no placeholder
	public MayamClientErrorCode updateTitle(Material.Title title);
	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title);
	public MayamClientErrorCode purgeTitle(PurgeTitle title);
	public boolean titleExists(String titleID) throws MayamClientException;
	
	/* material */
	public MayamClientErrorCode createMaterial(MaterialType material);
	public String createMaterial(String titleID, MarketingMaterialType material) throws MayamClientException;
	public MayamClientErrorCode updateMaterial(ProgrammeMaterialType material);
	public MayamClientErrorCode updateMaterial(MaterialType material);
	public MayamClientErrorCode deleteMaterial(DeleteMaterial deleteMaterial);
	public boolean materialExists(String materialID) throws MayamClientException;
	//returns true if the specified material has not had media\essence ingested
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
	
}