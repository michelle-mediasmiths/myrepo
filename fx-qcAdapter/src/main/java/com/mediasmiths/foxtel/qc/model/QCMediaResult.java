package com.mediasmiths.foxtel.qc.model;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QCMediaResult {

	private JobResultType result;
	private URI url;

	@XmlElement
	public URI getUrl() {
		return url;
	}

	public void setUrl(URI url) {
		this.url = url;
	}

	@XmlElement
	public JobResultType getResult() {
		return result;
	}

	public void setResult(JobResultType result) {
		this.result = result;
	}

	public void setResult(String result) {
		this.result = JobResultType.fromString(result);
	}

}
