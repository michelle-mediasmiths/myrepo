package com.mediasmiths.foxtel.tc.priorities;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;

import com.google.inject.Inject;

public class TranscodePriorities
{
	private final static Logger log = Logger.getLogger(TranscodePriorities.class);

	@Inject
	private TranscodePriorityConfiguration config;

	public Integer getPriorityForNewTranscodeJob(TranscodeJobType type, Date txDate){
		return getPriorityForTranscodeJob(type,txDate, DateTime.now().toDate(),0);
	}
	
	
	/**
	 * Returns the priority a given transcode job should have
	 * 
	 * @param type
	 *            - The type of transcode; caption\tx\publicity etc
	 * @param txDate
	 *            - The first tx date of the relevant asset
	 * @param created
	 *            - The time the transcode job was created, used when queued time is to be accounted for, for new tasks use the current date
	 * @return
	 */
	public Integer getPriorityForTranscodeJob(TranscodeJobType type, Date txDate, Date created, Integer currentPriority)
	{

		log.debug(String.format(
				"Transcode priority requsted for job of type %s tx date %s job created %s current priority %s",
				type.getText(),
				txDate,
				created,
				currentPriority));

		// check if priority should event be change (once something is manually escalated we dont want to go changing its prority again)
		if (config.getManualInterventionProirities().contains(currentPriority))
		{
			log.debug("Current priority is manual intervention, will not change");
			return currentPriority;
		}

		// current priority is not a manual escalation priority

		if (type.equals(TranscodeJobType.CAPTION_PROXY) || type.equals(TranscodeJobType.TX))
		{

			// check if txDate is in the past
			boolean txDatePassed = isInPast(txDate);

			if (txDatePassed)
			{
				// tx date is passed return either highest or lowest priority for the given job type
				return priorityForTxDatePassedJob(type, txDate);
			}

			// tx date is in the future

			if (txDate == null)
			{
				log.trace("tx date null assuming far in the future");
				return config.getLowestPriorityForJobType(type);
			}

		}
		
		switch (type)
		{

			case CAPTION_PROXY:
				return getCaptionProxyPriority(txDate, created);
			case COMPLIANCE_PROXY:
				return getComplianceProxyPriority(txDate, created);
			case PUBLICITY_PROXY:
				return getPublicityProxyPriority(txDate, created);
			case TX:
				return getTxPriority(txDate, created);
			default:
				log.fatal("Default case on enum switch statement for TranscodeJobType!");
				break;
		}

		return 1;
	}

	/**
	 * 
	 * If content has passed first tx date and target date is no more than ${tc.past.tx.recent.threshold} milliseconds in the past job will get the highest priority for that destination
	 * 
	 * @param type
	 * @param txDate
	 * @return
	 */
	private Integer priorityForTxDatePassedJob(TranscodeJobType type, Date txDate)
	{
		log.trace("tx date has passed");
		DateTime txDateTime = new DateTime(txDate);
		Duration duration = new Duration(txDateTime, new Instant());
		long difference = duration.getMillis();

		if (difference < config.getPastTxPriorityThreshold())
		{
			log.trace("returning highest priority for job type");
			return config.getHighestPriorityForJobType(type); //New content being added to the transcode queue should go to the highest priority level for that destination if the target date is no more than 7 days in the past.
		}
		else
		{
			log.trace("returning lowest priority for job type");
		 	return config.getLowestPriorityForJobType(type); //Else, content goes to the lowest priority queue for that destination.
		}
	}

	private boolean isInPast(Date txDate)
	{

		if (txDate == null)
		{
			log.trace("tx date is in null, will assume it is a long time from now");
			return false;
		}

		DateTime txDateTime = new DateTime(txDate);
		
		if (txDateTime.isBeforeNow())
		{
			log.trace("tx date passed");
			return true;
		}

		return false;
	}

	private Integer getTxPriority(Date txDate, Date created)
	{
		List<PriorityRule> rules = config.getTxRules();
		return processAscendingRules(txDate, TranscodeJobType.TX, rules);
	}
	
	private Integer getCaptionProxyPriority(Date txDate, Date created)
	{
		List<PriorityRule> rules = config.getCaptionRules();
		return processAscendingRules(txDate, TranscodeJobType.CAPTION_PROXY, rules);
	}

	private Integer getPublicityProxyPriority(Date txDate, Date created)
	{
		List<PriorityRule> rules = config.getPublicityRules();
		return processDescendingRules(created, TranscodeJobType.PUBLICITY_PROXY, rules);
	}

	private Integer getComplianceProxyPriority(Date txDate, Date created)
	{
		List<PriorityRule> rules = config.getComplianceRules();
		return processDescendingRules(created, TranscodeJobType.COMPLIANCE_PROXY, rules);
	}

	private Integer processAscendingRules(Date date, TranscodeJobType type, List<PriorityRule> rules){
		
		DateTime txDateTime = new DateTime(date);
		Duration duration = new Duration(new Instant(),txDateTime);
		long difference = duration.getMillis();
		
		for (PriorityRule priorityRule : rules)
		{
			if(difference < priorityRule.getThreshold().longValue()){
				return priorityRule.getPriority();
			}
		}
		log.trace("no rules matched, using lowest priority");
		return config.getLowestPriorityForJobType(type);
	}
	
	private Integer processDescendingRules(Date date, TranscodeJobType type, List<PriorityRule> rules){
		
		DateTime createdDateTime = new DateTime(date);
		Duration duration = new Duration(createdDateTime, new Instant());
		long difference = duration.getMillis();
		
		for (PriorityRule priorityRule : rules)
		{
			if(difference > priorityRule.getThreshold().longValue()){
				return priorityRule.getPriority();
			}
		}
		log.trace("no rules matched, using lowest priority");
		return config.getLowestPriorityForJobType(type);
	}

}
