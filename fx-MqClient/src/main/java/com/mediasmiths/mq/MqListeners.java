package com.mediasmiths.mq;

import com.google.inject.Inject;
import com.mayam.wf.mq.Mq;
import com.mayam.wf.mq.Mq.Detachable;
import com.mayam.wf.mq.Mq.ListenIntensity;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.MqDestination;
import com.mayam.wf.mq.MqException;
import com.mayam.wf.mq.MqMessage;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.controllers.MayamTaskController;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;
import com.mediasmiths.mq.listeners.IncomingListener;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.guice.common.shutdown.iface.StoppableService;
import com.mediasmiths.std.threading.Daemon;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class MqListeners extends Daemon implements StoppableService {
	private ArrayList<Detachable> listeners;
	
	
	private final TasksClientVeneer client;
	
	private final MayamTaskController taskController;
	
	private final Mq mq;
	
	private final IncomingListener incomingListener;
	
	private final MediasmithsDestinations destinations;

	private final  ShutdownManager shutDownManager;
	
	protected final static Logger log = Logger.getLogger(MqListeners.class); 
	
	public void run() 
	{
		log.trace("MqListeners.run() enter");
		
		listeners = new ArrayList<Detachable>();
		attachIncomingListners();
		
		while (isRunning()) {
			mq.listen(ListenIntensity.NORMAL);
		}
		
		log.trace("MqListeners.run() return");
	}
	
	@Inject
	public MqListeners(TasksClientVeneer client, MayamTaskController taskController,Mq mq,IncomingListener incomingListener,MediasmithsDestinations destinations, ShutdownManager shutdownManager) 
	{
		log.info("MqListeners startup");
		this.client=client;
		this.taskController=taskController;
		this.mq=mq;
		this.incomingListener=incomingListener;
		this.shutDownManager = shutdownManager;
		this.destinations=destinations;
		
		// register with shutdown manager		
		shutdownManager.register(this);
		this.startThread();
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
		
		for (int i=0; i < listeners.size(); i++)
		{
			listeners.get(i).detach();
		}
		mq.shutdownConsumers();
		mq.shutdownProducers();
		
		stopThread();
	}
}
