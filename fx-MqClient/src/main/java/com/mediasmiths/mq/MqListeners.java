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
import com.mediasmiths.mq.listeners.AssetDeletionListener;
import com.mediasmiths.mq.listeners.AssetPurgeListener;
import com.mediasmiths.mq.listeners.ComplianceEditingListener;
import com.mediasmiths.mq.listeners.ComplianceLoggingListener;
import com.mediasmiths.mq.listeners.EmergencyIngestListener;
import com.mediasmiths.mq.listeners.FixAndStitchListener;
import com.mediasmiths.mq.listeners.ImportFailureListener;
import com.mediasmiths.mq.listeners.IngestCompleteListener;
import com.mediasmiths.mq.listeners.InitiateQcListener;
import com.mediasmiths.mq.listeners.ItemCreationListener;
import com.mediasmiths.mq.listeners.PackageUpdateListener;
import com.mediasmiths.mq.listeners.PreviewTaskListener;
import com.mediasmiths.mq.listeners.QcCompleteListener;
import com.mediasmiths.mq.listeners.SegmentationCompleteListener;
import com.mediasmiths.mq.listeners.TemporaryContentListener;
import com.mediasmiths.mq.listeners.UnmatchedListener;


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
	
	public void setListenIntensity(ListenIntensity intensity)
	{
		mq.listen(intensity);
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
		attachListener(Topics.ASSET_CREATE, UnmatchedListener.getInstance(taskController));
		attachListener(Topics.ASSET_DELETE, AssetDeletionListener.getInstance(taskController));
		attachListener(Topics.TASK_DELETE, AssetPurgeListener.getInstance(taskController));
		attachListener(Topics.ASSET_UPDATE, EmergencyIngestListener.getInstance(client, taskController));
		attachListener(Topics.ASSET_CREATE, TemporaryContentListener.getInstance(client, taskController));
		attachListener(Topics.TASK_UPDATE, SegmentationCompleteListener.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, ComplianceEditingListener.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, ComplianceLoggingListener.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, ImportFailureListener.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, InitiateQcListener.getInstance(taskController));
		attachListener(Topics.ASSET_CREATE, ItemCreationListener.getInstance(client, taskController));
		attachListener(Topics.ASSET_UPDATE, PackageUpdateListener.getInstance(client, taskController));
		attachListener(Topics.TASK_UPDATE, PreviewTaskListener.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, QcCompleteListener.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, IngestCompleteListener.getInstance(taskController));
		attachListener(Topics.TASK_UPDATE, FixAndStitchListener.getInstance(taskController));
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
