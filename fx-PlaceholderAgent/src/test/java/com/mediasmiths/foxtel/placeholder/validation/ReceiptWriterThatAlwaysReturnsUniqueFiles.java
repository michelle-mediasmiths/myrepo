package com.mediasmiths.foxtel.placeholder.validation;

import java.io.File;

import org.apache.commons.lang.RandomStringUtils;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.agent.ReceiptWriter;

/**
 * Used to stub out some request validation behavior for testing
 * 
 * normally the value of receiptPathForMessageID is used to decide if a given messages messageID is valdi (if there is no receipt then that message has not already been processed)
 * @author bmcleod
 *
 */
public class ReceiptWriterThatAlwaysReturnsUniqueFiles extends
		ReceiptWriter {

	@Inject
	public ReceiptWriterThatAlwaysReturnsUniqueFiles(String receiptPath) {
		super();
	}
	
	@Override
	public String receiptPathForMessageID(String messagePath,String messageID){
		String path = getReceiptPathForFile(messagePath) + RandomStringUtils.randomAlphabetic(20);
		
		if(! new File(path).exists()){
			return path;
		}
		else{
			return receiptPathForMessageID(messagePath,messageID);
		}
		

	}
	

}
