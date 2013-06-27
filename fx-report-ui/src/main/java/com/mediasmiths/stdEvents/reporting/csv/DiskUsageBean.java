package com.mediasmiths.stdEvents.reporting.csv;

public class DiskUsageBean {
	
	private String channel;
	private String hrSize;
	private String tsmSize;
	private String lrSize;
	private String othersSize;
	private String totalSize;
	
	public void setChannel(String channel)
	{
		this.channel = channel;
	}
	
	public String getChannel()
	{
		return this.channel;
	}
	
	public void setHrSize(String hrSize)
	{
		this.hrSize = hrSize;
	}
	
	public String getHrSize()
	{
		return this.hrSize;
	}
	
	public void setTsmSize(String tsmSize)
	{
		this.tsmSize = tsmSize;
	}
	
	public String getTsmSize()
	{
		return this.tsmSize;
	}
	
	public void setLrSize(String lrSize)
	{
		this.lrSize = lrSize;
	}
	
	public String getLrSize()
	{
		return this.lrSize;
	}
	
	public void setOthersSize(String othersSize)
	{
		this.othersSize = othersSize;
	}
	
	public String getOthersSize()
	{
		return this.othersSize;
	}
	
	public void setTotalSize(String totalSize)
	{
		this.hrSize = totalSize;
	}
	
	public String getTotalSize()
	{
		return this.totalSize;
	}
	
	@Override
	public int hashCode() 
	{
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
 		result = prime * result + ((hrSize == null) ? 0 : hrSize.hashCode());
 		result = prime * result + ((tsmSize == null) ? 0 : tsmSize.hashCode());
 		result = prime * result + ((lrSize == null) ? 0 : lrSize.hashCode());
 		result = prime * result + ((othersSize == null) ? 0 : othersSize.hashCode());
		result = prime * result + ((totalSize == null) ? 0 : totalSize.hashCode());
 		return result;
 	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if( this == obj ) {
			return true;
		}
		if( obj == null ) {
			return false;
		}
		if( !(obj instanceof DiskUsageBean) ) {
 			return false;
 		}
		DiskUsageBean other = (DiskUsageBean) obj;
 		
		if( channel == null ) {
 			if( other.channel != null ) {
 				return false;
 			}
 		} else if( !channel.equals(other.channel) ) {
 			return false;
 		}
 		
 		if( hrSize == null ) {
 			if( other.hrSize != null ) {
 				return false;
 			}
 		} else if( !hrSize.equals(other.hrSize) ) {
 			return false;
 		}
 		
 		if( tsmSize == null ) {
 			if( other.tsmSize != null ) {
 				return false;
 			}
 		} else if( !tsmSize.equals(other.tsmSize) ) {
 			return false;
 		}
 		
 		if( lrSize == null ) {
 			if( other.lrSize != null ) {
 				return false;
 			}
 		} else if( !lrSize.equals(other.lrSize) ) {
 			return false;
 		}
 		
 		if( othersSize == null ) {
 			if( other.othersSize != null ) {
 				return false;
 			}
 		} else if( !othersSize.equals(other.othersSize) ) {
 			return false;
 		}
 		
 		if( totalSize == null ) {
 			if( other.totalSize != null ) {
 				return false;
 			}
 		} else if( !totalSize.equals(other.totalSize) ) {
 			return false;
 		}
 		
 		return true;
 	}
	
	@Override
	public String toString() 
	{
		return String.format(
	 		"DiskUsageBean [channel=%s, HR Size=%s, TSM Size=%s, LR Size=%s, Others Size=%s, TotalSize=%s]",
	 		channel, hrSize, tsmSize, lrSize, othersSize, totalSize);
	 }
	
}
