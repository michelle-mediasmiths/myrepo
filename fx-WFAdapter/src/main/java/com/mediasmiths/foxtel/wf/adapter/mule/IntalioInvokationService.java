package com.mediasmiths.foxtel.wf.adapter.mule;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.mediasmiths.foxtel.wf.adapter.model.InvokeIntalioQCFlow;

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
