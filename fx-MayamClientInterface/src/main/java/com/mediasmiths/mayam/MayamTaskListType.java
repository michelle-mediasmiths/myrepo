package com.mediasmiths.mayam;

public enum MayamTaskListType {
	INGEST("ingest"),
	INGEST_FAILURE("ingest failure"),
	MY_WORKLIST("my worklist"),
	QC_VIEW("qc"),
	QC_WARNING("qc"),
	PREVIEW("preview"),
	PREVIEW_FURTHER_ANALYSIS("preview"),
	COMPLIANCE_LOGGING("comp_logging"),
	COMPLIANCE_EDIT("comp_edit"),
	UNMATCHED_MEDIA("unmatched"),
	FIX_STITCH_EDIT("fixstitch"),
	SEGMENTATION("segmentation"),
	TX_DELIVERY("tx_delivery"),
	TX_DELIVERY_FAILURE("tx_delivery"),
	PURGE_CANDIDATE_LIST("purge candidate list"),
	GENERIC_TASK_ERROR("generic task error"),
	EXTENDED_PUBLISHING("extended publishing"),
	THIRD_PARTY("third party"),
	ASSOCIATED_TASKS("associated tasks"), 
	PURGE_BY_BMS("Purge By BMS");
	
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
