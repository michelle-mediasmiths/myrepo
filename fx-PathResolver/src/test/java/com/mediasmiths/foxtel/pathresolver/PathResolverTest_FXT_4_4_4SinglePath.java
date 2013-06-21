package com.mediasmiths.foxtel.pathresolver;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import com.mediasmiths.foxtel.pathresolver.PathResolver;
import com.mediasmiths.foxtel.pathresolver.PathResolver.PathType;

public class PathResolverTest_FXT_4_4_4SinglePath
{	
	private static Logger logger = Logger.getLogger(PathResolverTest_FXT_4_4_4SinglePath.class);

	private final static String NIXPATH = "/storage";
	private final static String WINPATH = "f:";
	private final static String UNCPATH = "\\\\foxtel\\foxtel";
	private final static String FTPPATH = "ftp://user:pass@host.mediasmiths.com:2121//some/path/to/storage";
	
	PathResolver toTest;

	@Before
	public void before()
	{

		toTest = new PathResolver(NIXPATH, WINPATH, UNCPATH,FTPPATH);

	}

	@Test
	public void testNixToWin() throws UnknownPathException
	{
		logger.info("Starting FXT 4.4.4 – Manage output file");
		String input = "/storage/my/important/folder";
		String output = toTest.winPath(PathType.NIX, input);
		String expected = WINPATH + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = "/storage/my/important/file.txt";
		output = toTest.winPath(PathType.NIX, input);
		expected = WINPATH + "\\my\\important\\file.txt";
		
		if (expected == output)
		{
			logger.info(" FXT 4.4.4 – Manage output file --Passed");
		}
		else
			logger.info(" FXT 4.4.4 – Manage output file --Failed");

		assertEquals(expected, output);

	}

	@Test
	public void testWinToWin() throws UnknownPathException
	{
		String input = "f:\\my\\important\\folder";
		String output = toTest.winPath(PathType.WIN, input);
		String expected = WINPATH + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = "f:\\my\\important\\file.txt";
		output = toTest.winPath(PathType.WIN, input);
		expected = WINPATH + "\\my\\important\\file.txt";
		assertEquals(expected, output);

	}

	@Test
	public void testUncToWin() throws UnknownPathException
	{
		String input = UNCPATH + "\\my\\important\\folder";
		String output = toTest.winPath(PathType.UNC, input);
		String expected = WINPATH + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = UNCPATH + "\\my\\important\\file.txt";
		output = toTest.winPath(PathType.UNC, input);
		expected = WINPATH + "\\my\\important\\file.txt";
		assertEquals(expected, output);
	}

	@Test
	public void testNixToUNC() throws UnknownPathException
	{
		logger.info("Starting FXT 4.4.4 – Manage output file");


		String input = "/storage/my/important/folder";
		String output = toTest.uncPath(PathType.NIX, input);
		String expected = UNCPATH + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = "/storage/my/important/file.txt";
		output = toTest.uncPath(PathType.NIX, input);
		expected = UNCPATH + "\\my\\important\\file.txt";
		
		if (expected == output)
		{
			logger.info(" FXT 4.4.4 – Manage output file --Passed");
		}
		else
			logger.info(" FXT 4.4.4 – Manage output file --Failed");
		assertEquals(expected, output);

	}

	@Test
	public void testWinToUNC() throws UnknownPathException
	{

		String input = "f:\\my\\important\\folder";
		String output = toTest.uncPath(PathType.WIN, input);
		String expected = UNCPATH + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = "f:\\my\\important\\file.txt";
		output = toTest.uncPath(PathType.WIN, input);
		expected = UNCPATH + "\\my\\important\\file.txt";
		assertEquals(expected, output);

	}

	@Test
	public void testUNCtoUNC() throws UnknownPathException
	{

		String input = UNCPATH + "\\my\\important\\folder";
		String output = toTest.uncPath(PathType.UNC, input);
		String expected = UNCPATH + "\\my\\important\\folder";
		assertEquals(expected, output);

		input = UNCPATH + "\\my\\important\\file.txt";
		output = toTest.uncPath(PathType.UNC, input);
		expected = UNCPATH + "\\my\\important\\file.txt";
		assertEquals(expected, output);

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
