package com.mediasmiths.foxtel.ip.utils.impl;

import com.mediasmiths.foxtel.ip.utils.Heartbeat;
import org.apache.log4j.Logger;

import javax.ws.rs.QueryParam;

/**
 * Stub Implementation of the heartbeat API.
 *
 * Author: Harmer
 */
public class HeartbeatStub  implements Heartbeat
{

	private static final Logger logger = Logger.getLogger(HeartbeatStub.class);

	boolean status = false;

	@Override
	public void beat()
	{
        logger.info("Heart beat @" + System.currentTimeMillis());
	}

	@Override
	public void setStatus(@QueryParam("status") final boolean status)
	{
		logger.info("set status - " + status);
	}

	@Override
	public boolean getStatus()
	{
		return status;
	}
}
