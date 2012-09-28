package com.mediasmiths.foxtel.cerify;

import javax.xml.rpc.ServiceException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.tektronix.www.cerify.soap.client.CeriTalk_PortType;
import com.tektronix.www.cerify.soap.client.CeriTalk_ServiceLocator;

import static com.mediasmiths.foxtel.cerify.CerifyClientConfig.CERITALK_SERVER;

public class CerifyModule extends AbstractModule {

	@Override
	protected void configure() {
		
	}
	
	@Provides
	public CeriTalk_PortType getServiceLocator(@Named(CERITALK_SERVER) String server) throws ServiceException{
		
		CeriTalk_ServiceLocator serviceLocator = new CeriTalk_ServiceLocator();
		 serviceLocator.setCeriTalkSOAPEndpointAddress("http://" + server
                 + "/CeriTalk");
		 
		 return serviceLocator.getCeriTalkSOAP();
	}

}
