package com.mediasmiths.foxtel.agent;

import java.util.concurrent.LinkedBlockingQueue;

import com.google.inject.Singleton;

@Singleton
public class FilesPendingProcessingQueue extends LinkedBlockingQueue<String> {

	private static final long serialVersionUID = 1L;
	
}
