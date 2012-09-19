package com.mediasmiths.mayam;

public interface AlertInterface {
	public void sendAlert(String destination, String subject, Object contents);
}
