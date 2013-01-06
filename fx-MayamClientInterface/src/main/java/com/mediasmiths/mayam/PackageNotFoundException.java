package com.mediasmiths.mayam;

public class PackageNotFoundException extends MayamClientException
{

	private static final long serialVersionUID = 1L;
	
	public PackageNotFoundException(Exception cause){
		super(MayamClientErrorCode.PACKAGE_FIND_FAILED,cause);
	}

	public PackageNotFoundException(){
		super(MayamClientErrorCode.PACKAGE_FIND_FAILED);
	}
}
