package com.mediasmiths.foxtel.agent;

public class MessageProcessingFailedException extends Exception {

	private static final long serialVersionUID = 1L;
	private MesageProcessingFailureReason reason;
	
	public MessageProcessingFailedException(MesageProcessingFailureReason reason, Exception e){
		super(e);
		this.reason = reason;
	}
	
	public MessageProcessingFailedException(MesageProcessingFailureReason reason){
		super();
		this.reason=reason;
	}
	
	public MesageProcessingFailureReason getReason(){
		return this.reason;
	}

}
