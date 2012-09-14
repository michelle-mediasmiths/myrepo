package com.mediasmiths.foxtel.agent;

import java.io.File;

public class MessageEnvelope<T> {

	private final File file;
	private final T message;
		
	public MessageEnvelope(File file, T message){
		this.file=file;
		this.message=message;
	}
	
	public File getFile() {
		return file;
	}

	public T getMessage() {
		return message;
	}
	
}
