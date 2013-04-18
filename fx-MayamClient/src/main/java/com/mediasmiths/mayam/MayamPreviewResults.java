package com.mediasmiths.mayam;

public class MayamPreviewResults {
	public static final String PREVIEW_FAIL = "fail";
	public static final String STITCH_EDIT_AND_REORDER = "stitchr";
	public static final String STITCH_EDIT_REQUIRED = "stitch";
	public static final String FIX_EDIT_AND_REORDER = "fixr";
	public static final String PREVIEW_PASSED_BUT_REORDER = "passr";
	public static final String PREVIEW_PASSED = "pass";
	public static final String PREVIEW_NOT_DONE = "pvnd";
	public static final String FIX_EDIT_REQUIRED = "fix";
	public static final int ESCALATION_LEVEL_1 = 1; //FurtherAnalysis
	public static final int ESCALATION_LEVEL_2 = 2; //Classification
	
	public static boolean isPreviewPass(String previewStatus){
		return previewStatus != null && (previewStatus.equals(PREVIEW_PASSED) || previewStatus.equals(PREVIEW_PASSED_BUT_REORDER)); 
	}
}
