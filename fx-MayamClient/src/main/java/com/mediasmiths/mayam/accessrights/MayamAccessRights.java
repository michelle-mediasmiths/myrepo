package com.mediasmiths.mayam.accessrights;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity  
@Table(name = "MayamAccessRights", schema = "FoxtelAccessRights")  
public class MayamAccessRights {

	    @Id  
	    @GeneratedValue  
	    @Column(name = "id") 
	    private int id;
	    
	    @Column(name = "taskType")  
	    private String taskType;  
	    
	    @Column(name = "taskState")  
	    private String taskState;
	    
	    @Column(name = "assetType")  
	    private String assetType;
	    
	    @Column(name = "groupName") 
		private String groupName;
	    
	    @Column(name = "readAccess") 
		private boolean readAccess;
	    
	    @Column(name = "writeAccess") 
		private boolean writeAccess;
	    
	    @Column(name = "adminAccess") 
		private boolean adminAccess;
	    
	    public int getId() {return id;}  
	    public void setId(int id) {this.id = id;}  
	   
	    public String getTaskType() {return taskType;}  
	    public void setTaskType(String taskTypeName) {this.taskType = taskTypeName.toString();}  
	    
	    public String getTaskState() {return taskState;}  
	    public void setTaskState(String state) {this.taskState = state;} 
	    
	    public String getAssetType() {return assetType;}  
	    public void setAssetType(String assetTypeName) {this.assetType = assetTypeName;} 
	    
	    public String getGroupName() {return groupName;}  
	    public void setGroupName(String name) {this.groupName = name;} 

	    public boolean getReadAccess() {return readAccess;}  
	    public void setReadAccess(boolean access) {this.readAccess = access;} 
	    
	    public boolean getWriteAccess() {return writeAccess;}  
	    public void setWriteAccess(boolean access) {this.writeAccess = access;} 
	    
	    public boolean getAdminAccess() {return adminAccess;}  
	    public void setAdminAccess(boolean access) {this.adminAccess = access;} 
}
