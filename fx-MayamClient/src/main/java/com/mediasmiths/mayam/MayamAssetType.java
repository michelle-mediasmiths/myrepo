package com.mediasmiths.mayam;

import com.mayam.wf.attributes.shared.type.AssetType;

public enum MayamAssetType {
	TITLE("EPISODE"),
	MATERIAL("ITEM"),
	PACKAGE("PACKAGE");
	
	  private String text;

	  MayamAssetType(String text) {
	    this.text = text;
	  }

	  public String getText() {
	    return this.text;
	  }

	  public AssetType getAssetType() {
		  return AssetType.valueOf(this.text);
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


