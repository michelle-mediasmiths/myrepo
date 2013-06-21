package com.mediasmiths.foxtel.wf.adapter.mule;

import com.mediasmiths.foxtel.wf.adapter.model.InvokeIntalioQCFlow;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * May be used to create proxy clients for invoking intalio workflows via mule
 *
 */

@Path("/")
public interface IntalioInvokationService
{
	@POST
	@Path("qc")
	public void invokeAutoQC(InvokeIntalioQCFlow request);
	
}
