package com.mediasmiths.foxtel.qc.model;

public enum JobResultType {
	error,warning,success;

	public static JobResultType fromString(String result) {
		
		String resultLower = result.toLowerCase();
		
		if(resultLower.equals("error")) return error;
		else if(resultLower.equals("warning")) return warning;
		else if(resultLower.equals("success")) return success;			
		else throw new IllegalArgumentException("Specified result was not valid");
	}
} 