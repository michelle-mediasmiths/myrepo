package com.mediasmiths.foxtel.qc;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QCStartResponse {

	private QCStartStatus status = QCStartStatus.UNSET;
	private QCIdentifier qcIdentifier;
	
	public QCStartResponse(){
		
	}
	
	public QCStartResponse(QCStartStatus status){
		this.status=status;
	}
	
	@XmlElement(name="identifier")
	public QCIdentifier getQcIdentifier() {
		return qcIdentifier;
	}
	public void setQcIdentifier(QCIdentifier qcIdentifier) {
		this.qcIdentifier = qcIdentifier;
	}
	
	@XmlElement(name="status")
	public QCStartStatus getStatus() {
		return status;
	}
	
	
}
