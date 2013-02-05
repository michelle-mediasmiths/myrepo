package com.mediasmiths.mayam;

/**
 * button events are seen as task create events, using a different enum than the task list types to keep some seperation between the two notions
 * 
 */
public enum MayamButtonType
{
	UNINGEST("w_uningest"),
	DELETE("w_delete"),
	EXPORT_MARKERS("w_export_markers"),
	PUBLICITY_PROXY("w_publ_proxy"),
	CAPTION_PROXY("w_cap_proxy"),
	COMPLIANCE_PROXY("w_compl_proxy"),
	UNPROTECT("w_unprotect"),
	PROTECTED("w_protect"),
	PREVIEW_STATUS("w_preview_status"),
	QC_PARALLEL_ALLOWED("w_qc_parallel");

	private String text;

	MayamButtonType(String text)
	{
		this.text = text;
	}

	public String getText()
	{
		return this.text;
	}

	public static MayamButtonType fromString(String text)
	{
		if (text != null)
		{
			for (MayamButtonType b : MayamButtonType.values())
			{
				if (text.equalsIgnoreCase(b.text))
				{
					return b;
				}
			}
		}
		return null;
	}
}
