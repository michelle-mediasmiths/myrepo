package com.mediasmiths.mayam.util.test;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.guice.common.shutdown.iface.ShutdownManager;
import com.mediasmiths.std.io.PropertyFile;

public class MayamClientTest
{
	private final static Logger logger = Logger.getLogger(MayamClientTest.class);
	
	public static void main(String[] args) throws InterruptedException
	{
		final Injector injector = GuiceInjectorBootstrap.createInjector(new GuiceSetup()
		{
			
			@Override
			public void registerModules(List<Module> modules, PropertyFile config)
			{
				modules.add(new MayamClientModule());
				modules.add(new AbstractModule()
				{
					
					@Override
					protected void configure()
					{
						
						
					}
					
					@Provides
					@Named("mayam.endpoint")
					public String provideEndpoint(){
						return "http://mamwkf01.mam.foxtel.com.au:8084/tasks-ws";
					}
					
					@Provides
					@Named("mayam.auth.token")
					public String provideAuthToken(){
						return "mediasmiths:U8yPfqweVB";
					}
					
				});
			}
			
			@Override
			public void injectorCreated(Injector injector)
			{
				// TODO Auto-generated method stub
				
			}
		});
		
		try
		{
			MayamClientTest mct = injector.getInstance(MayamClientTest.class);
		}
		finally
		{
			// Cleanly shutdown
			injector.getInstance(ShutdownManager.class).shutdown();
		}

	}	
	
	@Inject
	private MayamClient mayamClient;
	
	public MayamClientTest(){
		
	}
	
}
