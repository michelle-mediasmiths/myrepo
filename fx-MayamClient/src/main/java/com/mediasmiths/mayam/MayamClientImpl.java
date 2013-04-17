package com.mediasmiths.mayam;

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
import com.mayam.wf.attributes.shared.type.FilterCriteria;
import com.mayam.wf.attributes.shared.type.FilterCriteria.SortOrder;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.ws.client.FilterResult;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.foxtel.channels.config.ChannelProperties;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.outputruzz.RuzzIF;
import com.mediasmiths.foxtel.generated.ruzz.DetailType;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.controllers.MayamTitleController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.util.AssetProperties;
import com.mediasmiths.mayam.util.MediaExchangeProgrammeOutputBuilder;
import com.mediasmiths.mayam.util.RuzzProgrammeOutputBuilder;
import com.mediasmiths.mayam.validation.MayamValidator;
import com.mediasmiths.mayam.validation.MayamValidatorImpl;
import org.apache.log4j.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
	protected ChannelProperties channelProperties;

	@Inject
	MediaExchangeProgrammeOutputBuilder mediaExchangeBuilder;


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
		materialController = new MayamMaterialController(client, tasksController);
		packageController = new MayamPackageController(client, new DateUtil(),materialController, tasksController);
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
		if (title == null)
		{
			return MayamClientErrorCode.TITLE_UNAVAILABLE;
		}
		else
		{
			return titleController.purgeTitle(title.getTitleID(),0);
		}
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
	public boolean updateMaterial(ProgrammeMaterialType material, Material.Details details, Material.Title title) throws MayamClientException
	{
		return materialController.updateMaterial(material,details,title);
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
	public AttributeMap getMaterialAttributes(String materialID) throws MayamClientException{
		return materialController.getMaterialAttributes(materialID);
	}
	
	@Override
	public MayamClientErrorCode deleteMaterial(DeleteMaterial deleteMaterial)
	{
		return materialController.deleteMaterial(deleteMaterial.getMaterial().getMaterialID(),0);
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
		return packageController.deletePackage(deletePackage.getPackage().getPresentationID(),0);
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
	public boolean isTitleOrDescendentsProtected(String titleID) throws MayamClientException
	{
		Boolean isProtected = false;
		AttributeMap titleAttributes = titleController.getTitle(titleID);

		if (titleAttributes != null && titleAttributes.containsAttribute(Attribute.PURGE_PROTECTED))
		{
			isProtected = ((Boolean) titleAttributes.getAttribute(Attribute.PURGE_PROTECTED)).booleanValue();

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
						if (materialAttributes != null && materialAttributes.containsAttribute(Attribute.PURGE_PROTECTED))
						{
							if (((Boolean) materialAttributes.getAttribute(Attribute.PURGE_PROTECTED)).booleanValue())
							{
								isProtected = true;
								break;
							}
						}
					}
					
				}
				catch (RemoteException e)
				{
					log.error("Exception thrown by Mayam while checking protection flag on descendants of title : " + titleID,e);
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
		AttributeMap materialAttributes = materialController.getMaterialAttributes(materialID);
		return AssetProperties.isMaterialPlaceholder(materialAttributes);		
	}

	/**
	 * creates an item in viz ardome for associated media files.
	 * 
	 * @return the site ID of the created item
	 */
	@Override
	public String createMaterial(String titleID, MarketingMaterialType material, Material.Details details, Material.Title title) throws MayamClientException
	{
		// not strictitly a master\materialid but a site id generated by viz ardome for the item
		String siteid = materialController.createMaterial(material, titleID, details, title);

		return siteid;
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
				StringList channels = title.getAttribute(Attribute.CHANNELS);
				if (channels != null)
				{
					for (int i = 0; i < channels.size(); i++)
					{
						licenseTags.add(channels.get(i));
					}
				}
			}
		}
		return licenseTags;
	}

	@Override
	public String pathToMaterial(String materialID, boolean acceptNonPreferredLocations) throws MayamClientException
	{
		log.debug(String.format("Requesting pathTo material for materialID %s", materialID));
		
		String assetPath = null;
		AttributeMap material = materialController.getMaterialAttributes(materialID);
		
		if(null != material)
		{
			String assetID = material.getAttribute(Attribute.ASSET_ID);
			assetPath = materialController.getAssetPath(assetID,acceptNonPreferredLocations);
		}
		else
		{
			log.error(String.format("No attribute map was returned for materialID %s; unable to retrieve asset path.", materialID));
			throw new MayamClientException(MayamClientErrorCode.FAILURE);
		}
		
		log.debug(String.format("Asset path returned %s for materialID %s.", assetPath, materialID));
		return assetPath;
	}

	@Override
	public AttributeMap getOnlyTaskForAsset(MayamTaskListType type, String id) throws MayamClientException
	{
		return tasksController.getOnlyTaskForAssetBySiteID(type, id);
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

		AttributeMap task = this.getOnlyTaskForAsset(type, id);
		task.setAttribute(Attribute.TASK_STATE, TaskState.REJECTED);
		this.saveTask(task);
	}

	@Override
	public Programme getProgramme(String packageID) throws MayamClientException
	{

		//fetch the packages information		
		FullProgrammePackageInfo pack = new FullProgrammePackageInfo(packageID, packageController, materialController, titleController, client.assetApi());
		//build the Programme Object
		return mediaExchangeBuilder.buildProgramme(pack);
	}
	
	@Override
	public RuzzIF getRuzzProgramme(String packageID) throws MayamClientException
	{
		//fetch the packages information		
		FullProgrammePackageInfo pack = new FullProgrammePackageInfo(packageID, packageController, materialController, titleController, client.assetApi());
		return RuzzProgrammeOutputBuilder.buildProgramme(pack, titleController);
				
	}
	

	@Override
	public String getMaterialIDofPackageID(String packageID) throws MayamClientException
	{
		return packageController.getSegmentList(packageID).getAttributeMap().getAttributeAsString(Attribute.PARENT_HOUSE_ID);
	}

	@Override
	public void createTxDeliveryFailureTask(String packageID, String failureReason) throws MayamClientException
	{
		long id = tasksController.createTask(packageID, MayamAssetType.PACKAGE, MayamTaskListType.TX_DELIVERY);
		AttributeMap newTask;
		try
		{
			newTask = tasksController.getTask(id);
			CommentLog comments = newTask.getAttribute(Attribute.COMMENT_LOG);
			comments.addComment("WFE TxDelivery", new Date(), failureReason);
			newTask.setAttribute(Attribute.COMMENT_LOG, comments);
			tasksController.saveTask(newTask);
		}
		catch (RemoteException e)
		{
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
		try
		{
			client.assetApi().importFile(AssetType.ITEM, id, handle, filepath);
		}
		catch (RemoteException e)
		{
			log.error("EXception thrown by Mayam while attching report: " + id,e);
			e.printStackTrace();
		}
	}

	@Override
	public void updateMaterial(DetailType details, String materialID) throws MayamClientException
	{
		String title = details.getTitle();
		String som = details.getSOM();
		String duration = details.getDuration();
		String format = details.getFormat();

		if (log.isDebugEnabled())
		{
			log.debug(String.format(
					"material {%s} title {%s} som {%s} duration {%s} format {%s}",
					materialID,
					title,
					som,
					duration,
					format));
		}

		materialController.updateMaterial(details,materialID);
	}

	@Override
	public long createWFEErrorTaskNoAsset(String id, String title, String message) throws MayamClientException
	{
		return tasksController.createWFEErrorTaskNoAsset(id, title, message);
	}

	@Override
	public boolean isTitleAO(String titleID) throws MayamClientException
	{

		boolean isAO = false;
		AttributeMap title = titleController.getTitle(titleID);
		if (title != null)
		{
			Boolean adult = title.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL);
	
			if (adult != null)
			{
				isAO = adult.booleanValue();
			}
			else
			{
				log.error("CONT_RESTRICTED_MATERIAL attribute missing from title " + titleID);
				throw new MayamClientException(MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES);
			}
		}
		return isAO;
	}

	@Override
	public boolean isPackageAO(String packageID) throws MayamClientException
	{
		FullProgrammePackageInfo info = new FullProgrammePackageInfo(packageID, packageController, materialController, titleController,
		                                                             client.assetApi());
		
		return AssetProperties.isAO(info.getTitleAttributes());
	}

	@Override
	public void autoQcFailedForMaterial(String materialId, long taskID) throws MayamClientException
	{
		tasksController.autoQcFailedForMaterial(materialId,taskID);
	}

	@Override
	public void autoQcPassedForMaterial(String materialId, long taskID) throws MayamClientException
	{
		tasksController.autoQcPassedForMaterial(materialId,taskID);
	}

	@Override
	public void attachFileToMaterial(final String houseID, final String absolutePath, final String serviceHandle) throws MayamClientException
	{
		log.info(String.format("Attatching file {%s} to material {%s}", absolutePath, houseID));
		AttributeMap materialAttributes = materialController.getMaterialAttributes(houseID);
		
		if (null == materialAttributes)
		{
			log.debug(String.format("Material attributes returned null for id %s; " +
					"trying to get attributes using parent house id from segments.", houseID));
			try
			{
				SegmentList segList = client.segmentApi().getSegmentListBySiteId(houseID);
				
				if(null != segList)
				{
					log.debug("SegmentList not null; getting material attributes using parent house id");
					String materialID = segList.getAttributeMap().getAttribute(Attribute.PARENT_HOUSE_ID);
					materialAttributes = materialController.getMaterialAttributes(materialID);
					
					if(null == materialAttributes)
					{
						log.error("Unable to retrieve material attributes from either material nor segments.");
						throw new MayamClientException(MayamClientErrorCode.IMPORT_FILE_FAILED);
					}
				}
				else
				{
					log.error(String.format("Segment list returned null for asset with id %s", houseID));
					throw new MayamClientException(MayamClientErrorCode.IMPORT_FILE_FAILED);
				}
			}
			catch (RemoteException e)
			{
				log.error("Error attatching file to material " + houseID, e);
				throw new MayamClientException(MayamClientErrorCode.IMPORT_FILE_FAILED, e);
			}
		}
		
		final String assetID = materialAttributes.getAttributeAsString(Attribute.ASSET_ID);
		try
		{
			client.assetApi().importFile(AssetType.ITEM, assetID, serviceHandle, absolutePath);
		}
		catch (RemoteException e)
		{
			log.error("Error attatching file to material " + houseID, e);
			throw new MayamClientException(MayamClientErrorCode.IMPORT_FILE_FAILED, e);
		}
	}

	/**
	 * only ever returns attributes for 'real'tx packages, not 'pending' oones
	 */
	@Override
	public AttributeMap getPackageAttributes(String packageID) throws MayamClientException
	{
		return packageController.getSegmentList(packageID).getAttributeMap();
	}
	
	/**
	 * returned package is 'real' or 'pending' depending on the materialid passed in
	 */
	@Override
	public SegmentList getTxPackage(String presentationID, String materialID) throws PackageNotFoundException, MayamClientException{
		return packageController.getTxPackage(presentationID, materialID);
	}
	
	@Override
	public SegmentList getTxPackage(String presentationID) throws PackageNotFoundException, MayamClientException
	{

		log.debug("looking for tx package " + presentationID);

		SegmentList segmentList = null;

		try
		{
			segmentList = packageController.getSegmentList(presentationID);

		}
		catch (PackageNotFoundException pnfe)
		{
			log.debug(String.format("Real tx package with id %s not found, will look for a pending one", presentationID));
		}

		if (segmentList != null)
		{
			return segmentList;
		}
		else
		{	
			return packageController.getPendingTxPackage(presentationID, null);
		}
	}

	@Override
	public int getLastDeliveryVersionForMaterial(String materialID)
	{
		try
		{
			AttributeMap materialAttributes = materialController.getMaterialAttributes(materialID);
			Integer deliveryVersion = materialAttributes.getAttribute(Attribute.VERSION_NUMBER);

			if (deliveryVersion == null)
			{
				return -1;
			}
			else
			{
				return deliveryVersion.intValue();
			}
		}
		catch (Exception e)
		{
			log.error("error determining delivery version for material", e);
			return -1;
		}

	}

	@Override
	public boolean materialHasPassedPreview(String materialID)
	{
		AttributeMap materialAttributes = materialController.getMaterialAttributes(materialID);
		if (materialAttributes != null)
		{
			return MayamPreviewResults.isPreviewPass((String) materialAttributes.getAttribute(Attribute.QC_PREVIEW_RESULT));
		}
		else
		{
			return false;
		}
	}
	
	private long createWFEErrorTaskForAsset(MayamAssetType type, String siteId, String message)
	{
		try
		{
			return tasksController.createWFEErorTask(type, siteId, message);
		}
		catch (MayamClientException e)
		{
			log.error("Error creating wfe error task for asset with siteid" + siteId);
			return -1;
		}
	}

	@Override
	public long createWFEErrorTaskForUnmatched(String aggregator, String filename)
	{
		long taskId = -1;
		
		AttributeMap task = client.createAttributeMap();
		task.setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.WFE_ERROR.getText());
		task.setAttribute(Attribute.ASSET_TYPE, MayamAssetType.MATERIAL.getAssetType());
		
		task.setAttribute(Attribute.ASSET_TITLE, filename);
		task.setAttribute(Attribute.AGGREGATOR, aggregator);
		task.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
		
		try {
			AttributeMap newTask = client.taskApi().createTask(task);
			taskId = newTask.getAttribute(Attribute.TASK_ID);
		} 
		catch (RemoteException e) {
			log.error("Exception while creating WFE Error task to store Unmatched Aggregator", e);
			e.printStackTrace();
		}
		return taskId;
	}
	
	@Override
	public long createWFEErrorTaskForPackage(String packageID, String message)
	{
		return createWFEErrorTaskForAsset(MayamAssetType.PACKAGE, packageID,message);
	}

	@Override
	public long createWFEErrorTaskForMaterial(String materialID, String message)
	{
		return createWFEErrorTaskForAsset(MayamAssetType.MATERIAL, materialID,message);
	}

	@Override
	public long createWFEErrorTaskForTitle(String titleID, String message)
	{
		return createWFEErrorTaskForAsset(MayamAssetType.TITLE, titleID,message);
	}

	@Override
	public boolean attemptAutoMatch(String siteID, String fileName)
	{

		log.debug("searching for unmatched tasks for filename " + fileName);

		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.UNMATCHED_MEDIA.getText());
		criteria.getFilterEqualities().setAttribute(Attribute.SERIES_TITLE, fileName);
		criteria.getSortOrders().add(new SortOrder(Attribute.TASK_CREATED, SortOrder.Direction.DESC));
		FilterResult result;
		try
		{
			result = client.taskApi().getTasks(criteria, 10, 0);
		}
		catch (RemoteException e1)
		{
			log.warn("error searching for unmatched tasks with filename"+fileName);
			return false;
		}
		
		log.info("Total matches: " + result.getTotalMatches());

		if (result.getTotalMatches() > 1)
		{
			log.warn("more than one unmatched task for " + fileName);
		}
		else if (result.getTotalMatches() == 1)
		{
			AttributeMap unmatchedTask = result.getMatches().get(0);

			// get item we are going to try to automatch to
			AttributeMap materialAttributes = materialController.getMaterialAttributes(siteID);
			String assetID = materialAttributes.getAttributeAsString(Attribute.ASSET_ID);
			//perform match
			unmatchedTask.setAttribute(Attribute.ASSET_PEER_ID, assetID);
			try
			{
				tasksController.saveTask(unmatchedTask);
				return true;
			}
			catch (MayamClientException e)
			{
					log.error("error automatching to asset "+siteID);
			}

		}
		else{
			log.info("no candidates for automatch found");
		}

		return false;
	}

	@Override
	public boolean autoQcRequiredForTXTask(Long taskID) throws MayamClientException
	{
		AttributeMap task;
		try
		{
			task = tasksController.getTask(taskID.longValue());
		}
		catch (RemoteException e)
		{
			log.error("Error fetching task with id " + taskID,e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED,e);
		}
		
		Boolean qcRequired = task.getAttribute(Attribute.QC_REQUIRED);
		
		if(qcRequired==null){
			return false;
		}
		else{
			return qcRequired.booleanValue();
		}
		
	}

	@Override
	public boolean isPackageSD(String packageID) throws MayamClientException
	{
		SegmentList segmentList = packageController.getSegmentList(packageID);
		return AssetProperties.isPackageSD(segmentList.getAttributeMap());
	}

	@Override
	public void txDeliveryCompleted(String packageID, long taskID) throws MayamClientException
	{
		try
		{
			AttributeMap txTask = tasksController.getTask(taskID);
			if (txTask != null)
			{
				txTask.setAttribute(Attribute.TX_DELIVER, Boolean.TRUE);
				txTask.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
				tasksController.saveTask(txTask);
			}
			else
			{
				log.error("Failed to fetch tx delivery task with id " + taskID);
				throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
			}
		}
		catch (RemoteException e)
		{
			log.error("Failed to fetch tx delivery task with id " + taskID, e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED, e);
		}
	}

	@Override
	public void txDeliveryFailed(final String packageID, final long taskID, final String stage) throws MayamClientException
	{
		log.debug(String.format("TX delivery failed for packageId %s, taskId %s, stage %s. Setting task to ERROR", packageID, taskID, stage));
		try
		{
			AttributeMap txTask = tasksController.getTask(taskID);
			if (txTask != null)
			{
				txTask.setAttribute(Attribute.TX_DELIVER, Boolean.FALSE);
				txTask.setAttribute(Attribute.ERROR_MSG, String.format("TX Delivery Failed : %s",stage));
				txTask.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
				tasksController.saveTask(txTask);
			}
			else
			{
				log.error("Failed to fetch tx delivery task with id " + taskID);
				throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
			}
		}
		catch (RemoteException e)
		{
			log.error("Failed to fetch tx delivery task with id " + taskID, e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED, e);
		}		
	}

	@Override
	public TaskState getTaskState(long taskid) throws MayamClientException
	{
		AttributeMap task;
		try
		{
			task = tasksController.getTask(taskid);
		}
		catch (RemoteException e)
		{
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED,e);
		}
		
		if(task != null){
			return (TaskState) task.getAttribute(Attribute.TASK_STATE);
		}
		
		throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED);
	}

	@Override
	public boolean titleIsAO(String titleID) throws MayamClientException
	{
		boolean isAO = false;
		
		AttributeMap title = titleController.getTitle(titleID);
		if (title != null)
		{
			isAO = AssetProperties.isAO(title);
		}
		
		return isAO;
	}

	@Override
	public void exportCompleted(long taskID) throws MayamClientException
	{
		AttributeMap task;
		try
		{
			task = tasksController.getTask(taskID);
		}
		catch (RemoteException e)
		{
			log.error("error fetching task "+taskID,e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED,e);
		}
		
		AttributeMap update = tasksController.updateMapForTask(task);
		update.setAttribute(Attribute.TASK_STATE, TaskState.FINISHED);
		tasksController.saveTask(update);
	}
	
	@Override
	public void exportFailed(long taskID) throws MayamClientException
	{
		AttributeMap task;
		try
		{
			task = tasksController.getTask(taskID);
		}
		catch (RemoteException e)
		{
			log.error("error fetching task "+taskID,e);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED,e);
		}
		
		AttributeMap update = tasksController.updateMapForTask(task);
		update.setAttribute(Attribute.TASK_STATE, TaskState.ERROR);
		tasksController.saveTask(update);
	}

	@Override
	public void addMaterialToPurgeCandidateList(String materialID, int daysUntilPurge) throws MayamClientException
	{
		tasksController.createOrUpdatePurgeCandidateTaskForAsset(MayamAssetType.MATERIAL, materialID, daysUntilPurge);
	}

	@Override
	public List<AttributeMap> getAllPurgeCandidatesPendingDeletion() throws MayamClientException
	{
		final FilterCriteria criteria = client.taskApi().createFilterCriteria();
		criteria.getFilterEqualities().setAttribute(Attribute.TASK_LIST_ID, MayamTaskListType.PURGE_CANDIDATE_LIST.getText());
		criteria.getFilterAlternatives().addAsExclusion(Attribute.TASK_STATE, TaskState.REMOVED);
		criteria.getFilterRanges().setAttributeRange(Attribute.OP_DATE, new Date(0), new Date()); //find all with OP_DATE between start of epoch and now
		criteria.getSortOrders().add(new SortOrder(Attribute.OP_DATE, SortOrder.Direction.ASC));
		FilterResult result;
		try
		{
			result = client.taskApi().getTasks(criteria, 1000, 0);			
			return result.getMatches();
		}
		catch (RemoteException e1)
		{
			log.error("error searching for purge candidates",e1);
			throw new MayamClientException(MayamClientErrorCode.TASK_SEARCH_FAILED,e1);
		}
	}

	@Override
	public boolean deletePurgeCandidates() throws MayamClientException
	{
		List<AttributeMap> allPurgeCandidatesPendingDeletion = getAllPurgeCandidatesPendingDeletion();

		log.info(String.format("%d purge candidates due for deletion",allPurgeCandidatesPendingDeletion.size()));
		
		boolean allsuccess = true;

		for (AttributeMap asset : allPurgeCandidatesPendingDeletion)
		{
			try
			{
				AssetType assetType = asset.getAttribute(Attribute.ASSET_TYPE);
				String houseID = asset.getAttributeAsString(Attribute.HOUSE_ID);

				if(houseID==null){
					log.info(String.format("house id null for asset %s", asset.getAttribute(Attribute.ASSET_ID)));
				}
				else
				if (assetType.equals(MayamAssetType.TITLE.getAssetType()))
				{
					log.debug("purge title " + houseID);
					titleController.purgeTitle(houseID,0);
				}
				else if (assetType.equals(MayamAssetType.MATERIAL.getAssetType()))
				{
					log.debug("delete item " + houseID);
					MayamClientErrorCode deleteMaterial = materialController.deleteMaterial(houseID,0);
					if (deleteMaterial == MayamClientErrorCode.MATERIAL_FIND_FAILED)
					{
						log.info("failed to find material for deletion, it may have already been deleted, closing purge candidate task");
						AttributeMap updateMap = tasksController.updateMapForTask(asset);
						updateMap.setAttribute(Attribute.TASK_STATE, TaskState.REMOVED);
						tasksController.saveTask(updateMap);
					}
				}
				else if (assetType.equals(MayamAssetType.PACKAGE.getAssetType()))
				{
					log.debug("delete package " + houseID);
					packageController.deletePackage(houseID,0);
				}
				else
				{
					log.info(String.format("Unknown asset type %s wont attempt delete", assetType));
				}
			}
			catch (Exception e)
			{
				log.error("Error deleting asset", e);
				allsuccess = false;
			}

		}
		return allsuccess;
	}
	
	@Override
	public AttributeMap getTask(long taskId)
	{
		AttributeMap returnMap = null;
		try{
			returnMap = tasksController.getTask(taskId);
		}
		catch(RemoteException e)
		{
			log.error("Exception thrown by Mayam while retrieving task : " + taskId, e);
		}
		return returnMap;
	}

	@Override
	public void setNaturalBreaks(String materialID, String naturalBreaks) throws MayamClientException
	{
		materialController.setNaturalBreaks(materialID,naturalBreaks);
	}

	@Override
	public void requireAutoQCForMaterial(String materialID) throws MayamClientException
	{
		AttributeMap materialAttributes = materialController.getMaterialAttributes(materialID);
		AttributeMap updateMapForAsset = tasksController.updateMapForAsset(materialAttributes);
		updateMapForAsset.setAttribute(Attribute.QC_REQUIRED, Boolean.TRUE);
		try
		{
			client.assetApi().updateAsset(updateMapForAsset);
		}
		catch (RemoteException e)
		{
			log.error("error updating qc required attribute on material " + materialID, e);
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_UPDATE_FAILED, e);
		}

	}

	@Override
	public void autoQcErrorForMaterial(String assetId, long taskID) throws MayamClientException
	{
		tasksController.autoQcErrorForMaterial(assetId,taskID);
	}

	@Override
	public Set<String> getChannelGroupsForTitle(String titleId) throws MayamClientException
	{
		if (titleId == null)
		{
			log.warn("null titleid passed to getChannelGroupsForTitle");
			return Collections.<String> emptySet();
		}
		else

		{
			AttributeMap title = titleController.getTitle(titleId);
			StringList channels = title.getAttribute(Attribute.CHANNELS);

			return channelProperties.groupsForChannels(channels);
		}

	}
	
	@Override
	public Set<String> getChannelGroupsForItem(AttributeMap itemAttributes) throws MayamClientException
	{
		
		StringList channels = itemAttributes.getAttribute(Attribute.CHANNELS);
		Set<String> channelGroups;
		if (channels == null || channels.isEmpty())
		{
			log.debug("no channels found on item, looking for channels on parent title");
			String titleId = itemAttributes.getAttributeAsString(Attribute.PARENT_HOUSE_ID);
			channelGroups = getChannelGroupsForTitle(titleId);
		}
		else
		{
			log.debug("using channels on item");
			channelGroups = channelProperties.groupsForChannels(channels);
		}
		
		return channelGroups;		
	}

	@Override
	public Set<String> getChannelGroupsForItem(String materialId) throws MayamClientException
	{
		AttributeMap materialAttributes = getMaterialAttributes(materialId);
		if (materialAttributes == null)
		{
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}

		return getChannelGroupsForItem(materialAttributes);
	}

}
