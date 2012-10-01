package com.mediasmiths.foxtel.qc;

import java.util.Calendar;

import org.apache.axis.types.URI;

import com.mediasmiths.foxtel.qc.QCJobResult.ResultType;

public class QCJobResult {

	public enum ResultType {
		error,warning,success;

		public static ResultType fromString(String result) {
			if(result.equals("error")) return error;
			else if(result.equals("warning")) return warning;
			else if(result.equals("success")) return success;			
			else throw new IllegalArgumentException("Specified result was not valid");
		}
	}

	private QCJobIdentifier ident;
	private QCJobResult.ResultType result;
	private Calendar completed;
	private URI url;
	
	public URI getUrl() {
		return url;
	}
	public Calendar getCompleted() {
		return completed;
	}
	public void setCompleted(Calendar completed) {
		this.completed = completed;
	}
	public QCJobIdentifier getIdent() {
		return ident;
	}
	public void setIdent(QCJobIdentifier ident) {
		this.ident = ident;
	}
	
	public QCJobResult.ResultType getResult() {
		return result;
	}
	public void setResult(QCJobResult.ResultType result) {
		this.result = result;
	}
	
	public void setResult(String result) {
		this.result = ResultType.fromString(result);
	}
	public void setUrl(URI url) {
		this.url=url;
	}
	
	@Override
	public String toString(){
		return String.format("Job : {%s} , Result : {%s} , Completed {%s} , URL {%s} ", ident.toString(), result.toString(), completed.toString(), url.toString());
	}
	
}
