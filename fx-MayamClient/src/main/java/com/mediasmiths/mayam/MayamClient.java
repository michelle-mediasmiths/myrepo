package com.mediasmiths.mayam;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.mediasmiths.foxtel.generated.MediaExchange.Programme;

public interface MayamClient {

	public void shutdown();

	/* titles */
	public MayamClientErrorCode createTitle(Programme.Detail title);
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title);
	public MayamClientErrorCode updateTitle(Programme.Detail programme);
	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title);
	public MayamClientErrorCode purgeTitle(PurgeTitle title);
	public boolean titleExists(String titleID) throws MayamClientException;
	/* material */
	public MayamClientErrorCode createMaterial(Programme.Media media);
	public MayamClientErrorCode createMaterial(MaterialType material);
	public MayamClientErrorCode updateMaterial(Programme.Media media);
	public MayamClientErrorCode updateMaterial(MaterialType material);
	public boolean materialExists(String materialID) throws MayamClientException;

	/* packages */
	public MayamClientErrorCode createPackage(PackageType txPackage);
	public MayamClientErrorCode createPackage();
	public MayamClientErrorCode updatePackage(PackageType txPackage);
	public MayamClientErrorCode updatePackage();
	public MayamClientErrorCode purgePackage();

	public boolean isMaterialForPackageProtected(String packageID) throws MayamClientException;
	public boolean isTitleOrDescendentsProtected(String titleID) throws MayamClientException;


}