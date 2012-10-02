package com.mediasmiths.foxtel.cerify;

public class CerifyClientConfig {

	private CerifyClientConfig(){
		//hide utility class constructor
	}
	
	public final static String CERITALK_SERVER = "cerify.server";
	public final static String CERIFY_LOCATION_NAME = "cerify.location.name";
	
	//not user configured, url is provided at runtime
	public final static String CERIFY_LOCATION_URL = "cerify.location.url";
	
}
