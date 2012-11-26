package com.mediasmiths.mayam.guice;

import static com.mediasmiths.mayam.MayamClientConfig.MAYAM_AUTH_TOKEN;
import static com.mediasmiths.mayam.MayamClientConfig.MAYAM_ENDPOINT;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.mayam.wf.attributes.server.AttributesModule;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientImpl;
import com.mediasmiths.mayam.validation.MayamValidator;
import com.mediasmiths.mayam.validation.MayamValidatorImpl;

public class MayamClientModule extends AbstractModule
{

	public final static String SETUP_TASKS_CLIENT = "SETUP_TASKS_CIENT";
	
	private final static Logger log = Logger.getLogger(MayamClientModule.class);
	
	@Override
	protected void configure()
	{
		install(new SecurityModule());
		install(new AttributesModule());
		bind(MayamClient.class).to(MayamClientImpl.class);
		bind(MayamValidator.class).to(MayamValidatorImpl.class);
		
	}

	@Provides
	@Named(SETUP_TASKS_CLIENT)
	public TasksClient getSetupTasksClient(TasksClient tc,@Named(MAYAM_ENDPOINT) String endpoint,
			@Named(MAYAM_AUTH_TOKEN) String token) throws MalformedURLException{
		
		log.info(String.format("Using mayam endpoint %s and auth token %s", endpoint,token));
		
		URL url = new URL(endpoint);
		return tc.setup(url, token);
	}
	
}
