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
import com.mayam.wf.mq.Mq;
import com.mayam.wf.mq.Mq.Detachable;
import com.mayam.wf.mq.Mq.ListenIntensity;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.AttributeMessageBuilder;
import com.mayam.wf.mq.MqDestination;
import com.mayam.wf.mq.MqException;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.MqModule;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mq.listeners.AssetListener;
import com.mediasmiths.mq.listeners.TaskListener;
import com.mediasmiths.mule.worflows.MuleWorkflowController;

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
		MuleWorkflowController mule = new MuleWorkflowController();
		mule.initiateQcWorkflow("12345", false);
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
	
	public void attachIncomingListners() 
	{
		attachListener(MediasmithsDestinations.TASKS, TaskListener.getInstance(taskController));
		attachListener(MediasmithsDestinations.ASSETS, AssetListener.getInstance(client, taskController));
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
}
