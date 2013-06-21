package com.mediasmiths.mayam.retrying;

import com.google.inject.Injector;
import com.mediasmiths.mayam.veneer.TasksClientVeneer;
import com.mediasmiths.std.guice.apploader.BasicSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;
import org.junit.Ignore;

@Ignore //not a junit test
public class TasksWSRetryMethodInterceptorTest
{
	public static void main(String[] args) throws Exception
	{
		Injector injector = GuiceInjectorBootstrap.createInjector(new PropertyFile(), new BasicSetup(new TasksWSRetryModule()));

		TasksClientVeneer v = injector.getInstance(TasksClientVeneer.class);
		
		v.testAlwaysThrowException();
		
	}

}
