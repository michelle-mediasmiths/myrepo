package com.mediasmiths.foxtel.ibmshelper;

import java.util.List;

import au.com.foxtel.cf.mam.pms.PlaceholderMessage;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.AddorUpdateMaterial;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.CreateUpdatePackage;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.CreateUpdateTitleFunction;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.CompileMaterial;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.PurgePackage;
import com.mediasmiths.foxtel.ibmshelper.ibmsfunction.PurgeTitle;

public class IbmsDaoHelper
{
	public CreateUpdateTitleFunction createUpdateTitleFunction;
	public CreateUpdatePackage createUpdatePackage;
	public CompileMaterial compileMaterial;
	public AddorUpdateMaterial addorUpdateMaterial;
	public PurgePackage purgePackage;
	public PurgeTitle purgeTitle;

	@Inject
	public IbmsDaoHelper(
			CreateUpdateTitleFunction createUpdateTitleFunction,
			CreateUpdatePackage createUpdatePackage,
			AddorUpdateMaterial addorUpdateMaterial,
			CompileMaterial compileMaterial,
			PurgePackage purgePackage,
			PurgeTitle purgeTitle)
	{
		this.createUpdateTitleFunction = createUpdateTitleFunction;
		this.createUpdatePackage = createUpdatePackage;
		this.compileMaterial = compileMaterial;
		this.addorUpdateMaterial = addorUpdateMaterial;
		this.purgePackage = purgePackage;
		this.purgeTitle = purgeTitle;

	}

	public List<PlaceholderMessage> createUpdateTitle()
	{
		List<PlaceholderMessage> placeholderMessage = createUpdateTitleFunction.GetCreateUpdateTitle();

		return placeholderMessage;
	}

	public List<PlaceholderMessage> createUpdatePackage()
	{
		List<PlaceholderMessage> placeholderMessage = createUpdatePackage.GetCreateUpdatePackage();

		return placeholderMessage;
	}

	public List<PlaceholderMessage> getCompileMaterial()
	{
		List<PlaceholderMessage> placeholderMessage = compileMaterial.GetCompileMaterial();

		return placeholderMessage;
	}

	public List<PlaceholderMessage> addorUpdateMaterial()
	{
		List<PlaceholderMessage> placeholderMessage = addorUpdateMaterial.GetAddorUpdateMaterial();

		return placeholderMessage;
	}

	public List<PlaceholderMessage> purgePackage()
	{
		List<PlaceholderMessage> placeholderMessage = purgePackage.GetPurgePackage();

		return placeholderMessage;
	}

	public List<PlaceholderMessage> purgeTitle()
	{
		List<PlaceholderMessage> placeholderMessage = purgeTitle.GetPurgeTitle();

		return placeholderMessage;
	}

	public void updateRequestedStatus(String verionID, String requestID)
	{
	}

}
