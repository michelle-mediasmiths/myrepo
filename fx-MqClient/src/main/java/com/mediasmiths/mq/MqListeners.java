package com.mediasmiths.mq;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.server.AttributesModule;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.Mq;
import com.mayam.wf.mq.Mq.Detachable;
import com.mayam.wf.mq.Mq.ListenIntensity;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.AttributeMessageBuilder;
import com.mayam.wf.mq.MqDestination;
import com.mayam.wf.mq.MqException;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.MqModule;
import com.mayam.wf.mq.common.Queues;
import com.mayam.wf.mq.common.Topics;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mq.listeners.AssetDeletionHandler;
import com.mediasmiths.mq.listeners.AssetPurgeHandler;
import com.mediasmiths.mq.listeners.ComplianceEditingHandler;
import com.mediasmiths.mq.listeners.ComplianceLoggingHandler;
import com.mediasmiths.mq.listeners.EmergencyIngestHandler;
import com.mediasmiths.mq.listeners.FixAndStitchHandler;
import com.mediasmiths.mq.listeners.ImportFailureHandler;
import com.mediasmiths.mq.listeners.IngestCompleteHandler;
import com.mediasmiths.mq.listeners.InitiateQcHandler;
import com.mediasmiths.mq.listeners.ItemCreationHandler;
import com.mediasmiths.mq.listeners.PackageUpdateHandler;
import com.mediasmiths.mq.listeners.PreviewTaskHandler;
import com.mediasmiths.mq.listeners.QcCompleteHandler;
import com.mediasmiths.mq.listeners.SegmentationCompleteHandler;
import com.mediasmiths.mq.listeners.TemporaryContentHandler;
import com.mediasmiths.mq.listeners.UnmatchedHandler;


public class MqListeners implements Runnable {
	private ArrayList<Detachable> listeners;
	
	@Named(MayamClientModule.SETUP_TASKS_CLIENT)
	@Inject
	TasksClient client;
	
	@Inject
	MayamTaskController taskController;
	
	@Inject 
	Mq mq;
	
	Injector injector;
	Provider<AttributeMessageBuilder> ambp;
	AtomicBoolean listening = new AtomicBoolean(true);
	
	public void run() 
	{	
		attachIncomingListners();
		
		while (listening.get()) {
			mq.listen(ListenIntensity.NORMAL);
		}
		
		shutdown();
	}
	
	@Inject
	public MqListeners() 
	{
		listeners = new ArrayList<Detachable>();
		injector = Guice.createInjector(new AttributesModule(), new MqModule("fxMayamClient"));
		ambp = injector.getProvider(AttributeMessageBuilder.class);
		mq = injector.getInstance(Mq.class);
		
		//TODO: Fix the guice injection
		URL url;
		try {
			url = new URL("http://localhost:8084/tasks-ws");
			client = injector.getInstance(TasksClient.class);
			client.setup(url, "someuser:somepassword");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void stopListening()
	{
		listening.set(false);
	}
	
	public void startListening()
	{
		listening.set(true);
	}
	
	public void attachListener(MqDestination type, Listener listener)
	{
		Detachable mqListener = mq.attachListener(type, listener);
		listeners.add(mqListener);
	}
	
	//TODO: Other expected listeners?
	// - QC button clicked, update QC flag - DG: Shouldnt this by Mayam?
	public void attachIncomingListners() 
	{
		attachListener(Topics.ASSET_CREATE, UnmatchedHandler.getInstance(taskController));
		attachListener(Topics.ASSET_DELETE, AssetDeletionHandler.getInstance(taskController));
		attachListener(Topics.TASK_DELETE, AssetPurgeHandler.getInstance(taskController));
		attachListener(Topics.ASSET_UPDATE, EmergencyIngestHandler.getInstance(client, taskController));
		attachListener(Topics.ASSET_CREATE, TemporaryContentHandler.getInstance(client, taskController));
		attachListener(Topics.TASK_UPDATE, SegmentationCompleteHandler.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, ComplianceEditingHandler.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, ComplianceLoggingHandler.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, ImportFailureHandler.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, InitiateQcHandler.getInstance(taskController));
		attachListener(Topics.ASSET_CREATE, ItemCreationHandler.getInstance(client, taskController));
		attachListener(Topics.ASSET_UPDATE, PackageUpdateHandler.getInstance(client, taskController));
		attachListener(Topics.TASK_UPDATE, PreviewTaskHandler.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, QcCompleteHandler.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, IngestCompleteHandler.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, FixAndStitchHandler.getInstance(taskController));
	}
	
	public MayamClientErrorCode sendMessage(MqDestination destination, MqMessage message) throws MayamClientException
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		try {
			mq.send(destination, message);
		} catch (MqException e) {
			returnCode = MayamClientErrorCode.MQ_MESSAGE_SEND_FAILED;
			throw new MayamClientException(returnCode);
		}
		return returnCode;
	}

	public void shutdown() {
		listening.set(false);
		for (int i=0; i < listeners.size(); i++)
		{
			listeners.get(i).detach();
		}
		mq.shutdownConsumers();
		mq.shutdownProducers();
	}
	
/*	private void test() {
		final AttributeMap task = client.createAttributeMap();
		task.setAttribute(Attribute.ASSET_TITLE, "Hello");

		try {
			mq.attachListener(Topics.ASSET_CREATE, new Listener() 
			{
				public void onMessage(MqMessage msg) throws Throwable 
				{
					System.out.println("Message recieved");
				}
			});
			
			mq.send(Topics.ASSET_CREATE, ambp.get().subject(task).build());
			System.out.println("Success - Message sent!");
			
		} catch (MqException e) {
			e.printStackTrace();
			System.out.println("Failure - Message send failed!");
		}
	}*/
}
