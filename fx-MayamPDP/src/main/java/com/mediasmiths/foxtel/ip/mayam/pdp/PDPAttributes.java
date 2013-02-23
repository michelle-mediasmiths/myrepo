package com.mediasmiths.foxtel.ip.mayam.pdp;

public enum PDPAttributes {
	STATUS("OperationStatus"),
	FAILURE_CODE("ReasonForFailure"),
	UI_MESSAGE("DisplayMessage"),
	LOGGING("LogMessage");
	
	private String text;

	PDPAttributes(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}
}
