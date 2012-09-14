package com.medismiths.foxtel.mpa.queue;

import java.util.concurrent.LinkedBlockingQueue;

import com.google.inject.Singleton;
import com.medismiths.foxtel.mpa.PendingImport;

@Singleton
public class PendingImportQueue  extends LinkedBlockingQueue<PendingImport> {

	private static final long serialVersionUID = 1L;

}
