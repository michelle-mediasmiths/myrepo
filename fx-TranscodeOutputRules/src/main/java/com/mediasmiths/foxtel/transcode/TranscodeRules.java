package com.mediasmiths.foxtel.transcode;

import com.google.inject.ImplementedBy;
import com.mediasmiths.foxtel.tc.rest.api.TCAudioType;

@ImplementedBy(com.mediasmiths.foxtel.transcode.TranscodeRulesImpl.class)
public interface TranscodeRules
{
	TCAudioType audioTypeForTranscode(boolean inputIsSD, boolean inputIsSurround, boolean outputIsSD);
	
}
