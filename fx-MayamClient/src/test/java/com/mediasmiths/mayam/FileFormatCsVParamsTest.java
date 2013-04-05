package com.mediasmiths.mayam;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mediasmiths.mayam.FileFormatVerification.FileFormatTest;

public class FileFormatCsVParamsTest
{

	@Test
	public void testCSVParam()
	{

		String expected = "foo,bar";
		String actual = "bar";
		FileFormatVerification ffv = new FileFormatVerification();
		FileFormatVerification.FileFormatTest test = ffv.new FileFormatTest(expected, actual, "test csv");

		StringBuilder sb = new StringBuilder();
		boolean check = test.check(sb);
		assertEquals(true, check);

		actual = "foo";
		test = ffv.new FileFormatTest(expected, actual, "test csv");
		check = test.check(sb);
		assertEquals(true, check);

		actual = "somethingelse";
		test = ffv.new FileFormatTest(expected, actual, "test csv");
		check = test.check(sb);
		assertEquals(false, check);

		String expectInt = "10,101";
		Integer actualInt = Integer.valueOf(10);
		test = ffv.new FileFormatTest(expectInt, actualInt, "test csv");
		check = test.check(sb);
		assertEquals(true, check);
		
		actualInt = Integer.valueOf(100);
		test = ffv.new FileFormatTest(expectInt, actualInt, "test csv");
		check = test.check(sb);
		assertEquals(false, check);
		
		actualInt = Integer.valueOf(101);
		test = ffv.new FileFormatTest(expectInt, actualInt, "test csv");
		check = test.check(sb);
		assertEquals(true, check);
		
		expectInt = "101";
		actualInt = Integer.valueOf(101);
		test = ffv.new FileFormatTest(expectInt, actualInt, "test csv");
		check = test.check(sb);
		assertEquals(true, check);
		

	}

}
