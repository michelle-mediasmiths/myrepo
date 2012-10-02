package com.mediasmiths.foxtel.qc;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QCJobStatus {
	
	private QCJobIdentifier ident;

	private BigInteger progress;

	private JobStatusType status;

	public QCJobStatus(){
		
	}
	
	public QCJobStatus(QCJobIdentifier ident, String status, BigInteger progress) {
		this.ident=ident;
		this.status = JobStatusType.fromString(status);
		this.progress=progress;
	}

	public QCJobIdentifier getIdent() {
		return ident;
	}
	public BigInteger getProgress() {
		return progress;
	}	
	public JobStatusType getStatus() {
		return status;
	}
	
	public void setIdent(QCJobIdentifier ident) {
		this.ident = ident;
	}

	public void setProgress(BigInteger progress) {
		this.progress = progress;
	}

	public void setStatus(JobStatusType status) {
		this.status = status;
	}
	
}
