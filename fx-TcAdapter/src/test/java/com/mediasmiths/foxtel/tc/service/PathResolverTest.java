package com.mediasmiths.foxtel.tc.service;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


import com.mediasmiths.foxtel.tc.service.PathResolver.PathType;

public class PathResolverTest
{
	
	private final static String NIXPATH="/storage";
	private final static String WINPATH="f:";
	private final static String UNCPATH="\\\\foxtel\\foxtel";
	
	PathResolver toTest;
	
	@Before
	public void before(){
		
		toTest = new PathResolver(NIXPATH,WINPATH,UNCPATH);
		
	}
	
	@Test
	public void testNixToUNC(){
		
		String input = "/storage/my/important/folder";
		String output = toTest.uncPath(PathType.NIX, input);
		String expected = UNCPATH + "\\my\\important\\folder";
		assertEquals(expected,output);	
		
		input = "/storage/my/important/file.txt";
		output = toTest.uncPath(PathType.NIX, input);
		expected = UNCPATH + "\\my\\important\\file.txt";
		assertEquals(expected,output);	
		
	}
	
	@Test
	public void testWinToUNC(){
		

		String input = "f:\\my\\important\\folder";
		String output = toTest.uncPath(PathType.WIN, input);
		String expected = UNCPATH + "\\my\\important\\folder";
		assertEquals(expected,output);	
		
		input ="f:\\my\\important\\file.txt";
		output = toTest.uncPath(PathType.WIN, input);
		expected = UNCPATH + "\\my\\important\\file.txt";
		assertEquals(expected,output);	
		
		
	}
	
	@Test
	public void testUNCtoUNC(){

		String input =  UNCPATH + "\\my\\important\\folder";
		String output = toTest.uncPath(PathType.UNC, input);
		String expected = UNCPATH + "\\my\\important\\folder";
		assertEquals(expected,output);	
		
		input = UNCPATH + "\\my\\important\\file.txt";
		output = toTest.uncPath(PathType.UNC, input);
		expected = UNCPATH + "\\my\\important\\file.txt";
		assertEquals(expected,output);	
		
	}
	
}
