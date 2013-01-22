package com.mediasmiths.mayam.accessrights;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity  
@Table(name = "MayamAccessRights", schema = "FoxtelAccessRights")  
public class MayamAccessRights {

	    @Id  
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    @Column(name = "id") 
	    private long id;
	    
	    @Column(name = "qcStatus")  
	    private String qcStatus;  
	    
	    @Column(name = "qaStatus")  
	    private String qaStatus;
	    
	    @Column(name = "qcParallel") 
		private Boolean qcParallel;
	    
	    @Column(name = "contentType")  
	    private String contentType;
	    
	    @Column(name = "groupName") 
		private String groupName;
	    
	    @Column(name = "channelOwner") 
		private String channelOwner;
	    
	    @Column(name = "readAccess") 
		private Boolean readAccess;
	   
		@Column(name = "writeAccess") 
		private Boolean writeAccess;
	    
	    @Column(name = "adminAccess") 
		private Boolean adminAccess;
	    
	    @Column(name = "restricted") 
		private Boolean restricted;
	    
	    @Column(name = "adultOnly") 
		private Boolean adultOnly;
	    
	    public long getId() {return id;}  
	    public void setId(long id) {this.id = id;}  
	   
	    public String getQcStatus() {return qcStatus;}  
	    public void setQcStatus(String qcStatusValue) {this.qcStatus = qcStatusValue;}  
	    
	    public String getQaStatus() {return qaStatus;}  
	    public void setQaStatus(String qaStatusValue) {this.qaStatus = qaStatusValue;} 
	    
	    public Boolean getQcParallel() {return qcParallel;}  
	    public void setQcParallel(Boolean qcParallelValue) {this.qcParallel = qcParallelValue;} 
	    
	    public String getContentType() {return contentType;}  
	    public void setContentType(String contentTypeName) {this.contentType = contentTypeName;} 
	    
	    public String getGroupName() {return groupName;}  
	    public void setGroupName(String name) {this.groupName = name;} 
	    
	    public String getChannelOwner() {return channelOwner;}  
	    public void setChannelOwner(String ownerGroup) {this.channelOwner = ownerGroup;} 

	    public Boolean getReadAccess() {return readAccess;}  
	    public void setReadAccess(Boolean access) {this.readAccess = access;} 
	    
	    public Boolean getWriteAccess() {return writeAccess;}  
	    public void setWriteAccess(Boolean access) {this.writeAccess = access;} 
	    
	    public Boolean getAdminAccess() {return adminAccess;}  
	    public void setAdminAccess(Boolean access) {this.adminAccess = access;} 
	    
	    public Boolean getRestricted() {return restricted;}  
	    public void setRestricted(Boolean purgeProtected) {this.restricted = purgeProtected;}
	    
	    public Boolean getAdultOnly() {return adultOnly;}  
	    public void setAdultOnly(Boolean adultOnlyValue) {this.adultOnly = adultOnlyValue;}
	    
	    
	    @Override
		public String toString()
		{
			return "MayamAccessRights [id=" + id + ", qcStatus=" + qcStatus + ", qaStatus=" + qaStatus + ", qcParallel="
					+ qcParallel + ", contentType=" + contentType + ", groupName=" + groupName + ", channelOwner=" + channelOwner
					+ ", readAccess=" + readAccess + ", writeAccess=" + writeAccess + ", adminAccess=" + adminAccess
					+ ", restricted=" + restricted + ", adultOnly=" + adultOnly + "]";
		}
	    
}
