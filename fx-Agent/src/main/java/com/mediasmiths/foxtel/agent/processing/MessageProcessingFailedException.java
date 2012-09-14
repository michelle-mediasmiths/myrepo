package com.mediasmiths.foxtel.agent.processing;

public class MessageProcessingFailedException extends Exception {

	private static final long serialVersionUID = 1L;
	private MessageProcessingFailureReason reason;
	
	public MessageProcessingFailedException(MessageProcessingFailureReason reason, Exception e){
		super(e);
		this.reason = reason;
	}
	
	public MessageProcessingFailedException(MessageProcessingFailureReason reason){
		super();
		this.reason=reason;
	}
	
	public MessageProcessingFailureReason getReason(){
		return this.reason;
	}

}
