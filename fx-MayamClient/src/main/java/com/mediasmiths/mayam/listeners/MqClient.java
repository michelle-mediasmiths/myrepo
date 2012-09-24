package com.mediasmiths.mayam.listeners;
import java.util.ArrayList;

import com.google.inject.Injector;
import com.mayam.wf.mq.Mq;
import com.mayam.wf.mq.Mq.Detachable;
import com.mayam.wf.mq.Mq.ListenIntensity;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.MqDestination;
import com.mayam.wf.mq.MqException;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.common.Queues;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;
import com.mediasmiths.mayam.controllers.MayamTaskController;

public class MqClient {
	private final Mq mq;
	private ArrayList<Detachable> listeners;
	private TasksClient client;
	private MayamTaskController taskController;
	
	public MqClient(Injector injector, TasksClient mayamClient, MayamTaskController mayamTaskController) 
	{
		mq = injector.getInstance(Mq.class);
		listeners = new ArrayList<Detachable>();
		mq.listen(ListenIntensity.NORMAL);
		client = mayamClient;
		taskController = mayamTaskController;
	}
		
	public void dispose()
	{
		for (int i=0; i < listeners.size(); i++)
		{
			listeners.get(i).detach();
		}
		mq.shutdownConsumers();
		mq.shutdownProducers();
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
		attachListener(Queues.MAM_INCOMING, UnmatchedListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, AssetDeletionListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, AssetPurgeListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, EmergencyIngestListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, TemporaryContentListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, SegmentationCompleteListener.getInstance(client, taskController));
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
	
}
