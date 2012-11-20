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
	    
	    @Column(name = "taskType")  
	    private String taskType;  
	    
	    @Column(name = "taskState")  
	    private String taskState;
	    
	    @Column(name = "category")  
	    private String category;
	    
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
	    
	    public long getId() {return id;}  
	    public void setId(long id) {this.id = id;}  
	   
	    public String getTaskType() {return taskType;}  
	    public void setTaskType(String taskTypeName) {this.taskType = taskTypeName.toString();}  
	    
	    public String getTaskState() {return taskState;}  
	    public void setTaskState(String state) {this.taskState = state;} 
	    
	    public String getCategory() {return category;}  
	    public void setCategory(String categoryName) {this.category = categoryName;} 
	    
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
}
