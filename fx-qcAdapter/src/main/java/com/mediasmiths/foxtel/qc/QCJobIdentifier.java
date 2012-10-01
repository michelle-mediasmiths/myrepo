package com.mediasmiths.foxtel.qc;

public class QCJobIdentifier {

	private String identifier;
	private String profile;

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public QCJobIdentifier() {

	}

	public QCJobIdentifier(String jobName) {
		this.identifier = jobName;
	}

	public QCJobIdentifier(String jobName, String profile) {
		this.identifier = jobName;
		this.profile = profile;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	@Override
	public String toString(){
		return String.format("Identifier : {%s} , Profile : {%s}", identifier,profile);
	}
	
}
