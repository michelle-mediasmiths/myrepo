package com.mediasmiths.mayam;

public class MayamClientException extends Exception{
	
	private static final long serialVersionUID = 1L;
	private final MayamClientErrorCode errorcode;
	
	public MayamClientErrorCode getErrorcode() {
		return errorcode;
	}

	public MayamClientException(MayamClientErrorCode errorcode){
		super(errorcode.toString());
		this.errorcode=errorcode;
	}

}
