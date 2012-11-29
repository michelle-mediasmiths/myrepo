package com.mediasmiths.mayam;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.CommentLog;
import com.mayam.wf.attributes.shared.type.GenericTable;
import com.mayam.wf.attributes.shared.type.GenericTable.Row;
import com.mayam.wf.attributes.shared.type.IdSet;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.controllers.MayamTitleController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.validation.MayamValidator;
import com.mediasmiths.mayam.validation.MayamValidatorImpl;

public class MayamClientImpl implements MayamClient
{

	private final static Logger log = Logger.getLogger(MayamClientImpl.class);

	@Named(MayamClientModule.SETUP_TASKS_CLIENT)
	@Inject
	TasksClient client;

	@Inject
	MayamTitleController titleController;
	@Inject
	MayamMaterialController materialController;
	@Inject
	MayamPackageController packageController;
	@Inject
	MayamTaskController tasksController;
	@Inject
	MayamValidator validator;

	@Inject
	public MayamClientImpl() throws MalformedURLException, IOException
	{

	}

	public MayamClientImpl(URL tasksURL, String mqModuleName, String userToken)
			throws MalformedURLException,
			IOException,
			DatatypeConfigurationException
	{

		tasksController = new MayamTaskController(client, new MayamAccessRightsController());
		titleController = new MayamTitleController(client);
		materialController = new MayamMaterialController(client);
		packageController = new MayamPackageController(client, new DateUtil());
		validator = new MayamValidatorImpl(client);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#shutdown()
	 */
	@Override
	public void shutdown()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#createTitle(au.com.foxtel.cf.mam.pms .CreateOrUpdateTitle)
	 */
	@Override
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title)
	{
		return titleController.createTitle(title);
	}

