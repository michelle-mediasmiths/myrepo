import java.util.List;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.mediasmiths.foxtel.carbonwfs.WfsClient;
import com.mediasmiths.foxtel.carbonwfs.WfsClientException;
import com.mediasmiths.foxtel.carbonwfs.guice.WfsClientModule;
import com.mediasmiths.std.guice.apploader.GuiceSetup;
import com.mediasmiths.std.guice.apploader.impl.GuiceInjectorBootstrap;
import com.mediasmiths.std.io.PropertyFile;

public class TestBuildTemplateXML
{

	@Test
	public void testbuildemplatexml() throws WfsClientException
	{

		WfsClient wfsClient = new WfsClient();

		wfsClient.buildWorkFlowForSimpleTranscode("c:\\my\\file.mxf", "job title", UUID.randomUUID());

	}

	@Test
	@Ignore
	public void testCreateJob() throws WfsClientException
	{

		final Injector injector = GuiceInjectorBootstrap.createInjector(new GuiceSetup()
		{

			@Override
			public void registerModules(List<Module> arg0, PropertyFile arg1)
			{
				arg0.add(new WfsClientModule());

			}

			@Override
			public void injectorCreated(Injector arg0)
			{
				// TODO Auto-generated method stub

			}
		});

		WfsClient client = injector.getInstance(WfsClient.class);
		client.transcode("f:\\tcinput\\test.mxf", "f:\\tcoutput\\testing", UUID.fromString("8b9cbc5a-2070-4e13-9936-e15c8682db8a"), "test job title");
	}
}