package com.mediasmiths.foxtel.ip.mayam.pdp;

public enum PDPAttributes {
	OP_STAT("OP_STAT"),
	FAILURE_CODE("FAILURE_CODE"),
	ERROR_MSG("ERROR_MSG"),
	LOGGING("LogMessage");
	
	private String text;

	PDPAttributes(String text) {
		this.text = text;
	}

	public String toString() {
		return this.text;
	}
}
