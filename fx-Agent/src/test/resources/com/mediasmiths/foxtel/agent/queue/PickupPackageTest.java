package com.mediasmiths.foxtel.agent.queue;

import junit.framework.TestCase;
import org.junit.Test;

public class PickupPackageTest extends TestCase
{

	private static final String loc1 = "/usr/local/drop";
	private static final String rootName = "fl1";

	private static PickupPackage setUpLocal()
	{
		PickupPackage p = new PickupPackage(FileExtensions.XML,  FileExtensions.MXF);
		p.addPickUp(loc1 + "/" + rootName + ".xml");
		p.addPickUp(loc1 + "/" + rootName + ".mxf");

		return p;

	}

	@Test
	public void testExpectedPickUpCount() throws Exception
	{
         PickupPackage p = setUpLocal();

	     assertTrue(p.expectedPickUpCount() == 2);
	}

	@Test
	public void testCount() throws Exception
	{
		PickupPackage p = setUpLocal();

		assertTrue(p.count() == 2);
	}

	@Test
	public void testIsComplete() throws Exception
	{
		PickupPackage p = setUpLocal();

		assertTrue(p.isComplete() );
	}


	@Test
	public void testIsPickedUpSuffix() throws Exception
	{
		PickupPackage p = setUpLocal();
		assertTrue(p.isPickedUpSuffix(FileExtensions.MXF) );
		assertTrue(p.isPickedUpSuffix(FileExtensions.XML) );

	}

	@Test
	public void testAddPickUp() throws Exception
	{
        PickupPackage p = setUpLocal();

		assertTrue(p.getRootName().equals(rootName));
		assertTrue(p.getRootPath().equals(loc1));

        System.out.print("file : " + p.getPickUp(FileExtensions.XML).getAbsolutePath());
		System.out.print("file : " + p.getPickUp(FileExtensions.MXF).getAbsolutePath());


	}



}
