package com.mediasmiths.foxtel.placeholder.receipt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ReceiptWriter {

	private static Logger logger = Logger.getLogger(ReceiptWriter.class);
	private final String receiptPath;
	
	@Inject
	public ReceiptWriter(@Named("placeholder.path.receipt") String receiptPath){
		this.receiptPath=receiptPath;
	}
	
	public void writeRecipet(String filePath, String messageID) throws FileNotFoundException, IOException {
		
		String path = receiptPath + IOUtils.DIR_SEPARATOR + messageID+ ".txt";
		
		logger.info(String.format("Writing reciept for messageID %s (%s) to %s", messageID,filePath,path));
		
		IOUtils.write(messageID, new FileOutputStream(new File(path)));
	}

}
