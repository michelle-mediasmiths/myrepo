package com.medismiths.foxtel.mpa;

import com.mediasmiths.foxtel.generated.MaterialExchange.Material;
import com.mediasmiths.foxtel.generated.MaterialExchange.Material.Title;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;

public class Util {

	private Util(){
		//hide utility class constructor
	}
	
	public static MaterialType getMaterialTypeForMaterial(Material material) {

		if (isProgramme(material)) {
			return material.getTitle().getProgrammeMaterial();
		} else {
			return material.getTitle().getMarketingMaterial();
		}
	}
	
	public static boolean isProgramme(Material material){
		return isProgramme(material.getTitle());
	}
	
	public static boolean isProgramme(Title title){
		return title.getProgrammeMaterial() != null;
	}

}
