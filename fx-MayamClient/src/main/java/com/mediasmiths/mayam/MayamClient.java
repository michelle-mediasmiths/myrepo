package com.mediasmiths.mayam;

import java.net.MalformedURLException;
import java.net.URL;
import au.com.foxtel.cf.mam.pms.CreateOrUpdateTitle;
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

public class MayamClient {
	final URL url;
	final String token = "someuser:somepassword";
	final Injector injector;
	final TasksClient client;
	final Provider<AttributeMessageBuilder> attributeMessageBuilder;
	final MqClient mqClient;
	final MayamTitleController titleController;
	final MayamMaterialController materialController;
	final MayamPackageController packageController;
	
	public MayamClient() throws MalformedURLException {
		url = new URL("http://localhost:8084/tasks-ws");
		injector = Guice.createInjector(new AttributesModule(), new MqModule("fxMayamClient"));
		client = injector.getInstance(TasksClient.class).setup(url, token);
		attributeMessageBuilder = injector.getProvider(AttributeMessageBuilder.class);
		mqClient = new MqClient(injector);
		titleController = new MayamTitleController(client, mqClient);
		materialController = new MayamMaterialController(client, mqClient);
		packageController = new MayamPackageController(client, mqClient);
	}
	
	public MayamClient(URL tasksURL, String mqModuleName, String userToken) throws MalformedURLException {
		url = tasksURL;
		injector = Guice.createInjector(new AttributesModule(), new MqModule(mqModuleName));
		client = injector.getInstance(TasksClient.class).setup(url, userToken);
		attributeMessageBuilder = injector.getProvider(AttributeMessageBuilder.class);
		mqClient = new MqClient(injector);
		titleController = new MayamTitleController(client, mqClient);
		materialController = new MayamMaterialController(client, mqClient);
		packageController = new MayamPackageController(client, mqClient);
	}
	
	public void shutdown()
	{
		mqClient.dispose();
	}
	
	public MayamClientErrorCode createTitle(Programme.Detail title)
	{
		return titleController.createTitle(title);
	}
	
	public MayamClientErrorCode createTitle(CreateOrUpdateTitle title)
	{
		return titleController.createTitle(title);
	}

	public MayamClientErrorCode updateTitle(Programme.Detail programme)
	{
		return titleController.updateTitle(programme);
	}
	
	public MayamClientErrorCode updateTitle(CreateOrUpdateTitle title)
	{
		return titleController.updateTitle(title);
	}
	
	public MayamClientErrorCode purgeTitle(PurgeTitle title)
	{
		return titleController.purgeTitle(title);
	}
	
	public MayamClientErrorCode createMaterial(Programme.Media media)
	{
		return materialController.createMaterial(media);
	}
	
	public MayamClientErrorCode createMaterial(MaterialType material)
	{
		return materialController.createMaterial(material);
	}
		
	public MayamClientErrorCode updateMaterial(Programme.Media media)
	{
		return materialController.updateMaterial(media);
	}
	
	public MayamClientErrorCode updateMaterial(MaterialType material)
	{
		return materialController.updateMaterial(material);
	}
	
	public MayamClientErrorCode createPackage(PackageType txPackage)
	{
		return packageController.createPackage(txPackage);
	}
	
	public MayamClientErrorCode createPackage()
	{
		return packageController.createPackage();
	}
	
	public MayamClientErrorCode updatePackage(PackageType txPackage)
	{
		return packageController.updatePackage(txPackage);
	}
	
	public MayamClientErrorCode updatePackage()
	{
		return packageController.updatePackage();
	}
	
	public MayamClientErrorCode purgePackage()
	{
		return packageController.purgePackage();
	}
}
