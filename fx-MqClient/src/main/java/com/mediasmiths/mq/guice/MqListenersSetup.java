package com.mediasmiths.mq.guice;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mayam.wf.attributes.server.AttributesModule;
import com.mayam.wf.mq.MqModule;
import com.mediasmiths.foxtel.channels.config.ChannelConfigModule;
import com.mediasmiths.foxtel.ip.event.guice.EventServiceModule;
import com.mediasmiths.foxtel.tc.priorities.guice.TranscodePrioritiesModule;
import com.mediasmiths.mayam.guice.MayamClientModule;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.io.PropertyFile;

import java.util.List;

public class MqListenersSetup implements GuiceSetup {

	@Override
	public void registerModules(List<Module> modules, PropertyFile config) {
		modules.add(new EventServiceModule());
		modules.add(new MqListenersModule());	
		modules.add(new MayamClientModule(true));
		modules.add(new AttributesModule());
		modules.add(new JaxModule());
		modules.add(new ChannelConfigModule());
		modules.add(new MqModule("fxMqListners"));
		modules.add(new TranscodePrioritiesModule());
	}

	@Override
	public void injectorCreated(Injector injector) {
	}

}
