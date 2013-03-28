package com.mediasmiths.foxtel.mpa.processing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.xml.sax.SAXException;

import com.mediasmiths.foxtel.agent.validation.MessageValidationResult;
import com.mediasmiths.foxtel.mpa.MarketingMaterialTest;
import com.mediasmiths.foxtel.mpa.MediaEnvelope;
import com.mediasmiths.foxtel.mpa.PendingImport;
import com.mediasmiths.foxtel.mpa.ResultLogger;
import com.mediasmiths.foxtel.mpa.TestUtil;
import com.mediasmiths.mayam.MayamClientErrorCode;
import com.mediasmiths.mayam.MayamClientException;

@Ignore
public class MarketingMaterialProcessingTest_FXT_4_6_3 extends MaterialProcessingTest {
	
	private static Logger logger = Logger.getLogger(MarketingMaterialProcessingTest_FXT_4_6_3.class);
	private static Logger resultLogger = Logger.getLogger(ResultLogger.class);

        @Test
        public void testProcessMessageValidMessageAndMediaTitleExistsMediaFirstMediaValid_FXT_4_6_3_2_3()
                        throws FileNotFoundException, DatatypeConfigurationException,
                        IOException, JAXBException, SAXException, InterruptedException,
                        MayamClientException {
    		String testName="FXT 4.6.3.2/3  - Marketing material message references existing title/Mayam ingest task for item is updated ";
    		logger.info("Starting" +testName);
    		
    		testProcessMessageValidMessageAndMedia(true, true, true, testName);
        }

        @Test
        public void testProcessMessageValidMessageAndMediaTitleExistsMediaFirstMediaInValid_FXT_4_6_2()
                        throws FileNotFoundException, DatatypeConfigurationException,
                        IOException, JAXBException, SAXException, InterruptedException,
                        MayamClientException {
        	
    		String testName="FXT 4.6.3.2  - Marketing material message references existing title";
    		logger.info("Starting" +testName);
    		
        	
                testProcessMessageValidMessageAndMedia(true, true, false, testName);
        }

        @Test
        public void testProcessMessageValidMessageAndMediaTitleExistsMessageFirstMediaValid_FXT_4_6_3_2_3()
                        throws FileNotFoundException, DatatypeConfigurationException,
                        IOException, JAXBException, SAXException, InterruptedException,
                        MayamClientException {
    			String testName="FXT 4.6.3.2/3  - Marketing material message references existing title/Mayam ingest task for item is updated ";
    			logger.info("Starting" +testName);
    			
                testProcessMessageValidMessageAndMedia(true, false, true, testName);
        }

        @Test
        public void testProcessMessageValidMessageAndMediaTitleExistsMessageFirstMediaInvalid_FXT_4_6_3_2()
                        throws FileNotFoundException, DatatypeConfigurationException,
                        IOException, JAXBException, SAXException, InterruptedException,
                        MayamClientException {
        		String testName="FXT 4.6.3.2  - Marketing material message references existing title";
    			logger.info("Starting" +testName);
    		
                testProcessMessageValidMessageAndMedia(true, false, false, testName);
        }

        @Test
        public void testProcessMessageValidMessageAndMediatitleDoesntExistMediaFirstMediaValid_FXT_4_6_3_1()
                        throws FileNotFoundException, DatatypeConfigurationException,
                        IOException, JAXBException, SAXException, InterruptedException,
                        MayamClientException {
        	
    		String testName="FXT 4.6.3.1  - Marketing material message references non existing title";
    		logger.info("Starting" +testName);
    		
                testProcessMessageValidMessageAndMedia(false, true, true, testName);
        }

        @Test
        public void testProcessMessageValidMessageAndMediatitleDoesntExistMediaFirstMediaInvalid_FXT_4_6_3_1()
                        throws FileNotFoundException, DatatypeConfigurationException,
                        IOException, JAXBException, SAXException, InterruptedException,
                        MayamClientException {       		
    			String testName="FXT 4.6.3.1  - Marketing material message references non existing title";
    			logger.info("Starting" +testName);
    			
                testProcessMessageValidMessageAndMedia(false, true, false, testName);
        }

        @Test
        public void testProcessMessageValidMessageAndMediatitleDoesntExistMessageFirstMediaValid_FXT_4_6_3_1()
                        throws FileNotFoundException, DatatypeConfigurationException,
                        IOException, JAXBException, SAXException, InterruptedException,
                        MayamClientException {
    			String testName="FXT 4.6.3.1  - Marketing material message references non existing title";
    			logger.info("Starting" +testName);
    		
                testProcessMessageValidMessageAndMedia(false, false, true, testName);
        }

