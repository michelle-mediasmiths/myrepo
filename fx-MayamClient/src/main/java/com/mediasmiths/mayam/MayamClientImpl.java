package com.mediasmiths.mayam;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
import au.com.foxtel.cf.mam.pms.DeleteMaterial;
import au.com.foxtel.cf.mam.pms.DeletePackage;
import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PurgeTitle;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.mayam.wf.attributes.server.AttributesModule;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.attributes.shared.type.IdSet;
import com.mayam.wf.mq.AttributeMessageBuilder;
import com.mayam.wf.mq.MqModule;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialExchange.MarketingMaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.ProgrammeMaterialType;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamPackageController;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.controllers.MayamTitleController;

public class MayamClientImpl implements MayamClient {
	final URL url;
	final String token = "someuser:somepassword";
	final Injector injector;
	final TasksClient client;
	final Provider<AttributeMessageBuilder> attributeMessageBuilder;
	final MqClient mqClient;
	final MayamTitleController titleController;
	final MayamMaterialController materialController;
	final MayamPackageController packageController;
	final MayamTaskController tasksController;	
	
	public MayamClientImpl() throws MalformedURLException, IOException {
		url = new URL("http://localhost:8084/tasks-ws");
		injector = Guice.createInjector(new AttributesModule(), new MqModule("fxMayamClient"));
		client = injector.getInstance(TasksClient.class).setup(url, token); //throws ioexception
		attributeMessageBuilder = injector.getProvider(AttributeMessageBuilder.class);
		tasksController = new MayamTaskController(client);
		mqClient = new MqClient(injector, client, tasksController);
		titleController = new MayamTitleController(client, mqClient);
		materialController = new MayamMaterialController(client, mqClient);
		packageController = new MayamPackageController(client, mqClient);
	}
	
