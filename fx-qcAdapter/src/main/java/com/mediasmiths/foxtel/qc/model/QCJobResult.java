package com.mediasmiths.foxtel.qc.model;

import java.net.URI;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class QCJobResult {
	private QCJobIdentifier jobIdent;
	private JobResultType result;
	private Calendar completed;
	private URI url;

	@XmlElement
	public URI getUrl() {
		return url;
	}

	@XmlElement
	public Calendar getCompleted() {
		return completed;
	}

	public void setCompleted(Calendar completed) {
		this.completed = completed;
	}
	@XmlElement
	public QCJobIdentifier getIdent() {
		return jobIdent;
	}

	public void setIdent(QCJobIdentifier ident) {
		this.jobIdent = ident;
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

	public void setUrl(URI url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return String.format(
				"Job : {%s} , Result : {%s} , Completed {%s} , URL {%s} ",
				jobIdent.toString(), result.toString(), completed.toString(),
				url.toString());
	}

}
