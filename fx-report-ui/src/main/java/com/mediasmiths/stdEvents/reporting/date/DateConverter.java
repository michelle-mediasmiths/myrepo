package com.mediasmiths.stdEvents.reporting.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateConverter
{
	public void dateConverter(Long longDate, Date start, Date end)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(longDate);
		Date date = cal.getTime();
		
		if ((date.after(start)) && (date.before(end)))
		{
			System.out.println("Date is within range");
		}
		else {
			System.out.println("Date is outside range");
		}
	}
	
	public Date stringToDate (String stringDate) throws ParseException
	{
		DateFormat df = DateFormat.getInstance();
		SimpleDateFormat sdf = (SimpleDateFormat) df;
		sdf.applyPattern("dd MMM yyyy");
		return df.parse(stringDate);
	}
	
	public static void withinRange(Long currentDate, Long start, Long end)
	{
		if ((currentDate>start) && (currentDate<end))
		{
			System.out.println("Date is within range");
		}
		else 
			System.out.println("Out of range");
	}
	
	public static void main (String [] args)
	{
		Calendar calStart = Calendar.getInstance();
		calStart.set(calStart.DATE, 1);
		calStart.set(calStart.MONTH, calStart.JANUARY);
		calStart.set(calStart.YEAR, 2013);
		
		Long millisStart = calStart.getTimeInMillis();	
		System.out.println("Start: " + millisStart);
		
		Calendar calEnd = Calendar.getInstance();
		calEnd.set(calEnd.DATE, 30);
		calEnd.set(calEnd.MONTH, calEnd.JANUARY);
		calEnd.set(calEnd.YEAR, 2013);
		
		Long millisEnd = calEnd.getTimeInMillis();
		System.out.println("End: " + millisEnd);
		
		Calendar calCurrent = Calendar.getInstance();
		calCurrent.set(calCurrent.DATE, 11);
		calCurrent.set(calCurrent.MONTH, calCurrent.JANUARY);
		calCurrent.set(calCurrent.YEAR, 2013);
		
		Long millisCurrent = calCurrent.getTimeInMillis();
		System.out.println("Current: " + millisCurrent);
		
		withinRange(millisCurrent, millisStart, millisEnd);
	}
}
