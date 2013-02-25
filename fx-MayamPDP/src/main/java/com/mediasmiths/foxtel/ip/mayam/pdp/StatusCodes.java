package com.mediasmiths.foxtel.ip.mayam.pdp;

public enum StatusCodes
{
	OK("ok"), ERROR("error"), CONFIRM("confirm"), AUTH_FAIL("auth_fail");

	private String text;

	StatusCodes(String text) {
		this.text = text;
	}

	public String toString() {
		return this.text;
	}
}
