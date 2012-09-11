package com.mediasmiths.mayam;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import com.mayam.wf.mq.AttributeMessageBuilder;
import com.mayam.wf.mq.MqModule;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.foxtel.generated.MediaExchange.Programme;
import com.mediasmiths.mayam.controllers.MayamMaterialController;
import com.mediasmiths.mayam.controllers.MayamPackageController;
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
	
	public MayamClientImpl() throws MalformedURLException, IOException {
		url = new URL("http://localhost:8084/tasks-ws");
		injector = Guice.createInjector(new AttributesModule(), new MqModule("fxMayamClient"));
		client = injector.getInstance(TasksClient.class).setup(url, token); //throws ioexception
		attributeMessageBuilder = injector.getProvider(AttributeMessageBuilder.class);
		mqClient = new MqClient(injector);
		titleController = new MayamTitleController(client, mqClient);
		materialController = new MayamMaterialController(client, mqClient);
		packageController = new MayamPackageController(client, mqClient);
	}
	
	public MayamClientImpl(URL tasksURL, String mqModuleName, String userToken) throws MalformedURLException, IOException {
		url = tasksURL;
		injector = Guice.createInjector(new AttributesModule(), new MqModule(mqModuleName));
		client = injector.getInstance(TasksClient.class).setup(url, userToken); //throws ioexception
		attributeMessageBuilder = injector.getProvider(AttributeMessageBuilder.class);
		mqClient = new MqClient(injector);
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
	 * @see com.mediasmiths.mayam.MayamClient#createTitle(com.mediasmiths.foxtel.generated.MediaExchange.Programme.Detail)
	 */
	@Override
	public MayamClientErrorCode createTitle(Programme.Detail title)
	{
		return titleController.createTitle(title);
	}
	
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#createTitle(au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle)
	 */
	@Override
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title)
	{
		return titleController.createTitle(title);
	}

	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#updateTitle(com.mediasmiths.foxtel.generated.MediaExchange.Programme.Detail)
	 */
	@Override
	public MayamClientErrorCode updateTitle(Programme.Detail programme)
	{
		return titleController.updateTitle(programme);
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
	 * @see com.mediasmiths.mayam.MayamClient#createMaterial(com.mediasmiths.foxtel.generated.MediaExchange.Programme.Media)
	 */
	@Override
	public MayamClientErrorCode createMaterial(Programme.Media media)
	{
		return materialController.createMaterial(media);
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
	public MayamClientErrorCode updateMaterial(Programme.Media media)
	{
		return materialController.updateMaterial(media);
	}
	
	/* (non-Javadoc)
	 * @see com.mediasmiths.mayam.MayamClient#updateMaterial(au.com.foxtel.cf.mam.pms.MaterialType)
	 */
	@Override
	public MayamClientErrorCode updateMaterial(MaterialType material)
	{
		return materialController.updateMaterial(material);
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
	 * @see com.mediasmiths.mayam.MayamClient#createPackage()
	 */
	@Override
	public MayamClientErrorCode createPackage()
	{
		return packageController.createPackage();
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
	public MayamClientErrorCode updatePackage()
	{
		return packageController.updatePackage();
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
	public boolean materialExists(String materialID)
			throws MayamClientException {
		return materialController.materialExists(materialID);
	}

	@Override
	public boolean isMaterialForPackageProtected(String packageID) {
		//TODO implement
		// will need to fetch the material for the given package and check its protected status flag		
		return false;
	}

	@Override
	public boolean isTitleOrDescendentsProtected(String titleID)
			throws MayamClientException {
		// TODO implement
		// will need to fetch the specified title and check it is not protected
		// then need to check its material + packages are not protected either
		return false;
	}

	@Override
	public MayamClientErrorCode deleteMaterial(DeleteMaterial deleteMaterial) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean packageExists(String presentationID)
			throws MayamClientException {
		// TODO Auto-generated method stub
		return false;
	}

}
