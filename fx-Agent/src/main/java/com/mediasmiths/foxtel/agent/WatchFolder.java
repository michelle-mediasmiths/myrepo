package com.mediasmiths.foxtel.agent;

public class WatchFolder {

	private String source; //location to watch for files in
	private String delivery; //delivery location (if applicable to current agent)
	private boolean isAO;
	
	public boolean isAO() {
		return isAO;
	}
	public void setAO(boolean isAO) {
		this.isAO = isAO;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getDelivery() {
		return delivery;
	}
	public void setDelivery(String delivery) {
		this.delivery = delivery;
	}
	
	public WatchFolder(String source){
		setSource(source);
	}
	
	
}
