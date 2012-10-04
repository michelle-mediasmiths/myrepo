import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.codec.EncoderException;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.jdom2.JDOMException;
import org.junit.Test;

import com.mediasmiths.foxtel.carbonwfs.Client;
import com.rhozet.rhozet_services_iwfcjmservices.IWfcJmServices;
import com.rhozet.rhozet_services_iwfcjmservices.JMServices;

public class foo
{

	@Test
	public void testservice() throws JDOMException, IOException, EncoderException
	{

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

		factory.getInInterceptors().add(new LoggingInInterceptor());
		factory.getOutInterceptors().add(new LoggingOutInterceptor());
		factory.setServiceClass(IWfcJmServices.class);
		factory.setAddress("http://127.0.0.1:9999/Rhozet.JobManager.JMServices/SOAP");
		IWfcJmServices services = (IWfcJmServices) factory.create();

		new Client(services).transcode(
				"C:/temp/foo.mxf",
				"C:/temp/fooout.mxf",
				UUID.fromString("8b9cbc5a-2070-4e13-9936-e15c8682db8a"),
				"TEST JOB");
	}

}
