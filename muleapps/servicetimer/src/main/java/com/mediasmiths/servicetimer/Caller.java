package com.mediasmiths.servicetimer;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class Caller extends QuartzJobBean {

	public static final transient Logger logger = Logger.getLogger(Caller.class);
	
	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
		
		logger.info("Service called");
	}

}
