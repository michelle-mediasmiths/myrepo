package com.mediasmiths.mq;

import com.mayam.wf.mq.MqDestination;

public class MediasmithsDestinations {
	public static final MqDestination TASKS = MqDestination.of("queue://mediasmiths.tasks");
	public static final MqDestination ASSETS = MqDestination.of("queue://mediasmiths.assets");
}
