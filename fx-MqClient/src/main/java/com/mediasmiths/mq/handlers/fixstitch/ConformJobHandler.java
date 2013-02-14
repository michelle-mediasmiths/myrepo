package com.mediasmiths.mq.handlers.fixstitch;

import org.apache.log4j.Logger;

import com.mayam.wf.attributes.shared.type.Job;
import com.mayam.wf.attributes.shared.type.Job.JobStatus;
import com.mayam.wf.attributes.shared.type.Job.JobType;
import com.mediasmiths.mq.handlers.JobHandler;

public class ConformJobHandler extends JobHandler
{

	private final static Logger log = Logger.getLogger(ConformJobHandler.class);
	
	@Override
	public void process(Job jobMessage)
	{
		boolean isConform = jobMessage.getJobType().equals(JobType.CONFORM);
		boolean isFinished = jobMessage.getJobStatus().equals(JobStatus.FINISHED);
		
		String assetID = jobMessage.getAssetId();
		
		if(assetID == null){
			log.error("asset id is null in CONFORM job message, I cant do anything");
			return;
		}
		
		if(isConform && isFinished){
			log.info(String.format("a conform job has finished for assetID %s", assetID));
			
		}
	}

	@Override 
	public String getName()
	{
		return "Conform Job";
	}

}
