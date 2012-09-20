package com.medismiths.foxtel.mpa.processing;


public class UnmatchedFile {


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filePath == null) ? 0 : filePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		UnmatchedFile other = (UnmatchedFile) obj;
		if (filePath == null) {
			if (other.filePath != null){
				return false;}
		} else if (!filePath.equals(other.filePath)) {
			return false;
		}
		return true;
	}

	private final long timeSeen;
	private final String filePath;

	public UnmatchedFile(long timeSeen, String filePath) {
		this.timeSeen = timeSeen;
		this.filePath = filePath;
	}

	public UnmatchedFile(String filePath) {
		this(0L, filePath);
	}

	public long getTimeSeen() {
		return timeSeen;
	}

	public String getFilePath() {
		return filePath;
	}

}