package com.mediasmiths.foxtel.ip.mayam.pdp;

public enum PDPAttributes {
	OP_STAT("OP_STAT"),
	ERROR_MSG("ERROR_MSG"),
	FORM_MSG("FORM_MSG");
	
	private String text;

	PDPAttributes(String text) {
		this.text = text;
	}

	public String toString() {
		return this.text;
	}
}
