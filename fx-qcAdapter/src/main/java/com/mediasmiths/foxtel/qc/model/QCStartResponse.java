package com.mediasmiths.foxtel.qc.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QCStartResponse {

	private QCStartStatus status = QCStartStatus.UNSET;
	private QCJobIdentifier qcIdentifier;
	
	public QCStartResponse(){
		
	}
	
	public QCStartResponse(QCStartStatus status){
		this.status=status;
	}
	
	@XmlElement(name="identifier")
	public QCJobIdentifier getQcIdentifier() {
		return qcIdentifier;
	}
	public void setQcIdentifier(QCJobIdentifier qcIdentifier) {
		this.qcIdentifier = qcIdentifier;
	}
	
	@XmlElement(name="status")
	public QCStartStatus getStatus() {
		return status;
	}
	
	
}
