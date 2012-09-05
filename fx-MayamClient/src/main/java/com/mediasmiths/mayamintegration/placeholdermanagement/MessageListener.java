package com.mediasmiths.mayamintegration.placeholdermanagement;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.google.inject.Injector;
import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import com.mayam.wf.mq.Mq;
import com.mayam.wf.mq.MqException;
import com.mayam.wf.mq.MqMessage;
import com.mayam.wf.mq.Mq.Detachable;
import com.mayam.wf.mq.Mq.ListenIntensity;
import com.mayam.wf.mq.Mq.Listener;
import com.mayam.wf.mq.common.ContentTypes;
import com.mayam.wf.mq.common.Queues;
import com.mayam.wf.mq.common.Topics;
import com.mayam.wf.ws.client.TasksClient;

public class MessageListener implements Runnable {

	private static Logger logger = Logger.getLogger(MessageListener.class);

	private final Injector injector;
	private final TasksClient client;
	private final AtomicBoolean keepListening = new AtomicBoolean(true);

	public MessageListener(Injector injector, TasksClient client) {
		this.injector = injector;
		this.client = client;
	}

	
	public void run() {
		logger.trace("MessageListener.run() enter");

		Mq mq = injector.getInstance(Mq.class);

		Detachable someListener = mq.attachListener(Topics.TASK_UPDATE,
				new Listener() {

					public void onMessage(MqMessage msg) throws Throwable {
						logger.trace(String.format("Received task update : %s",msg.getContent()));
					}
				});

		while (keepListening.get()) {
			mq.listen(ListenIntensity.NORMAL);
		}

		// detatch and shutdown
		someListener.detach();
		mq.shutdownConsumers();

		logger.trace("MessageListener.run() return");

	}

	public void stopListening() {
		keepListening.set(false);
	}

}