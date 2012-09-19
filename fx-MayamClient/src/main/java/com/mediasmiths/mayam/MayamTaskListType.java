package com.mediasmiths.mayam;

public enum MayamTaskListType {
	INGEST("ingest"),
	INGEST_FAILURE("ingest failure"),
	MY_WORKLIST("my worklist"),
	QC_VIEW("QC view"),
	QC_WARNING("QC warning"),
	PREVIEW("preview"),
	PREVIEW_FURTHER_ANALYSIS("preview further analysis"),
	COMPLIANCE_LOGGING("compliance logging"),
	COMPLIANCE_EDIT("compliance edit"),
	UNMATCHED_MEDIA("unmatched media"),
	FIX_STITCH_EDIT("fix/stitch edit"),
	SEGMENTATION("segmentation"),
	TX_DELIVERY("Tx delivery"),
	TX_DELIVERY_FAILURE("Tx delivery failure"),
	PURGE_CANDIDATE_LIST("purge candidate list"),
	GENERIC_TASK_ERROR("generic task error"),
	EXTENDED_PUBLISHING("extended publishing"),
	THIRD_PARTY("third party"),
	ASSOCIATED_TASKS("associated tasks");
	
	  private String text;

	  MayamTaskListType(String text) {
	    this.text = text;
	  }

	  public String getText() {
	    return this.text;
	  }

	  public static MayamTaskListType fromString(String text) {
	    if (text != null) {
	      for (MayamTaskListType b : MayamTaskListType.values()) {
	        if (text.equalsIgnoreCase(b.text)) {
	          return b;
	        }
	      }
	    }
	    return null;
	  }
}
