package com.mediasmiths.foxtel.agent;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class WatchFolders extends ArrayList<WatchFolder> {

	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(WatchFolders.class);
	
	public WatchFolders(){
		super();
	}
	
	public WatchFolders(List<String> sourcePaths){
		super();
		
		for(String s : sourcePaths){
			this.add(new WatchFolder(s));
		}
	}
	
	public boolean isAo(String src){
		
		for(WatchFolder f : this){
			if(f.getSource().equals(src)){
				log.trace(String.format("%s eq %s",f.getSource(),src));
				return f.isAO();
			}
			else{
				log.trace(String.format("%s neq %s",f.getSource(),src));
			}
		}
		
		log.warn(String.format("non recognised source directory %s passed as argument to isAo()", src));
		return false; 
		
	}
	
	public String destinationFor(String src){
				
		for(WatchFolder f : this){
			
			if(f.getSource().equals(src)){
				log.trace(String.format("%s eq %s",f.getSource(),src));
				return f.getDelivery();
			}
			else{
				log.trace(String.format("%s neq %s",f.getSource(),src));
			}
		}
		
		log.error(String.format("No delivery location configured for %s",src));
		throw new IllegalArgumentException(String.format("No delivery location configured for %s",src));
		
	}
	
	@Override
	public boolean add(WatchFolder watchFolder){
		log.debug(String.format("Adding watchfolder source: %s  delivery %s isAo %b ",watchFolder.getSource(), watchFolder.getDelivery(), watchFolder.isAO()));
		return super.add(watchFolder);
	}
	
}