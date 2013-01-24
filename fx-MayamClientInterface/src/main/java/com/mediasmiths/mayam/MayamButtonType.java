package com.mediasmiths.mayam;

/**
 * button events are seen as task create events, using a different enum than the task list types to keep some seperation between the two notions
 *
 */
public enum MayamButtonType {
	  UNINGEST("w_uningest");
	
	  private String text;

	  MayamButtonType(String text) {
	    this.text = text;
	  }

	  public String getText() {
	    return this.text;
	  }

	  public static MayamButtonType fromString(String text) {
	    if (text != null) {
	      for (MayamButtonType b : MayamButtonType.values()) {
	        if (text.equalsIgnoreCase(b.text)) {
	          return b;
	        }
	      }
	    }
	    return null;
	  }
}
