package com.mediasmiths.foxtel.generated.mediaexchange;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TestProgrammeSerialization 
{
	Programme programme;
	String programmeString;
	
	@Before
	public void setup() throws IOException, JAXBException
	{
		programmeString = FileUtils.readFileToString(new File("src/test/resources/ProgrammeExample.xml"));
		programme = unmarshall(programmeString);
	}
	
	@Test
	public void testMarshalledProgramme() throws JAXBException
	{
		System.out.println(marshall());
	}
	
	@Test
	public void testMarshallAndValidate() throws JAXBException, SAXException, IOException
	{
		JAXBContext context = getContext();
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

		Schema schema = factory.newSchema(TestProgrammeSerialization.class.getClassLoader().getResource("MediaExchange_V1.2.xsd"));
		marshaller.setSchema(schema);

		JAXBSource source = new JAXBSource(context, programme);
		Validator validator = schema.newValidator();
		validator.setErrorHandler(new MyErrorHandler());
		validator.validate(source);

		StringWriter output = new StringWriter();
		marshaller.marshal(programme, output);

		System.out.println(output.toString());
	}
	
	
	private String marshall() throws JAXBException
	{
		StringWriter output = new StringWriter();
		Marshaller marshaller = getContext().createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(programme, output);

		return output.toString();
	}
	
	private Programme unmarshall(String objectString) throws JAXBException
	{
		Unmarshaller unmarshall = getContext().createUnmarshaller();
		final StringReader reader = new StringReader(objectString);
		return (Programme)unmarshall.unmarshal(reader);
	}

	private JAXBContext getContext() throws JAXBException
	{
		return JAXBContext.newInstance("com.mediasmiths.foxtel.generated.mediaexchange");
	}
	
	static class MyErrorHandler implements ErrorHandler
	{
		@Override
		public void warning(SAXParseException exception) throws SAXException
		{
			System.out.println("\nWARNING");
			exception.printStackTrace();
		}

		@Override
		public void error(SAXParseException exception) throws SAXException
		{
			System.out.println("\nERROR");
			exception.printStackTrace();
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException
		{
			System.out.println("\nFATAL ERROR");
			exception.printStackTrace();
		}

	}
}