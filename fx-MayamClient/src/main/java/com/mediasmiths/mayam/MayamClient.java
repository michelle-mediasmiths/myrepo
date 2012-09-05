package com.mediasmiths.mayam;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.mayam.wf.attributes.server.AttributesModule;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.attributes.shared.type.AssetType;
import com.mayam.wf.mq.AttributeMessageBuilder;
import com.mayam.wf.mq.MqModule;
import com.mayam.wf.ws.client.TasksClient;
import com.mayam.wf.ws.client.TasksClient.RemoteException;
import com.mediasmiths.foxtel.generated.MaterialsService.TitleDetailType;
import com.mediasmiths.foxtel.generated.MediaExchange.Programme;

public class MayamClient {
	final URL url;
	final String token = "someuser:somepassword";
	final Injector injector;
	final TasksClient client;
	final Provider<AttributeMessageBuilder> attributeMessageBuilder;

	public MayamClient() throws MalformedURLException {
		url = new URL("http://localhost:8084/tasks-ws");
		injector = Guice.createInjector(new AttributesModule(), new MqModule("fxMayamClient"));
		client = injector.getInstance(TasksClient.class).setup(url, token);
		attributeMessageBuilder = injector.getProvider(AttributeMessageBuilder.class);
	}
	
	public MayamClient(URL tasksURL, String mqModuleName, String userToken) throws MalformedURLException {
		url = tasksURL;
		injector = Guice.createInjector(new AttributesModule(), new MqModule(mqModuleName));
		client = injector.getInstance(TasksClient.class).setup(url, userToken);
		attributeMessageBuilder = injector.getProvider(AttributeMessageBuilder.class);
	}
	
	//Title - Creating a title asset in Mayam
	public MayamClientErrorCode createTitle(Programme.Detail title)
	{
		
		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	public MayamClientErrorCode createTitle(TitleDetailType title)
	{
		final AttributeMap assetAttributes = client.createAttributeMap();
		assetAttributes.setAttribute(Attribute.ASSET_TYPE, AssetType.SER);
		assetAttributes.setAttribute(Attribute.SERIES_TITLE, title.getName());

		AttributeMap result;
		try {
			result = client.createAsset(assetAttributes);
		} catch (RemoteException e) {
			e.printStackTrace();
			e.printRemoteMessages(System.err);
		}
		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	//Title - Updating a title asset in Mayam
	public MayamClientErrorCode updateTitle(Programme.Detail programme)
	{

		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	public MayamClientErrorCode updateTitle(TitleDetailType title)
	{
		AttributeMap asset;
		try {
			asset = client.getAsset(AssetType.SER, title.getTitleID());
			asset.setAttribute(Attribute.SERIES_TITLE, title.getName());
			client.updateAsset(asset);

		} catch (RemoteException e) {
			e.printStackTrace();
			e.printRemoteMessages(System.err);
		}
		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	//Media - Creating a media asset in Mayam
	public MayamClientErrorCode createMedia(Programme.Media media)
	{

		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	public MayamClientErrorCode createMedia(TitleDetailType.SourceMedia media)
	{
		final AttributeMap assetAttributes = client.createAttributeMap();
		assetAttributes.setAttribute(Attribute.ASSET_TYPE, AssetType.ITEM);

		AttributeMap result;
		try {
			result = client.createAsset(assetAttributes);
		} catch (RemoteException e) {
			e.printStackTrace();
			e.printRemoteMessages(System.err);
		}
		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	//Media - Updating a media asset in Mayam
	public MayamClientErrorCode updateMedia(Programme.Media media)
	{

		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	public MayamClientErrorCode updateMedia(TitleDetailType.SourceMedia media)
	{

		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	//Version - Creating a version asset in Mayam
	public MayamClientErrorCode createVersion()
	{

		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	public MayamClientErrorCode createVersion(TitleDetailType.Versions versions)
	{

		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	//Version - Updating a version asset in Mayam
	public MayamClientErrorCode updateVersion()
	{

		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
	
	public MayamClientErrorCode updateVersion(TitleDetailType.Versions versions)
	{

		return MayamClientErrorCode.NOT_IMPLEMENTED;
	}
}
