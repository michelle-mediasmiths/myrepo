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
	
	public void DiskUsageEventJobScheduleur()
	{
		try {
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
		} 
	    catch (SchedulerException e) {
	    	logger.warn("Scheduler Exception thrown while creating daily disk usage eventing job", e);
			e.printStackTrace();
		}
	}
}
