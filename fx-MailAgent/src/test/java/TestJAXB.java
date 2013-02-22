/*import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.foxtel.ip.mail.generated.MailTemplate;

public class TestJAXB
{
	private static final String NOTIFICATION = "<MailTemplate xmlns=\"http://schema.fabric.bbc.co.uk/adapter/common/2010-10\">\n"
			+ "  <jobStatuses>\n"
			+ "    <jobStatus estimatedCompletion=\"10\" jobIdentifier=\"54e79fe5-e268-48a7-aea3-9e8dd5c4017c\" status=\"failed\">\n"
			+ "      <xmlBlock>ms-1045</xmlBlock>\n" + "    </jobStatus>\n" + "  </jobStatuses>\n" + "</MailTemplate>";

	public static void main(String[] args) throws Exception
	{
		JAXBContext context = JAXBContext.newInstance(new Class[] { MailTemplate.class });

		Unmarshaller unmarshaller = context.createUnmarshaller();

		MailTemplate notification = (MailTemplate) unmarshaller.unmarshal(new StringReader(NOTIFICATION));

		Marshaller marshaller = context.createMarshaller();

		{
			StringWriter sw = new StringWriter();
			marshaller.marshal(notification, sw);

			System.out.println(sw);
		}
	}

}*/
