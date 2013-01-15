package com.mediasmiths.mayam;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
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
import com.mayam.wf.attributes.shared.type.AudioTrack;
import com.mayam.wf.attributes.shared.type.AudioTrack.EncodingType;
import com.mayam.wf.attributes.shared.type.AudioTrackList;
import com.mayam.wf.attributes.shared.type.CommentLog;
import com.mayam.wf.attributes.shared.type.FileFormatInfo;
import com.mayam.wf.attributes.shared.type.Segment;
import com.mayam.wf.attributes.shared.type.SegmentList;
import com.mayam.wf.attributes.shared.type.StringList;
import com.mayam.wf.attributes.shared.type.TaskState;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.exception.RemoteException;
import com.mayam.wf.attributes.shared.type.MediaStatus;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Details;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType.Presentation.Package;
import com.mediasmiths.foxtel.generated.mediaexchange.AudioTrackType;
import com.mediasmiths.foxtel.generated.mediaexchange.ClassificationType;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme.Media;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme.Media.AudioTracks;
import com.mediasmiths.foxtel.generated.mediaexchange.Programme.Media.Segments;
import com.mediasmiths.foxtel.generated.ruzz.DetailType;
import com.mediasmiths.foxtel.generated.ruzz.RuzzIF;
import com.mediasmiths.mayam.accessrights.MayamAccessRightsController;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.controllers.MayamTitleController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.util.MediaExchangeProgrammeOutputBuilder;
import com.mediasmiths.mayam.util.RuzzProgrammeOutputBuilder;
import com.mediasmiths.mayam.util.SegmentUtil;
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
	public MayamClientErrorCode updateMaterial(ProgrammeMaterialType material, Material.Details details, Material.Title title)
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
		
		boolean isProtected = false;
		
		SegmentList segmentList = packageController.getSegmentList(packageID);
		String materialID = segmentList.getAttributeMap().getAttribute(Attribute.PARENT_HOUSE_ID);
		
		log.debug(String.format("material id %s found for package %s", materialID, packageID));
		
		if (materialID != null)
		{
			isProtected = materialController.isProtected(materialID);
		}
		
		return isProtected;
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
		boolean isPlaceholder = false;
		AttributeMap materialAttributes = materialController.getMaterialAttributes(materialID);

		if (materialAttributes != null && materialAttributes.containsAttribute(Attribute.MEDST_HR))
		{
			MediaStatus mediaStatus = materialAttributes.getAttribute(Attribute.MEDST_HR);
			if (mediaStatus != null)
			{
				log.info("Media status is " + mediaStatus.toString());
				if (mediaStatus== MediaStatus.MISSING) 
				{
					isPlaceholder = true;
					
				}
			}
			else
			{
				log.error("Media status is null");
			}
		}
		return isPlaceholder;
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
	public PackageType getPackage(String packageID) throws MayamClientException
	{
		return packageController.getPackageType(packageID);
	}

	@Override
	public String pathToMaterial(String materialID) throws MayamClientException
	{
		
		AttributeMap material = materialController.getMaterialAttributes(materialID);
		
		String assetID = material.getAttribute(Attribute.ASSET_ID);
		
		FileFormatInfo fileinfo;
		try
		{
			fileinfo = client.assetApi().getFormatInfo(MayamAssetType.MATERIAL.getAssetType(), assetID);
		}
		catch (RemoteException e)
		{
			log.error("error getting file format info for material "+materialID,e);
			throw new MayamClientException(MayamClientErrorCode.FILE_FORMAT_QUERY_FAILED,e);
		}
		
		
		List<String> urls = fileinfo.getUrls();
		
		for (String url : urls)
		{
			log.debug("url: "+url);
		}
		
		return urls.get(0);
	}

	@Override
	public MaterialType getMaterial(String materialID) throws MayamClientException
	{
		return materialController.getPHMaterialType(materialID);
	}

	@Override
	public AttributeMap getTaskForAsset(MayamTaskListType type, String id) throws MayamClientException
	{
		return tasksController.getTaskForAssetBySiteID(type, id);
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
	public Programme getProgramme(String packageID) throws MayamClientException
	{
		
		//fetch the packages information		
		FullProgrammePackageInfo pack = new FullProgrammePackageInfo(packageID, packageController, materialController, titleController);
		//build the Programme Object
		return MediaExchangeProgrammeOutputBuilder.buildProgramme(pack);
	}
	
	@Override
	public RuzzIF getRuzzProgramme(String packageID) throws MayamClientException
	{
		//fetch the packages information		
		FullProgrammePackageInfo pack = new FullProgrammePackageInfo(packageID, packageController, materialController, titleController);
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
		long id = tasksController.createTask(packageID, MayamAssetType.PACKAGE, MayamTaskListType.TX_DELIVERY_FAILURE);
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

		AttributeMap title = titleController.getTitle(titleID);
		Boolean adult = title.getAttribute(Attribute.CONT_RESTRICTED_MATERIAL);

		if (adult != null)
		{
			return adult.booleanValue();
		}
		else
		{
			log.error("CONT_RESTRICTED_MATERIAL attribute missing from title " + titleID);
			throw new MayamClientException(MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES);
		}
	}

	@Override
	public boolean isPackageAO(String packageID) throws MayamClientException
	{
		FullProgrammePackageInfo info = new FullProgrammePackageInfo(packageID, packageController, materialController, titleController);
		
		Boolean adult = info.getTitleAttributes().getAttribute(Attribute.CONT_RESTRICTED_MATERIAL);

		if (adult != null)
		{
			return adult.booleanValue();
		}
		else
		{
			log.error("CONT_RESTRICTED_MATERIAL attribute missing from title " + info.getTitleID());
			throw new MayamClientException(MayamClientErrorCode.ONE_OR_MORE_INVALID_ATTRIBUTES);
		}
	}

	@Override
	public void autoQcFailedForMaterial(String materialId) throws MayamClientException
	{
		tasksController.autoQcFailedForMaterial(materialId);
	}

	@Override
	public void autoQcPassedForMaterial(String materialId) throws MayamClientException
	{
		tasksController.autoQcPassedForMaterial(materialId);
		
	}
	
}
