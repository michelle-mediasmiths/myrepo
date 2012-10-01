package com.mediasmiths.foxtel.qc;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QCJobStatus {
	
	public enum StatusType {
		complete,processing,stopped,stopping,waiting;
		
		public static StatusType fromString(String status){
			if(status.equals("waiting")) return waiting;
			else if(status.equals("processing")) return processing;
			else if(status.equals("complete")) return complete;
			else if(status.equals("stopping")) return stopping;
			else if(status.equals("stopped")) return stopped;
			else throw new IllegalArgumentException("Specified status was not valid");
		}
		
	}

	private QCJobIdentifier ident;

	private BigInteger progress;

	private QCJobStatus.StatusType status;

	public QCJobStatus(QCJobIdentifier ident, String status, BigInteger progress) {
		this.ident=ident;
		this.status = StatusType.fromString(status);
		this.progress=progress;
	}

	public QCJobIdentifier getIdent() {
		return ident;
	}
	public BigInteger getProgress() {
		return progress;
	}	
	public QCJobStatus.StatusType getStatus() {
		return status;
	}
	
	public void setIdent(QCJobIdentifier ident) {
		this.ident = ident;
	}

	public void setProgress(BigInteger progress) {
		this.progress = progress;
	}

	public void setStatus(QCJobStatus.StatusType status) {
		this.status = status;
	}
	
}
