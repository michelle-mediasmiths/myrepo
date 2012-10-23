package com.mediasmiths.foxtel.tc;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class JobBuilderTest
{
	
	private static String PACKAGEID = "packageid";
	private static String MATERIALID = "materialid";
	private static String INPUTFILE = "f:\\tcinput\\packageid.mxf";
    private static String OUTPUTFOLDER = "f:\\tcoutput\\packageid\\";

	private JobBuilder toTest;
	private MayamClient mayamClient;
	
	private final static Logger log = Logger.getLogger(JobBuilderTest.class);
	
	@Before
	public void before(){
		
		mayamClient = mock(MayamClient.class);
		toTest = new JobBuilder(mayamClient);
		
	}
	
	@Test
	public void testBuildJobForTxPackageTranscode() throws MayamClientException, JobBuilderException{
	
		PackageType pt = buildPackage(PresentationFormatType.HD);
		pt.setMaterialID(MATERIALID);
		MaterialType mt =buildMaterialType("HD");
		
		when(mayamClient.getPackage(PACKAGEID)).thenReturn(pt);
		when(mayamClient.getMaterial(MATERIALID)).thenReturn(mt);
		
		String pcp = toTest.buildJobForTxPackageTranscode(PACKAGEID, INPUTFILE, OUTPUTFOLDER);
		log.debug(pcp);
	}
	
	private MaterialType buildMaterialType(String presentationFormat){
		MaterialType mt = new MaterialType();
		mt.setRequiredFormat(presentationFormat);
		return mt;
	}
	
	private PackageType buildPackage(PresentationFormatType presentationFormat){
		PackageType pt = new PackageType();
		pt.setPresentationFormat(presentationFormat);
		return pt;
	}
}
