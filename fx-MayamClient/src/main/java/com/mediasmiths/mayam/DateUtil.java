package com.mediasmiths.mayam;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


public class DateUtil
{
	final DatatypeFactory dataTypeFactory;
	
	public DateUtil() throws DatatypeConfigurationException{
		dataTypeFactory = DatatypeFactory.newInstance();
	}

	
	public XMLGregorianCalendar fromDate(Date from){
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(from);
		return dataTypeFactory.newXMLGregorianCalendar(c);
		
	}
	
	public  Date fromXMLGregorianCalendar(XMLGregorianCalendar from){
		
		return from.toGregorianCalendar().getTime();
	}
}
