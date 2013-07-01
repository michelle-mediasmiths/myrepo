package com.mediasmiths.stdEvents.jobs;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class DiskUsageEventJobScheduler {

	private static final transient Logger logger = Logger.getLogger(DiskUsageEventJobScheduler.class);
	
	public DiskUsageEventJobScheduler()
	{
		try {
			logger.info("Setting up daily disk usage job scheduler");
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			
		    JobDetail job = newJob(DiskUsageJob.class)
		        .withIdentity("dailyDiskUsageJob", "diskUsageEvent")
		        .build();
	
		    Trigger trigger = newTrigger()
		        .withIdentity("dailyTrigger", "diskUsageEvent")
		        .startNow()
		        .withSchedule(simpleSchedule()
		                .withIntervalInHours(24)
		                .repeatForever())            
		        .build();

			scheduler.scheduleJob(job, trigger);
			scheduler.start();
			logger.info("Scheduler Job Started");
		} 
	    catch (SchedulerException e) {
	    	logger.warn("Scheduler Exception thrown while creating daily disk usage eventing job", e);
			e.printStackTrace();
		}
	}
}
