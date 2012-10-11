package com.mediasmiths.mq;
import java.util.ArrayList;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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

public class MqClient {
	private ArrayList<Detachable> listeners;
	
	@Named(MayamClientModule.SETUP_TASKS_CLIENT)
	@Inject
	TasksClient client;
	
	@Inject
	MayamTaskController taskController;
	
	@Inject 
	Mq mq;
	
	@Inject
	public MqClient() 
	{
		listeners = new ArrayList<Detachable>();
		mq.listen(ListenIntensity.NORMAL);
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
		attachListener(Queues.MAM_INCOMING, ComplianceEditingListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, ComplianceLoggingListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, ImportFailureListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, InitiateQcListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, ItemCreationListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, PackageUpdateListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, PreviewTaskListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, QcCompleteListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, IngestCompleteListener.getInstance(client, taskController));
		attachListener(Queues.MAM_INCOMING, FixAndStitchListener.getInstance(client, taskController));
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
