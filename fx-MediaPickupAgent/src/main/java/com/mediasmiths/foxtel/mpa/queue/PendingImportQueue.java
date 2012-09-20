package com.mediasmiths.foxtel.mpa.queue;

import java.util.concurrent.LinkedBlockingQueue;

import com.google.inject.Singleton;
import com.mediasmiths.foxtel.mpa.PendingImport;

@Singleton
public class PendingImportQueue  extends LinkedBlockingQueue<PendingImport> {

	private static final long serialVersionUID = 1L;

}
