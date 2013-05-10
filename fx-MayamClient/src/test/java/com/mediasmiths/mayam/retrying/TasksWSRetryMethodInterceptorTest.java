package com.mediasmiths.mayam.retrying;

import java.net.ConnectException;
import java.net.SocketException;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mayam.wf.ws.client.TasksClient;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;
import com.mediasmiths.std.guice.apploader.BasicSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;
import com.mediasmiths.std.util.Logging;

public class TasksWSRetryMethodInterceptorTest
{
	public static void main(String[] args) throws Exception
	{
		Injector injector = GuiceInjectorBootstrap.createInjector(new PropertyFile(), new BasicSetup(new TasksWSRetryModule()));

		TasksClientVeneer v = injector.getInstance(TasksClientVeneer.class);
		
		v.testAlwaysThrowException();
		
	}

}
