package com.mediasmiths.foxtel.ip.mayam.pdp;

public enum PDPAttributes {
	OP_STAT("op_stat"),
	ERROR_MSG("error_msg"),
	FORM_MSG("form_msg");
	
	private String text;

	PDPAttributes(String text) {
		this.text = text;
	}

	public String toString() {
		return this.text;
	}
}
