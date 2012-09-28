package com.mediasmiths.foxtel.qc;

public class QCIdentifier {

	private String identifier;
	
	public QCIdentifier(){
		
	}
	
	public QCIdentifier(String ident){
		this.identifier=ident;
	}
	
	public String getIdentifier(){
		return this.identifier;
	}
	
}