	@Override
	public MayamClientErrorCode createTitle(Title title)
	{
		return titleController.createTitle(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#updateTitle(com.mediasmiths.foxtel. generated.MediaExchange.Programme.Detail)
	 */
	@Override
	public MayamClientErrorCode updateTitle(Material.Title title)
	{
		return titleController.updateTitle(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#updateTitle(au.com.foxtel.cf.mam.pms .CreateOrUpdateTitle)
	 */
	@Override
	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title)
	{
		return titleController.updateTitle(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#purgeTitle(au.com.foxtel.cf.mam.pms .PurgeTitle)
	 */
	@Override
	public MayamClientErrorCode purgeTitle(PurgeTitle title)
	{
		return titleController.purgeTitle(title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#createMaterial(au.com.foxtel.cf.mam .pms.MaterialType)
	 */
	@Override
	public MayamClientErrorCode createMaterial(MaterialType material, String titleID)
	{
		return materialController.createMaterial(material, titleID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#updateMaterial(com.mediasmiths.foxtel .generated.MediaExchange.Programme.Media)
	 */
	@Override
	public MayamClientErrorCode updateMaterial(ProgrammeMaterialType material)
	{
		return materialController.updateMaterial(material);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#updateMaterial(au.com.foxtel.cf.mam .pms.MaterialType)
	 */
	@Override
	public MayamClientErrorCode updateMaterial(MaterialType material)
	{
		try
		{
			return materialController.updateMaterial(material);
		}
		catch (NullPointerException npe)
		{
			log.error("npe when updating material", npe);
			return MayamClientErrorCode.FAILURE;
		}

	}

	@Override
	public MayamClientErrorCode deleteMaterial(DeleteMaterial deleteMaterial)
	{
		return materialController.deleteMaterial(deleteMaterial.getMaterial().getMaterialID());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#createPackage(au.com.foxtel.cf.mam. pms.PackageType)
	 */
	@Override
	public MayamClientErrorCode createPackage(PackageType txPackage)
	{
		return packageController.createPackage(txPackage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#updatePackage(au.com.foxtel.cf.mam. pms.PackageType)
	 */
	@Override
	public MayamClientErrorCode updatePackage(PackageType txPackage)
	{
		return packageController.updatePackage(txPackage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#updatePackage()
	 */
	@Override
	public MayamClientErrorCode updatePackage(ProgrammeMaterialType.Presentation.Package txPackage)
	{
		return packageController.updatePackage(txPackage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mediasmiths.mayam.MayamClient#purgePackage()
	 */
	@Override
	public MayamClientErrorCode deletePackage(DeletePackage deletePackage)
	{
		return packageController.deletePackage(deletePackage.getPackage().getPresentationID());
	}

	/**
	 * Returns true if a title with the specified ID exists, otherwise false
	 */
	@Override
	public boolean titleExists(String titleID)
	{
		return titleController.titleExists(titleID);
	}

	@Override
	public boolean materialExists(String materialID) throws MayamClientException
	{
		return materialController.materialExists(materialID);
	}

	@Override
	public boolean isMaterialForPackageProtected(String packageID) throws MayamClientException
	{
		boolean isProtected = true;
		AttributeMap packageAttributes = packageController.getPackageAttributes(packageID);
		if (packageAttributes != null)
		{
			String materialID = packageAttributes.getAttribute(Attribute.ASSET_PARENT_ID);
			AttributeMap materialAttributes = materialController.getMaterialAttributes(materialID);
			if (materialAttributes != null) {
				isProtected = materialAttributes.getAttribute(Attribute.AUX_FLAG);
			}
		}
		return isProtected;
	}

	@Override
	public boolean isTitleOrDescendentsProtected(String titleID) throws MayamClientException
	{
		Boolean isProtected = false;
		AttributeMap titleAttributes = titleController.getTitle(titleID);

		if (titleAttributes != null && titleAttributes.containsAttribute(Attribute.AUX_FLAG))
		{
			isProtected = titleAttributes.getAttribute(Attribute.AUX_FLAG);

			if (isProtected)
			{
				try
				{
					List<AttributeMap> materials = client.assetApi().getAssetChildren(
							MayamAssetType.TITLE.getAssetType(),
							titleID,
							MayamAssetType.MATERIAL.getAssetType());
					
					for (int i = 0; i < materials.size(); i++)
					{
						AttributeMap materialAttributes = materials.get(i);
						if (materialAttributes.getAttribute(Attribute.AUX_FLAG))
						{
							isProtected = true;
							break;
						}
					}

					List<AttributeMap> packages = client.assetApi().getAssetChildren(
							MayamAssetType.TITLE.getAssetType(),
							titleID,
							MayamAssetType.PACKAGE.getAssetType());
					
					for (int i = 0; i < packages.size(); i++)
					{
						AttributeMap packageAttributes = packages.get(i);
						if (packageAttributes.getAttribute(Attribute.AUX_FLAG))
						{
							isProtected = true;
							break;
						}
					}

				}
				catch (RemoteException e)
				{
					log.error("Exception thrown by Mayam while checking protection flag on descendants of title : " + titleID);
					e.printStackTrace();
				}
			}
		}

		return isProtected;
	}

	@Override
	public boolean packageExists(String presentationID) throws MayamClientException
	{
		return packageController.packageExists(presentationID);
	}

	@Override
	public boolean isMaterialPlaceholder(String materialID)
	{
		boolean isPlaceholder = true;
		AttributeMap materialAttributes = materialController.getMaterialAttributes(materialID);

		if (materialAttributes != null && materialAttributes.containsAttribute(Attribute.SOURCE_IDS))
		{
			IdSet sourceIds = materialAttributes.getAttribute(Attribute.SOURCE_IDS);
			if (sourceIds != null)
			{
				isPlaceholder = false;
			}
			else
			{
				// TODO: Need to check segment data once implemented in order to
				// determind if placeholder
			}
		}
		return isPlaceholder;
	}

	/**
	 * creates an item in viz ardome for associated media files.
	 * 
	 * @return the master ID of the created item
	 */
	@Override
	public String createMaterial(String titleID, MarketingMaterialType material) throws MayamClientException
	{
		AttributeMap titleAttributes = titleController.getTitle(titleID);

		if (titleAttributes == null)
		{
			throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
		}

		// the id of the created material
		String materialID = materialController.createMaterial(material);
		AttributeMap materialAttributes = materialController.getMaterialAttributes(materialID);

		if (materialAttributes == null)
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}


		 materialAttributes.setAttribute(Attribute.ASSET_PARENT_ID, titleID);
		try
		{
			client.assetApi().updateAsset(materialAttributes);
		}
		catch (RemoteException e)
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_UPDATE_FAILED);
		}

		return materialID;
	}

	public MayamValidator getValidator()
	{
		return validator;
	}

	public ArrayList<String> getChannelLicenseTagsForMaterial(String materialID) throws MayamClientException
	{
		ArrayList<String> licenseTags = new ArrayList<String>();
		AttributeMap material = null;
		try
		{
			material = client.assetApi().getAssetBySiteId(AssetType.valueOf(MayamAssetType.MATERIAL.getText()), materialID);
		}
		catch (RemoteException e)
		{
			licenseTags = null;
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}
		if (material != null)
		{
			String parentID = material.getAttribute(Attribute.PARENT_HOUSE_ID);
			AttributeMap title = null;

			try
			{
				title = client.assetApi().getAssetBySiteId(AssetType.valueOf(MayamAssetType.TITLE.getText()), parentID);
			}
			catch (RemoteException e)
			{
				licenseTags = null;
				throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
			}

			if (title != null)
			{
				GenericTable mediaRights = title.getAttribute(Attribute.MEDIA_RIGHTS);
				if (mediaRights != null) {
					List<Row> rows = mediaRights.getRows();
					if (rows != null) {
						for (int i = 0; i < rows.size(); i++) 
						{
							String channelTag = rows.get(i).get(5);
							licenseTags.add(channelTag);
						}
					}
				}
			}
		}
		return licenseTags;
	}

	@Override
	public PackageType getPackage(String packageID) throws MayamClientException
	{
		return packageController.getPackage(packageID);
	}

	@Override
	public String pathToMaterial(String materialID) throws MayamClientException
	{
		// TODO implelment!
		
		return "/path/to/file";
	}

	@Override
	public MaterialType getMaterial(String materialID) throws MayamClientException
	{
		return materialController.getPHMaterialType(materialID);
	}

	@Override
	public AttributeMap getTaskForAsset(MayamTaskListType type, String id) throws MayamClientException
	{
		return tasksController.getTaskForAsset(type, id);
	}

	@Override
	public void saveTask(AttributeMap task) throws MayamClientException
	{
		tasksController.saveTask(task);
	}

	@Override
	public void failTaskForAsset(MayamTaskListType type, String id) throws MayamClientException
	{

		log.info(String.format("Failing task of type %s for asset %s", type.getText(), id));

		AttributeMap task = this.getTaskForAsset(type, id);
		task.setAttribute(Attribute.TASK_STATE, TaskState.REJECTED);
		this.saveTask(task);
	}

	@Override
	public Package getPresentationPackage(String packageID) throws MayamClientException
	{
		return packageController.getPresentationPackage(packageID);
	}

	@Override
	public ProgrammeMaterialType getProgrammeMaterialType(String materialID)
	{
		return materialController.getProgrammeMaterial(materialID);
	}

	@Override
	public String getMaterialIDofPackageID(String packageID) throws MayamClientException
	{
		return packageController.getPackageAttributes(packageID).getAttribute(Attribute.ASSET_PARENT_ID);
	}

	@Override
	public void createTxDeliveryFailureTask(String packageID, String failureReason) throws MayamClientException
	{
		long id = tasksController.createTask(packageID, MayamAssetType.PACKAGE, MayamTaskListType.TX_DELIVERY_FAILURE);
		AttributeMap newTask;
		try {
			newTask = tasksController.getTask(id);
			CommentLog comments = newTask.getAttribute(Attribute.COMMENT_LOG);
			comments.addComment("WFE TxDelivery", new Date(), failureReason);
			newTask.setAttribute(Attribute.COMMENT_LOG, comments);
			tasksController.saveTask(newTask);
		} catch (RemoteException e) {
			log.error("Exception thrown by Mayam while creating Tx Deliver Failure task for package :" + packageID);
			e.printStackTrace();
		}
	}

	@Override
	public Title getTitle(String titleID, boolean includeVersionInfo) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitleOfPackage(String packageID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Details getSupplierDetails(String materialID) throws MayamClientException
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	protected void uploadReport(String id, String handle, String filepath)
	{
		try {
			client.assetApi().importFile(AssetType.ITEM, id, handle, filepath);
		} catch (RemoteException e) {
			log.error("EXception thrown by Mayam while attching report: " + id);
			e.printStackTrace();
		}
	}
}
