package com.mediasmiths.mayam.accessrights;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity  
@Table(name = "MayamChannelGroups", schema = "FoxtelAccessRights")  
public class MayamChannelGroups {
    @Id  
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id") 
    private long id;
    
    @Column(name = "channelName")  
    private String channelName; 
    
    @Column(name = "channelOwner")  
    private String channelOwner; 
    
    public long getId() {return id;}  
    public void setId(long id) {this.id = id;}  
   
    public String getChannelName() {return channelName;}  
    public void setChannelName(String channel) {this.channelName = channel;}  
    
    public String getChannelOwner() {return channelOwner;}  
    public void setChannelOwner(String owner) {this.channelOwner = owner;} 
}
