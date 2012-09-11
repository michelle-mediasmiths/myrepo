package com.mediasmiths.foxtel.placeholder.receipt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ReceiptWriter {

	private static Logger logger = Logger.getLogger(ReceiptWriter.class);
	private final String receiptPath;
	
	public ReceiptWriter(String receiptPath){
		this.receiptPath=receiptPath;
	}
	
	public void writeRecipet(String filePath, String messageID) throws FileNotFoundException, IOException {
		
		String path = receiptPath + IOUtils.DIR_SEPARATOR + messageID;
		
		logger.info(String.format("Writing reciept for messageID %s (%s) to %s", messageID,filePath,path));
		
		IOUtils.write(messageID, new FileOutputStream(new File(path)));
	}

}
