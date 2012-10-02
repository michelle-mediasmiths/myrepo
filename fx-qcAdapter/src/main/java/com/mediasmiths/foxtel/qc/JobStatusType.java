package com.mediasmiths.foxtel.qc;

public enum JobStatusType {
	complete, processing, stopped, stopping, waiting;

	public static JobStatusType fromString(String status) {
		if (status.equals("waiting"))
			return waiting;
		else if (status.equals("processing"))
			return processing;
		else if (status.equals("complete"))
			return complete;
		else if (status.equals("stopping"))
			return stopping;
		else if (status.equals("stopped"))
			return stopped;
		else
			throw new IllegalArgumentException("Specified status was not valid");
	}

}
