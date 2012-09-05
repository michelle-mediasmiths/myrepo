package com.mediasmiths.mayam;
import java.util.ArrayList;

import com.google.inject.Injector;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.Mq;
import com.mayam.wf.mq.Mq.Detachable;
import com.mayam.wf.mq.Mq.ListenIntensity;
import com.mayam.wf.mq.MqDestination;
import com.mayam.wf.mq.MqException;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.mq.common.Queues;


public class MqClient {
	private final Mq mq;
	private ArrayList<Detachable> listeners;
	
	public MqClient(Injector injector) 
	{
		mq = injector.getInstance(Mq.class);
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
	
	public void attachIncomingListner() 
	{
		Listener listener = new Listener() {
			public void onMessage(MqMessage msg) throws Throwable {
				System.out.println(msg.getContent());
				if (msg.getType().equals(ContentTypes.ATTRIBUTES)) {
	
					AttributeMap messageAttributes = msg.getSubject();
	
					String assetTitle = messageAttributes.getAttribute(Attribute.ASSET_TITLE);
	
					if (assetTitle != null) {
							
					}
				}
	
			}
		};

		attachListener(Queues.MAM_INCOMING, listener);
	}
	
	public MayamClientErrorCode sendMessage(MqDestination destination, MqMessage message)
	{
		MayamClientErrorCode returnCode = MayamClientErrorCode.SUCCESS;
		try {
			mq.send(destination, message);
		} catch (MqException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			returnCode = MayamClientErrorCode.FAILURE;
		}
		return returnCode;
	}
}
