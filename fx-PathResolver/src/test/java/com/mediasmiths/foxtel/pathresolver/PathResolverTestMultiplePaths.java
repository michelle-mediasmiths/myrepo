package com.mediasmiths.foxtel.pathresolver;

import com.mediasmiths.foxtel.pathresolver.PathResolver.PathType;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PathResolverTestMultiplePaths
{	
	private static Logger logger = Logger.getLogger(PathResolverTestMultiplePaths.class);

	
	private final static String NIXPATH1 = "/storage/mam/hires01";
	private final static String NIXPATH2 = "/storage/mam/hires02";
	private final static String NIXPATH3 = "/storage/";
	private final static String NIXPATH4 = "/storage/mam/hires03";
	private final static String NIXPATH = String.format("%s,%s,%s,%s",NIXPATH4, NIXPATH3, NIXPATH2,NIXPATH1);
	
	private final static String WINPATH1 = "f:";
	private final static String WINPATH2 = "g:";
	private final static String WINPATH3 = "h:";
	private final static String WINPATH4 = "g:\\subfolderOfG";
	private final static String WINPATH =  String.format("%s,%s,%s,%s",WINPATH4,WINPATH3, WINPATH2,WINPATH1);
	
	private final static String UNCPATH1 = "\\\\foxtel\\hires01";
	private final static String UNCPATH2 = "\\\\foxtel\\hires02";
	private final static String UNCPATH3 = "\\\\foxtel\\";
	private final static String UNCPATH4 = "\\\\foxtel\\hires02\\subfolderOfG";
	private final static String UNCPATH =  String.format("%s,%s,%s,%s",UNCPATH4, UNCPATH3, UNCPATH2,UNCPATH1);
	
	
	private final static String USERFTPPREFIX = "ftp://user:pass@host.mediasmiths.com:2121";
	
	private final static String FTPPATH1 = "//some/path/to/storage/hi1";
	private final static String FTPPATH2 = "//some/path/to/storage/hi2";
	private final static String FTPPATH3 = "//some/path/";
	private final static String FTPPATH4 = "//some/path/to/storage/hi3";
	private final static String FTPPATH = String.format("%s,%s,%s,%s",FTPPATH4, FTPPATH3, FTPPATH2,FTPPATH1);
	
	PathResolver toTest;

	@Before
	public void before()
	{

		toTest = new PathResolver(NIXPATH, WINPATH, UNCPATH,FTPPATH);

	}

	@Test
	public void testNixToWin1() throws UnknownPathException
	{
		logger.info("Starting FXT 4.4.4 – Manage output file");
		String input = NIXPATH1 + "/my/important/folder";
		String output = toTest.winPath(PathType.NIX, input);
		String expected = WINPATH1 + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = NIXPATH1 + "/my/important/file.txt";
		output = toTest.winPath(PathType.NIX, input);
		expected = WINPATH1 + "\\my\\important\\file.txt";
	
		assertEquals(expected, output);

		input = NIXPATH2 + "/my/important/folder";
		output = toTest.winPath(PathType.NIX, input);
		expected = WINPATH2 + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = NIXPATH2 + "/my/important/file.txt";
		output = toTest.winPath(PathType.NIX, input);
		expected = WINPATH2 + "\\my\\important\\file.txt";
		
		assertEquals(expected, output);

		input = NIXPATH4 + "/my/important/folder";
		output = toTest.winPath(PathType.NIX, input);
		expected = WINPATH4 + "\\my\\important\\folder";
		System.out.println("input : "+ input);
		System.out.println("output : "+ output);
		
		assertEquals(expected, output);
		
	}
	
	@Test
	public void testWinToWin() throws UnknownPathException
	{
		String input = WINPATH1+"\\my\\important\\folder";
		String output = toTest.winPath(PathType.WIN, input);
		String expected = WINPATH1 + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = WINPATH1+"\\my\\important\\file.txt";
		output = toTest.winPath(PathType.WIN, input);
		expected = WINPATH1 + "\\my\\important\\file.txt";
		assertEquals(expected, output);

		input = WINPATH2+"\\my\\important\\folder";
		output = toTest.winPath(PathType.WIN, input);
		expected = WINPATH2 + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = WINPATH2+"\\my\\important\\file.txt";
		output = toTest.winPath(PathType.WIN, input);
		expected = WINPATH2 + "\\my\\important\\file.txt";
		assertEquals(expected, output);
		
	}

	@Test
	public void testUncToWin() throws UnknownPathException
	{
		String input = UNCPATH1 + "\\my\\important\\folder";
		String output = toTest.winPath(PathType.UNC, input);
		String expected = WINPATH1+ "\\my\\important\\folder";
		assertEquals(expected, output);

		input = UNCPATH1 + "\\my\\important\\file.txt";
		output = toTest.winPath(PathType.UNC, input);
		expected = WINPATH1 + "\\my\\important\\file.txt";
		assertEquals(expected, output);
		
		input = UNCPATH2 + "\\my\\important\\folder";
		output = toTest.winPath(PathType.UNC, input);
		expected = WINPATH2+ "\\my\\important\\folder";
		assertEquals(expected, output);

		input = UNCPATH2 + "\\my\\important\\file.txt";
		output = toTest.winPath(PathType.UNC, input);
		expected = WINPATH2 + "\\my\\important\\file.txt";
		assertEquals(expected, output);
	}

	@Test
	public void testNixToUNC() throws UnknownPathException
	{
		logger.info("Starting FXT 4.4.4 – Manage output file");


		String input = NIXPATH1+"/my/important/folder";
		String output = toTest.uncPath(PathType.NIX, input);
		String expected = UNCPATH1 + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = NIXPATH1+"/my/important/file.txt";
		output = toTest.uncPath(PathType.NIX, input);
		expected = UNCPATH1 + "\\my\\important\\file.txt";
		
		assertEquals(expected, output);
		
		input = NIXPATH2+"/my/important/folder";
		output = toTest.uncPath(PathType.NIX, input);
		expected = UNCPATH2 + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = NIXPATH2+"/my/important/file.txt";
		output = toTest.uncPath(PathType.NIX, input);
		expected = UNCPATH2 + "\\my\\important\\file.txt";
		

	}

	@Test
	public void testWinToUNC() throws UnknownPathException
	{

		String input = WINPATH1+"\\my\\important\\folder";
		String output = toTest.uncPath(PathType.WIN, input);
		String expected = UNCPATH1 + "\\my\\important\\folder";
		assertEquals(expected, output);

		input =WINPATH1+"\\my\\important\\file.txt";
		output = toTest.uncPath(PathType.WIN, input);
		expected = UNCPATH1 + "\\my\\important\\file.txt";
		assertEquals(expected, output);

		input = WINPATH2+"\\my\\important\\folder";
		output = toTest.uncPath(PathType.WIN, input);
		expected = UNCPATH2 + "\\my\\important\\folder";
		assertEquals(expected, output);

		input =WINPATH2+"\\my\\important\\file.txt";
		output = toTest.uncPath(PathType.WIN, input);
		expected = UNCPATH2 + "\\my\\important\\file.txt";
		assertEquals(expected, output);
	}

	@Test
	public void testUNCtoUNC() throws UnknownPathException
	{

		String input = UNCPATH1 + "\\my\\important\\folder";
		String output = toTest.uncPath(PathType.UNC, input);
		String expected = UNCPATH1 + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = UNCPATH1 + "\\my\\important\\file.txt";
		output = toTest.uncPath(PathType.UNC, input);
		expected = UNCPATH1 + "\\my\\important\\file.txt";
		assertEquals(expected, output);
		
		input = UNCPATH2 + "\\my\\important\\folder";
		output = toTest.uncPath(PathType.UNC, input);
		expected = UNCPATH2 + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = UNCPATH2 + "\\my\\important\\file.txt";
		output = toTest.uncPath(PathType.UNC, input);
		expected = UNCPATH2 + "\\my\\important\\file.txt";
		assertEquals(expected, output);

	}
	
	@Test
	public void testWinToNIX() throws UnknownPathException{
		String input = WINPATH1+"\\my\\important\\folder";
		String output = toTest.nixPath(PathType.WIN, input);
		String expected = NIXPATH1 + "/my/important/folder";
		assertEquals(expected, output);

		input =WINPATH1+"\\my\\important\\file.txt";
		output = toTest.nixPath(PathType.WIN, input);
		expected = NIXPATH1 + "/my/important/file.txt";
		assertEquals(expected, output);

		input = WINPATH2+"\\my\\important\\folder";
		output = toTest.nixPath(PathType.WIN, input);
		expected = NIXPATH2 + "/my/important/folder";
		assertEquals(expected, output);

		input =WINPATH2+"\\my\\important\\file.txt";
		output = toTest.nixPath(PathType.WIN, input);
		expected = NIXPATH2 + "/my/important/file.txt";
		assertEquals(expected, output);
	}
	@Test
	public void testNixToNIX() throws UnknownPathException{
		String input = NIXPATH1 + "/my/important/folder";
		String output = toTest.nixPath(PathType.NIX, input);
		String expected = NIXPATH1 + "/my/important/folder";
		assertEquals(expected, output);

		input = NIXPATH1 + "/my/important/file.txt";
		output = toTest.nixPath(PathType.NIX, input);
		expected = NIXPATH1 + "/my/important/file.txt";
		assertEquals(expected, output);

		input = NIXPATH2 + "/my/important/folder";
		output = toTest.nixPath(PathType.NIX, input);
		expected = NIXPATH2 + "/my/important/folder";
		assertEquals(expected, output);

		input =NIXPATH2 + "/my/important/file.txt";
		output = toTest.nixPath(PathType.NIX, input);
		expected = NIXPATH2 + "/my/important/file.txt";
		assertEquals(expected, output);
	}
	
	@Test
	public void testUNCToNIX() throws UnknownPathException{
		String input = UNCPATH1+"\\my\\important\\folder";
		String output = toTest.nixPath(PathType.UNC, input);
		String expected = NIXPATH1 + "/my/important/folder";
		assertEquals(expected, output);

		input =UNCPATH1+"\\my\\important\\file.txt";
		output = toTest.nixPath(PathType.UNC, input);
		expected = NIXPATH1 + "/my/important/file.txt";
		assertEquals(expected, output);

		input = UNCPATH2+"\\my\\important\\folder";
		output = toTest.nixPath(PathType.UNC, input);
		expected = NIXPATH2 + "/my/important/folder";
		assertEquals(expected, output);

		input =UNCPATH2+"\\my\\important\\file.txt";
		output = toTest.nixPath(PathType.UNC, input);
		expected = NIXPATH2 + "/my/important/file.txt";
		assertEquals(expected, output);
	}
	
	@Test
	public void testFTPToNIX() throws UnknownPathException{
		String input = USERFTPPREFIX+FTPPATH1 + "/my/important/folder";
		String output = toTest.nixPath(PathType.FTP, input);
		String expected = NIXPATH1 + "/my/important/folder";
		System.out.println(String.format("input {%s} output {%s} expected {%s}", input,output,expected));
		assertEquals(expected, output);

		input = USERFTPPREFIX+FTPPATH1 + "/my/important/file.txt";
		output = toTest.nixPath(PathType.FTP, input);
		expected = NIXPATH1 + "/my/important/file.txt";
		System.out.println(String.format("input {%s} output {%s} expected {%s}", input,output,expected));
		assertEquals(expected, output);

		input = USERFTPPREFIX+FTPPATH2 + "/my/important/folder";
		output = toTest.nixPath(PathType.FTP, input);
		expected = NIXPATH2 + "/my/important/folder";
		System.out.println(String.format("input {%s} output {%s} expected {%s}", input,output,expected));
		assertEquals(expected, output);

		input =USERFTPPREFIX+FTPPATH2 + "/my/important/file.txt";
		output = toTest.nixPath(PathType.FTP, input);
		expected = NIXPATH2 + "/my/important/file.txt";
		System.out.println(String.format("input {%s} output {%s} expected {%s}", input,output,expected));
		assertEquals(expected, output);
	}

	@Test
	public void testPreprocess(){
		
		String expected= "//ardome/1/hr/hr01/2013/01/14/BM-140113-1-M1_0119315.mxf";
		String actual=toTest.preprocessPath(PathType.FTP,"ftp://armedia:aidem630@mamhrs05.mam.foxtel.com.au:2121//ardome/1/hr/hr01/2013/01/14/BM-140113-1-M1_0119315.mxf");
		
		assertEquals(expected,actual);
	}
	
	@Test
	public void testRelativePath(){
		
		logger.info("Starting FXT 4.4.4 – Manage output file");

		String outer = "/my/storage/";
		String inner = "/my/storage/path/to/resource";
		String expected = "path/to/resource";
		String output = toTest.getRelativePath(outer, inner);
		
		if (expected == output)
		{
			logger.info(" FXT 4.4.4 – Manage output file --Passed");
		}
		else
			logger.info(" FXT 4.4.4 – Manage output file --Failed");
		assertEquals(expected,output);
		

	}
	
}