        @Test
        public void testProcessMessageValidMessageAndMediatitleDoesntExistMessageFirstMediaInValid_FXT_4_6_3_1()
                        throws FileNotFoundException, DatatypeConfigurationException,
                        IOException, JAXBException, SAXException, InterruptedException,
                        MayamClientException {
    			String testName="FXT 4.6.3.1  - Marketing material message references non existing title";
    			logger.info("Starting" +testName);
    		
                testProcessMessageValidMessageAndMedia(false, false, false, testName);
        }

        public void testProcessFailsOnMayamExceptionCreatingOrUpdatingTitle(
                        boolean titleExists) throws DatatypeConfigurationException,
                        FileNotFoundException, IOException, MayamClientException,
                        JAXBException, SAXException, InterruptedException {
                // prepare files
                material = MarketingMaterialTest.getMaterial(TITLE_ID);

                materialXMLPath = materialxml.getAbsolutePath();
                TestUtil.writeMaterialToFile(material, materialXMLPath);

                // prepare mocks
                when(validator.validatePickupPackage(materialXMLPath)).thenReturn(
                                MessageValidationResult.IS_VALID);
                when(mayamClient.titleExists(TITLE_ID)).thenReturn(titleExists);
                when(mayamClient.updateTitle(argThat(titleIDMatcher))).thenReturn(
                                MayamClientErrorCode.FAILURE);
                when(mayamClient.createTitle(argThat(titleIDMatcher))).thenReturn(
                                MayamClientErrorCode.FAILURE);

                // add file to queue for processing
                filesPendingProcessingQueue.add(materialxml.getAbsolutePath());

                // wait for some time to allow processing to take place
                Thread.sleep(500l);

                // check message gets moved to failure folder
                assertFalse(materialxml.exists());
                assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(materialxml,
                                new File(failurePath)).exists());
        }

        @Test
        public void testProcessingFailsWhenErrorQueryingTitleExistance()
                        throws DatatypeConfigurationException, MayamClientException,
                        InterruptedException, FileNotFoundException, JAXBException,
                        SAXException {
                // prepare files
                material = MarketingMaterialTest.getMaterial(TITLE_ID);

                materialXMLPath = materialxml.getAbsolutePath();
                TestUtil.writeMaterialToFile(material, materialXMLPath);

                // prepare mocks
                when(validator.validatePickupPackage(materialXMLPath)).thenReturn(
                                MessageValidationResult.IS_VALID);
                when(mayamClient.titleExists(TITLE_ID)).thenThrow(
                                new MayamClientException(MayamClientErrorCode.FAILURE));
                // add file to queue for processing
                filesPendingProcessingQueue.add(materialxml.getAbsolutePath());

                // wait for some time to allow processing to take place
                Thread.sleep(500l);

                // check message gets moved to failure folder
                assertFalse(materialxml.exists());
                assertTrue(TestUtil.getPathToThisFileIfItWasInThisFolder(materialxml,
                                new File(failurePath)).exists());
        }

        @Test
        public void testProcessFailsOnMayamExceptionUpdatingTitle()
                        throws FileNotFoundException, DatatypeConfigurationException,
                        IOException, MayamClientException, JAXBException, SAXException,
                        InterruptedException {
                testProcessFailsOnMayamExceptionCreatingOrUpdatingTitle(true);
        }

        @Test
        public void testProcessFailsOnMayamExceptionCreatingTitle()
                        throws FileNotFoundException, DatatypeConfigurationException,
                        IOException, MayamClientException, JAXBException, SAXException,
                        InterruptedException {
                testProcessFailsOnMayamExceptionCreatingOrUpdatingTitle(false);

        }

        /**
         * Test that a valid message is processed correctly
         * 
         * @throws DatatypeConfigurationException
         * @throws IOException
         * @throws FileNotFoundException
         * @throws SAXException
         * @throws JAXBException
         * @throws InterruptedException
         * @throws MayamClientException
         * 
         */
        public void testProcessMessageValidMessageAndMedia(boolean titleExists,
                        boolean mediaFirst, boolean validMedia, String testName)
                        throws DatatypeConfigurationException, FileNotFoundException,
                        IOException, JAXBException, SAXException, InterruptedException,
                        MayamClientException {

                // prepare files
                TestUtil.writeBytesToFile(100, media);
                material = MarketingMaterialTest.getMaterial(TITLE_ID);

                materialXMLPath = materialxml.getAbsolutePath();
                TestUtil.writeMaterialToFile(material, materialXMLPath);

                // prepare mocks
                when(validator.validatePickupPackage(materialXMLPath)).thenReturn(
                                MessageValidationResult.IS_VALID);
                when(mayamClient.titleExists(TITLE_ID)).thenReturn(titleExists);
                when(mayamClient.updateTitle(argThat(titleIDMatcher))).thenReturn(
                                MayamClientErrorCode.SUCCESS);
                when(mayamClient.createTitle(argThat(titleIDMatcher))).thenReturn(
                                MayamClientErrorCode.SUCCESS);

                if (mediaFirst) {
                        when(matchMaker.matchMXF(media)).thenReturn(null);
                        when(matchMaker.matchXML(argThat(matchEnvelopeByFile))).thenReturn(
                                        media.getAbsolutePath());

                } else {
                        when(matchMaker.matchXML(argThat(matchEnvelopeByFile))).thenReturn(
                                        null);
                        when(matchMaker.matchMXF(media)).thenReturn(
                                        new MediaEnvelope(materialxml, material));
                }
                when(mediaCheck.mediaCheck(eq(media), argThat(matchEnvelopeByFile)))
                                .thenReturn(validMedia);

                // add pending files to queue
                if (mediaFirst) {
                        filesPendingProcessingQueue.add(media.getAbsolutePath());
                        filesPendingProcessingQueue.add(materialxml.getAbsolutePath());
                } else {
                        filesPendingProcessingQueue.add(materialxml.getAbsolutePath());
                        filesPendingProcessingQueue.add(media.getAbsolutePath());
                }

                // wait for some time to allow processing to take place
                Thread.sleep(500l);

                if (titleExists) {
                        verify(mayamClient).updateTitle(argThat(titleIDMatcher));
                } else {
                        verify(mayamClient).createTitle(argThat(titleIDMatcher));
                }

                InOrder inOrder = inOrder(matchMaker); // check that mxf and xml
                // processed in same order they
                // were placed in queue
                if (mediaFirst) {
                        inOrder.verify(matchMaker).matchMXF(media);
                        inOrder.verify(matchMaker).matchXML(argThat(matchEnvelopeByFile));
                } else {
                        inOrder.verify(matchMaker).matchXML(argThat(matchEnvelopeByFile));
                        inOrder.verify(matchMaker).matchMXF(media);
                }

                verify(mediaCheck).mediaCheck(eq(media), argThat(matchEnvelopeByFile));

                // check the files pending processing queue has been consumed
                assertTrue(filesPendingProcessingQueue.size() == 0);


                if (validMedia) {
                        // check there is a pending import on the queue
                        assertTrue(pendingImportQueue.size() == 1);
                        PendingImport pi = pendingImportQueue.take();
                        Boolean piMediaTest=pi.getMediaFile().equals(media);
                        assertTrue(piMediaTest);
                        Boolean piMaterialTest=pi.getMaterialEnvelope().getFile().equals(materialxml);
                        assertTrue(piMaterialTest);
                        
                        
                        if (testName!=null)
                       {
                        	if (piMediaTest && piMaterialTest)
                        	{
                        		resultLogger.info(testName+ "-- Passed");
                        	}
                        	else
                        	{
                        		resultLogger.info(testName+"-- FAILED");
                        	}
                        }
                } else {
                        // check message gets moved to failure folder and media gets moved to viz ardome emergency import folder
                		Boolean materialExist=materialxml.exists();
                        assertFalse(materialExist);
                        
                        Boolean mediaExist=media.exists();
                        assertFalse(mediaExist);
                        
                        Boolean failurePathExist=TestUtil.getPathToThisFileIfItWasInThisFolder(materialxml, new File(failurePath)).exists();
                        assertTrue(failurePathExist);
                        
                        Boolean importPathExist=TestUtil.getPathToThisFileIfItWasInThisFolder(media, new File(emergencyImportPath)).exists();
                        assertTrue(importPathExist);
                        
                        
                       if (testName!=null)
                       {
                        	if (!mediaExist && !materialExist && failurePathExist && importPathExist)
                        	{
                        		resultLogger.info(testName+ "-- Passed");
                        	}
                        	else
                        	{
                        		resultLogger.info(testName+"-- FAILED");
                        	}
                        }
                        
                        

                }

        }

}