package com.mediasmiths.mayamintegration.placeholdermanagement;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.mayam.wf.attributes.server.AttributesModule;
import com.mayam.wf.mq.AttributeMessageBuilder;
import com.mayam.wf.mq.MqModule;
import com.mayam.wf.ws.client.TasksClient;

/**
 * 
 * Creates a number of placeholders, and listens for any related messages
 * 
 */
public class PlaceHolderManager implements Runnable {

	private static Logger logger = Logger.getLogger(PlaceHolderManager.class);

	public static void main(String[] args) {
		try {
			new PlaceHolderManager().run();
		} catch (MalformedURLException e) {
			logger.fatal("MalformedURL", e);
		}
	}

	private final PlaceHolderCreator placeholderCreator;
	private final Thread placeHolderCreatorThread;

	private final MessageListener messageListener;
	private final Thread messageListenerThread;

	final URL url;
	final String token = "someuser:somepassword";
	final Injector injector;
	final TasksClient client;
	final Provider<AttributeMessageBuilder> ambp;

	public PlaceHolderManager() throws MalformedURLException {

		url = new URL("http://localhost:8084/tasks-ws");
		injector = Guice.createInjector(new AttributesModule(), new MqModule(
				"mycoolapp"));
		client = injector.getInstance(TasksClient.class).setup(url, token);
		ambp = injector.getProvider(AttributeMessageBuilder.class);

		placeholderCreator = new PlaceHolderCreator(injector, client);
		placeHolderCreatorThread = new Thread(placeholderCreator);

		messageListener = new MessageListener(injector, client);
		messageListenerThread = new Thread(messageListener);
	}


	public void run() {

		placeHolderCreatorThread.start();
		messageListenerThread.start();

		addShutdownHooks();

	}

	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

		
			public void run() {
				messageListener.stopListening();
			}
		}));
	}

}
