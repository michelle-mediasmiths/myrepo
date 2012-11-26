package com.mediasmiths.foxtel.agent.validation;

public class ConfigValidationFailureException extends Exception {

	private static final long serialVersionUID = 1L;

	public ConfigValidationFailureException() {
		super();
	}
	
	public ConfigValidationFailureException(String message){
		super(message);
	}

	public ConfigValidationFailureException(String message, Throwable cause) {
		super(message, cause);
	}

}
