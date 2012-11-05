package com.mediasmiths.foxtel.tc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import au.com.foxtel.cf.mam.pms.MaterialType;
import au.com.foxtel.cf.mam.pms.PackageType;
import au.com.foxtel.cf.mam.pms.PresentationFormatType;

import com.mayam.wf.exception.RemoteException;
import com.mediasmiths.foxtel.tc.service.PathResolver;
import com.mediasmiths.foxtel.tc.service.PathResolver.PathType;
import com.mediasmiths.mayam.MayamClient;
import com.mediasmiths.mayam.MayamClientException;

public class JobBuilderTest
{
	
	private static String PACKAGEID = "packageid";
	private static String MATERIALID = "materialid";
	private static String INPUTFILE = "f:\\tcinput\\packageid.mxf";
	private static String INPUTFILE_UNC = "\\\\foxtel\\foxtel\\tcinput\\packageid.mxf";
    private static String OUTPUTFOLDER = "f:\\tcoutput\\packageid\\";
    private static String OUTPUTFOLDER_UNC = "\\\\foxtel\\foxtel\\tcoutput\\packageid\\";
    

	private JobBuilder toTest;
	private MayamClient mayamClient;
	private PathResolver pathResolver;
	
	private final static Logger log = Logger.getLogger(JobBuilderTest.class);
	
	@Before
	public void before(){
		
		mayamClient = mock(MayamClient.class);		
		pathResolver = mock(PathResolver.class);
		
		toTest = new JobBuilder();
		toTest.setMayamClient(mayamClient);
		toTest.setPathResolver(pathResolver);
		
	}
	
	@Test
	public void testBuildJobForTxPackageTranscode() throws MayamClientException, JobBuilderException, RemoteException{
	
		PackageType pt = buildPackage(PresentationFormatType.HD);
		pt.setMaterialID(MATERIALID);
		MaterialType mt =buildMaterialType("HD");
		
		when(mayamClient.getPackage(PACKAGEID)).thenReturn(pt);
		when(mayamClient.getMaterial(MATERIALID)).thenReturn(mt);
		when(pathResolver.uncPath(PathType.WIN, INPUTFILE)).thenReturn(INPUTFILE_UNC);
		when(pathResolver.uncPath(PathType.WIN, OUTPUTFOLDER)).thenReturn(OUTPUTFOLDER_UNC);
		
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
