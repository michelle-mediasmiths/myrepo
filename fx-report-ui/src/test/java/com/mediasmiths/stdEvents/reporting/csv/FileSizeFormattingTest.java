package com.mediasmiths.stdEvents.reporting.csv;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileSizeFormattingTest
{

	@Test
	public void testGetReportList()
	{
		long fileSize = 10573729792L;
		long fileSize1 = 10917224496L;
		long fileSize2 = 1032344L;
		long fileSize3 = 2097152L;
		long fileSize4 = 1073741824L;

		List fileList = new ArrayList();

		fileList.add(fileSize);
		fileList.add(fileSize1);
		fileList.add(fileSize2);
		fileList.add(fileSize3);
		fileList.add(fileSize4);

		for (Object file : fileList)
		{


		}

		//String displaySize = String.valueOf(fileSize/ FileUtils.ONE_GB);
		String displaySize1 = String.valueOf(fileSize2/ FileUtils.ONE_GB);
		String displaySize2 = String.valueOf(fileSize3/ FileUtils.ONE_GB);
		String displaySize3 = String.valueOf(fileSize4/ FileUtils.ONE_GB);


		if (fileSize3 >= FileUtils.ONE_GB)
		{
			String displaySize = String.valueOf(fileSize/ FileUtils.ONE_GB);
			System.out.println("\n");
			System.out.println(displaySize + " \tDISPLAY 1");

		}
		else
		{
			double file = (double) fileSize3;
			Double displaySize = (double) (file / ((double) FileUtils.ONE_GB));
			DecimalFormat twoDForm = new DecimalFormat("#.####");

			System.out.println("\n");
			System.out.println(file);
			System.out.println(FileUtils.ONE_GB);
			System.out.println(Double.valueOf(twoDForm.format(displaySize)) + " \tDISPLAY 1");

		}

//		System.out.println("\n");
//
//		System.out.println(displaySize1);
//		System.out.println(displaySize2);
//		System.out.println(displaySize3);
//		System.out.println("Testing filesize");
//		System.out.println("\n");

//		System.out.println(FileUtils.byteCountToDisplaySize(fileSize));
//		System.out.println(FileUtils.byteCountToDisplaySize(fileSize1));
//		System.out.println(FileUtils.byteCountToDisplaySize(fileSize2));
//		System.out.println(FileUtils.byteCountToDisplaySize(size));


	}
}
