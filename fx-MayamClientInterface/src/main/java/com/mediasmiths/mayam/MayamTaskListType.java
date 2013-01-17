package com.mediasmiths.mayam;

public enum MayamTaskListType {
	INGEST("ingest"),
	MY_WORKLIST("my worklist"),
	QC_VIEW("qc"),
	PREVIEW("preview"),
	COMPLIANCE_LOGGING("comp_logging"),
	COMPLIANCE_EDIT("comp_edit"),
	UNMATCHED_MEDIA("unmatched"),
	FIX_STITCH_EDIT("fixstitch"),
	SEGMENTATION("segmentation"),
	TX_DELIVERY("tx_delivery"),
	PURGE_CANDIDATE_LIST("purge_cand"),
	GENERIC_TASK_ERROR("generic_error"),
	EXTENDED_PUBLISHING("export"),
	ASSOCIATED_TASKS("associated tasks"), 
	WFE_ERROR("wfe_error");
	
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
