package com.mediasmiths.mayam;

public enum MayamAssetType {
	TITLE("SER"),
	MATERIAL("ITEM"),
	PACKAGE("PACK");
	
	  private String text;

	  MayamAssetType(String text) {
	    this.text = text;
	  }

	  public String getText() {
	    return this.text;
	  }

	  public static MayamAssetType fromString(String text) {
	    if (text != null) {
	      for (MayamAssetType b : MayamAssetType.values()) {
	        if (text.equalsIgnoreCase(b.text)) {
	          return b;
	        }
	      }
	    }
	    return null;
	  }
}