	public MayamClientImpl(URL tasksURL, String mqModuleName, String userToken) throws MalformedURLException, IOException {
		url = tasksURL;
		injector = Guice.createInjector(new AttributesModule(), new MqModule(mqModuleName));
		client = injector.getInstance(TasksClient.class).setup(url, userToken); //throws ioexception
		attributeMessageBuilder = injector.getProvider(AttributeMessageBuilder.class);
		tasksController = new MayamTaskController(client);
		mqClient = new MqClient(injector, client, tasksController);
		titleController = new MayamTitleController(client, mqClient);
		materialController = new MayamMaterialController(client, mqClient);
		packageController = new MayamPackageController(client, mqClient);
	}
	
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#shutdown()
	 */
	@Override
	public void shutdown()
	{
		mqClient.dispose();
	}
	
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#createTitle(au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle)
	 */
	@Override
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title)
	{
		return titleController.createTitle(title);
	}
	
	@Override
	public MayamClientErrorCode createTitle(Title title) {
		return titleController.createTitle(title);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#updateTitle(com.mediasmiths.foxtel.generated.MediaExchange.Programme.Detail)
	 */
	@Override
	public MayamClientErrorCode updateTitle(Material.Title title)
	{
		return titleController.updateTitle(title);
	}
	
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#updateTitle(au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle)
	 */
	@Override
	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title)
	{
		return titleController.updateTitle(title);
	}
	
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#purgeTitle(au.com.foxtel.cf.mam.pms.PurgeTitle)
	 */
	@Override
	public MayamClientErrorCode purgeTitle(PurgeTitle title)
	{
		return titleController.purgeTitle(title);
	}
	
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#createMaterial(au.com.foxtel.cf.mam.pms.MaterialType)
	 */
	@Override
	public MayamClientErrorCode createMaterial(MaterialType material)
	{
		return materialController.createMaterial(material);
	}
		
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#updateMaterial(com.mediasmiths.foxtel.generated.MediaExchange.Programme.Media)
	 */
	@Override
	public MayamClientErrorCode updateMaterial(ProgrammeMaterialType material)
	{
		return materialController.updateMaterial(material);
	}
	
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#updateMaterial(au.com.foxtel.cf.mam.pms.MaterialType)
	 */
	@Override
	public MayamClientErrorCode updateMaterial(MaterialType material)
	{
		return materialController.updateMaterial(material);
	}
	
	@Override
	public MayamClientErrorCode deleteMaterial(DeleteMaterial deleteMaterial) {
		return materialController.deleteMaterial(deleteMaterial.getMaterial().getMaterialID());
	}
	
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#createPackage(au.com.foxtel.cf.mam.pms.PackageType)
	 */
	@Override
	public MayamClientErrorCode createPackage(PackageType txPackage)
	{
		return packageController.createPackage(txPackage);
	}
		
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#updatePackage(au.com.foxtel.cf.mam.pms.PackageType)
	 */
	@Override
	public MayamClientErrorCode updatePackage(PackageType txPackage)
	{
		return packageController.updatePackage(txPackage);
	}
	
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#updatePackage()
	 */
	@Override
	public MayamClientErrorCode updatePackage(ProgrammeMaterialType.Presentation.Package txPackage)
	{
		return packageController.updatePackage(txPackage);
	}
	
	/* (non-Javadoc)
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
	public boolean titleExists(String titleID) {
		return titleController.titleExists(titleID);
	}

	@Override
	public boolean materialExists(String materialID) throws MayamClientException {
		return materialController.materialExists(materialID);
	}

	@Override
	public boolean isMaterialForPackageProtected(String packageID) {
		//TODO implement
		// will need to fetch the material for the given package and check its protected status flag		
		boolean isProtected = true;
		AttributeMap packageAttributes = packageController.getPackage(packageID);
		if (packageAttributes != null) {
			//TODO: need to make use of parent ID attribute once Mayam add it
		}
		return isProtected;
	}

	@Override
	public boolean isTitleOrDescendentsProtected(String titleID) throws MayamClientException {
		boolean isProtected = false;
		AttributeMap titleAttributes = titleController.getTitle(titleID);
		
		if (titleAttributes != null) {
			//TODO: Are we checking accessRestriction or purgeProtection?
			isProtected = titleAttributes.getAttribute(Attribute.APP_FLAG);
			//isProtected = titleAttributes.getAttribute(Attribute.AUX_FLAG);
			
			if (!isProtected) {
				try {
					List<AttributeMap> materials = client.getAssetChildren(AssetType.SER, titleID, AssetType.ITEM);
					for (int i = 0; i < materials.size(); i++) {
						AttributeMap materialAttributes = materials.get(i);
						
						//TODO: Are we checking accessRestriction or purgeProtection?
						//materialAttributes.getAttribute(Attribute.AUX_FLAG);
						if (materialAttributes.getAttribute(Attribute.APP_FLAG)) {
							isProtected = true;
							break;
						}	
					}
					
					List<AttributeMap> packages = client.getAssetChildren(AssetType.SER, titleID, AssetType.PACK);
					for (int i = 0; i < packages.size(); i++) {
						AttributeMap packageAttributes = packages.get(i);
						
						//TODO: Are we checking accessRestriction or purgeProtection?
						//packageAttributes.getAttribute(Attribute.AUX_FLAG);
						if (packageAttributes.getAttribute(Attribute.APP_FLAG)) {
							isProtected = true;
							break;
						}	
					}
					
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return isProtected;
	}

	@Override
	public boolean packageExists(String presentationID)
			throws MayamClientException {
		return packageController.packageExists(presentationID);
	}

	@Override
	public boolean isMaterialPlaceholder(String materialID) {
		boolean isPlaceholder = true;
		AttributeMap materialAttributes = materialController.getMaterial(materialID);
		
		if (materialAttributes != null) {
			IdSet sourceIds = materialAttributes.getAttribute(Attribute.SOURCE_IDS);
			if (sourceIds != null) {
				isPlaceholder = false;
			}
			else {
				//TODO: Need to check segment data once implemented in order to determind if placeholder
			}
		}
		return isPlaceholder;	
	}

	/**
	 * creates an item in viz ardome for associated media files.
	 * @return the master ID of the created item
	 */
	@Override
	public String createMaterial(String titleID, MarketingMaterialType material)  throws MayamClientException{
		AttributeMap titleAttributes = titleController.getTitle(titleID);
		
		if (titleAttributes == null) {
			throw new MayamClientException(MayamClientErrorCode.TITLE_FIND_FAILED);
		}
		
		//TODO: set some id relating to material - origin of id TBC
		String materialID = "???";
		
		titleAttributes.setAttribute(Attribute.MOB_ID, materialID);
		try {
			client.updateAsset(titleAttributes);
		} catch (RemoteException e) {
			throw new MayamClientException(MayamClientErrorCode.TITLE_UPDATE_FAILED);
		}
		
		//TODO: need to generate an id for the material? or can we trust the one that Mayam will create?
		MayamClientErrorCode returnCode = materialController.createMaterial(material);
		if (returnCode != MayamClientErrorCode.SUCCESS) {
			throw new MayamClientException(returnCode);
		}
		
		AttributeMap materialAttributes = materialController.getMaterial(materialID);
		
		if (materialAttributes == null) {
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_FIND_FAILED);
		}
		
		//TODO: Set parent ID once implemented
		//materialAttributes.setAttribute(Attribute., titleID);
		try {
			client.updateAsset(materialAttributes);
		} catch (RemoteException e) {
			throw new MayamClientException(MayamClientErrorCode.MATERIAL_UPDATE_FAILED);
		}

		return materialID;
	}

}
