package com.mediasmiths.mayam;

import com.mayam.wf.attributes.shared.Attribute;
import com.mayam.wf.attributes.shared.AttributeMap;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 * Logs when an update made doesn't result in all of the expected attributes
 */
public final class RaceLoggingInterceptor implements MethodInterceptor
{
	private final static Logger log = Logger.getLogger(RaceLoggingInterceptor.class);


	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable
	{
		AttributeMap requestedUpdate = (AttributeMap) invocation.getArguments()[0];
		AttributeMap updated = (AttributeMap) invocation.proceed();

		if (updated != null && requestedUpdate != null)
		{
			final Set<Attribute> attributesInUpdateRequest = requestedUpdate.getAttributeSet();

			for (Attribute a : attributesInUpdateRequest)
			{
				Object requested = requestedUpdate.getAttribute(a);
				Object returned = updated.getAttribute(a);

				if (!ObjectUtils.equals(requested, returned))
				{
					log.warn(String.format("Requested attribute %s of asset (or task for) %s (%s) update to %s but got back %s . Someone else may have updated this asset at the same time or the call made to this function did not have a sparse enough map",
					                       a,
					                       updated.getAttributeAsString(Attribute.HOUSE_ID),
					                       updated.getAttributeAsString(Attribute.ASSET_ID),
					                       requested,
					                       returned));
				}
			}
		}
		return updated;
	}
}
