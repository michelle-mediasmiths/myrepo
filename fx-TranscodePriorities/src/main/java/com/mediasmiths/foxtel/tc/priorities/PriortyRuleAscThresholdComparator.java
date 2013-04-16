package com.mediasmiths.foxtel.tc.priorities;

import java.util.Comparator;

public class PriortyRuleAscThresholdComparator implements Comparator<PriorityRule>
{
	@Override
	public int compare(PriorityRule o1, PriorityRule o2)
	{
		return o1.getThreshold().compareTo(o2.getThreshold());
	}
}