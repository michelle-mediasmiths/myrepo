package com.mediasmiths.foxtel.placeholder.processing;

public class MessageProcessingFailedException extends Exception {

	private static final long serialVersionUID = 1L;
	private MesageProcessingFailureReason reason;
	
	public MessageProcessingFailedException(MesageProcessingFailureReason reason){
		this.reason = reason;
	}
	
	public MesageProcessingFailureReason getReason(){
		return this.reason;
	}

}
