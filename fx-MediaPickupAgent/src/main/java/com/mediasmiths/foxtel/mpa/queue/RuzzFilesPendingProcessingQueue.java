package com.mediasmiths.foxtel.mpa.queue;

import java.util.concurrent.LinkedBlockingQueue;

import com.google.inject.Singleton;

@Singleton
public class RuzzFilesPendingProcessingQueue extends LinkedBlockingQueue<String> {

	private static final long serialVersionUID = 1L;
	
}
