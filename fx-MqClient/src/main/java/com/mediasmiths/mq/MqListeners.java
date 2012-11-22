package com.mediasmiths.mq;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.mayam.wf.mq.Mq;
import com.mayam.wf.mq.Mq.Detachable;
import com.mayam.wf.mq.Mq.ListenIntensity;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.AttributeMessageBuilder;
import com.mayam.wf.mq.MqDestination;
import com.mayam.wf.mq.MqException;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mq.listeners.AssetListener;
import com.mediasmiths.mq.listeners.TaskListener;

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
		listeners = new ArrayList<Detachable>();
		attachIncomingListners();
		while (listening.get()) {
			mq.listen(ListenIntensity.NORMAL);
		}
		
		shutdown();
	}
	
	@Inject
	public MqListeners() 
	{
		

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
