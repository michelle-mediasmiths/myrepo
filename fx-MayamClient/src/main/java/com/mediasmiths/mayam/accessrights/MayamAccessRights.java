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
		private boolean qcParallel;
	    
	    @Column(name = "contentType")  
	    private String contentType;
	    
	    @Column(name = "groupName") 
		private String groupName;
	    
	    @Column(name = "channelOwner") 
		private String channelOwner;
	    
	    @Column(name = "readAccess") 
		private boolean readAccess;
	    
	    @Column(name = "writeAccess") 
		private boolean writeAccess;
	    
	    @Column(name = "adminAccess") 
		private boolean adminAccess;
	    
	    @Column(name = "restricted") 
		private boolean restricted;
	    
	    @Column(name = "adultOnly") 
		private boolean adultOnly;
	    
	    public long getId() {return id;}  
	    public void setId(long id) {this.id = id;}  
	   
	    public String getQcStatus() {return qcStatus;}  
	    public void setQcStatus(String qcStatusValue) {this.qcStatus = qcStatusValue;}  
	    
	    public String getQaStatus() {return qaStatus;}  
	    public void setQaStatus(String qaStatusValue) {this.qaStatus = qaStatusValue;} 
	    
	    public boolean getQcParallel() {return qcParallel;}  
	    public void setQcParallel(boolean qcParallelValue) {this.qcParallel = qcParallelValue;} 
	    
	    public String getContentType() {return contentType;}  
	    public void setContentType(String contentTypeName) {this.contentType = contentTypeName;} 
	    
	    public String getGroupName() {return groupName;}  
	    public void setGroupName(String name) {this.groupName = name;} 
	    
	    public String getChannelOwner() {return channelOwner;}  
	    public void setChannelOwner(String ownerGroup) {this.channelOwner = ownerGroup;} 

	    public boolean getReadAccess() {return readAccess;}  
	    public void setReadAccess(boolean access) {this.readAccess = access;} 
	    
	    public boolean getWriteAccess() {return writeAccess;}  
	    public void setWriteAccess(boolean access) {this.writeAccess = access;} 
	    
	    public boolean getAdminAccess() {return adminAccess;}  
	    public void setAdminAccess(boolean access) {this.adminAccess = access;} 
	    
	    public boolean getRestricted() {return restricted;}  
	    public void setRestricted(boolean purgeProtected) {this.restricted = purgeProtected;}
	    
	    public boolean getAdultOnly() {return adultOnly;}  
	    public void setAdultOnly(boolean adultOnlyValue) {this.adultOnly = adultOnlyValue;} 
}
