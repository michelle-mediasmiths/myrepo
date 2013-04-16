package com.mediasmiths.foxtel.tc.priorities;

public enum TranscodeJobType
{
	COMPLIANCE_PROXY("Compliance Proxy"), PUBLICITY_PROXY("Publicity Proxy"), CAPTION_PROXY("Caption Proxy"), TX("TX");

	private String text;

	TranscodeJobType(String text)
	{
		this.text = text;
	}

	public String getText()
	{
		return text;
	}
}
