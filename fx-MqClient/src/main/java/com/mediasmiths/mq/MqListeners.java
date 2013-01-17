package com.mediasmiths.mq;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.mayam.wf.mq.AttributeMessageBuilder;
import com.mayam.wf.mq.Mq;
import com.mayam.wf.mq.Mq.Detachable;
import com.mayam.wf.mq.Mq.ListenIntensity;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.MqDestination;
import com.mayam.wf.mq.MqException;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.foxtel.ip.event.EventService;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mq.listeners.IncomingListener;

public class MqListeners implements Runnable {
	private ArrayList<Detachable> listeners;
	
	@Named(MayamClientModule.SETUP_TASKS_CLIENT)
	@Inject
	TasksClient client;
	
	@Inject
	MayamTaskController taskController;
	
	@Inject 
	Mq mq;
	
	@Inject
	IncomingListener incomingListener;
	
	@Inject
	private MediasmithsDestinations destinations;

	//need a marshaller before this will inject
//	@Inject
//	EventService eventService;
	
	Injector injector;
	Provider<AttributeMessageBuilder> ambp;
	AtomicBoolean listening = new AtomicBoolean(true);
	
	protected final static Logger log = Logger.getLogger(MqListeners.class); 
	
	public void run() 
	{
		log.trace("MqListeners.run() enter");
		
		listeners = new ArrayList<Detachable>();
		attachIncomingListners();
		
		while (listening.get()) {
			mq.listen(ListenIntensity.NORMAL);
		}
		
		shutdown();
		
		log.trace("MqListeners.run() return");
	}
	
	@Inject
	public MqListeners() 
	{
		log.trace("MqListeners()");
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
		log.info("Attatching listeners");
		attachListener(destinations.getIncoming(), incomingListener);
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
		
		log.info("Shutting down");
		
		listening.set(false);
		for (int i=0; i < listeners.size(); i++)
		{
			listeners.get(i).detach();
		}
		mq.shutdownConsumers();
		mq.shutdownProducers();
	}
}
