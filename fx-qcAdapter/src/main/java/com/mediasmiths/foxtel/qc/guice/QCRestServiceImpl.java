package com.mediasmiths.foxtel.qc.guice;

import java.io.File;
import java.rmi.RemoteException;

import org.apache.axis.types.URI.MalformedURIException;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.cerify.CerifyClient;
import com.mediasmiths.foxtel.qc.QCIdentifier;
import com.mediasmiths.foxtel.qc.QCRestService;
import com.mediasmiths.foxtel.qc.QCStartResponse;
import com.mediasmiths.foxtel.qc.QCStartStatus;
import com.mediasmiths.foxtel.qc.QCStatus;

public class QCRestServiceImpl implements QCRestService {

	private final static Logger log = Logger.getLogger(QCRestServiceImpl.class);

	private final CerifyClient cerifyClient;

	@Inject
	public QCRestServiceImpl(CerifyClient cerifyClient) {
		log.trace("Constructed");
		this.cerifyClient = cerifyClient;
	}

	@Override
	public QCStatus status(QCIdentifier filePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String ping() {
		return "ping";
	}

	@Override
	public QCStartResponse start(String file, String ident, String profileName) {
		
		log.debug(String.format("Start requested for file  %s", file));

		try {
			String jobName = cerifyClient.startQcForFile(file, ident,
					profileName);
			QCStartResponse res = new QCStartResponse(QCStartStatus.STARTED);
			res.setQcIdentifier(new QCIdentifier(jobName));
			return res;
		} catch (Exception e) {
			log.error("Error starting qc", e);
			return new QCStartResponse(QCStartStatus.ERROR);
		}

	}

}
