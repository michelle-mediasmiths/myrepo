package com.mediasmiths.foxtel.ibmshelper;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;
import com.google.inject.Inject;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.AddorUpdateMaterialFunction;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.CompileMaterialFunction;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.CreateUpdatePackageFunction;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.CreateUpdateTitleFunction;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.PurgePackageFunction;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.PurgeTitleFunction;

import java.util.List;

public class IbmsDaoHelper
{
	public CreateUpdateTitleFunction createUpdateTitleFunction;
	public CreateUpdatePackageFunction createUpdatePackageFunction;
	public CompileMaterialFunction compileMaterialFunction;
	public AddorUpdateMaterialFunction addorUpdateMaterialFunction;
	public PurgePackageFunction purgePackageFunction;
	public PurgeTitleFunction purgeTitleFunction;

	@Inject
	public IbmsDaoHelper(
			CreateUpdateTitleFunction createUpdateTitleFunction,
			CreateUpdatePackageFunction createUpdatePackageFunction,
			AddorUpdateMaterialFunction addorUpdateMaterialFunction,
			CompileMaterialFunction compileMaterialFunction,
			PurgePackageFunction purgePackageFunction,
			PurgeTitleFunction purgeTitleFunction)
	{
		this.createUpdateTitleFunction = createUpdateTitleFunction;
		this.createUpdatePackageFunction = createUpdatePackageFunction;
		this.compileMaterialFunction = compileMaterialFunction;
		this.addorUpdateMaterialFunction = addorUpdateMaterialFunction;
		this.purgePackageFunction = purgePackageFunction;
		this.purgeTitleFunction = purgeTitleFunction;

	}

	public List<PlaceholderMessage> createUpdateTitle()
	{
		List<PlaceholderMessage> placeholderMessage = createUpdateTitleFunction.GetCreateUpdateTitle();

		return placeholderMessage;
	}

	public List<PlaceholderMessage> createUpdatePackage()
	{
		List<PlaceholderMessage> placeholderMessage = createUpdatePackageFunction.GetCreateUpdatePackage();

		return placeholderMessage;
	}

	public List<PlaceholderMessage> getCompileMaterial()
	{
		List<PlaceholderMessage> placeholderMessage = compileMaterialFunction.GetCompileMaterial();

		return placeholderMessage;
	}

	public List<PlaceholderMessage> addorUpdateMaterial()
	{
		List<PlaceholderMessage> placeholderMessage = addorUpdateMaterialFunction.GetAddorUpdateMaterial();

		return placeholderMessage;
	}

	public List<PlaceholderMessage> purgePackage()
	{
		List<PlaceholderMessage> placeholderMessage = purgePackageFunction.GetPurgePackage();

		return placeholderMessage;
	}

	public List<PlaceholderMessage> purgeTitle()
	{
		List<PlaceholderMessage> placeholderMessage = purgeTitleFunction.GetPurgeTitle();

		return placeholderMessage;
	}

	public void updateSuccessStatus(String verionID, String requestID)
	{
	}

	public void updateFailureStatus(String verionID, String requestID, String comment)
	{
	}

}
