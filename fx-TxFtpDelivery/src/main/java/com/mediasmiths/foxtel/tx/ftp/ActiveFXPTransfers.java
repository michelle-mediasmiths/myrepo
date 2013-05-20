package com.mediasmiths.foxtel.tx.ftp;

import java.util.HashMap;

public class ActiveFXPTransfers extends HashMap<Long,ActiveFXPTransfer>
{
	private static final long serialVersionUID = 1L;
	
	@Override
	public synchronized ActiveFXPTransfer put(Long taskID, ActiveFXPTransfer e)
	{	
		ActiveFXPTransfer ret = super.put(taskID,e);
		Thread t = new Thread(e);
		t.setDaemon(true);
		t.start();
		return ret;
	}
}
