package com.mediasmiths.foxtel.pathresolver;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PathResolver
{
	//eg /storage
	final String nixPathToStorage;
	//eg f:\
	final String winPathToStorage;
	//eg \\foxtel\storage
	final String uncPathToStorage;

	private final static Logger log = Logger.getLogger(PathResolver.class);
	
	public enum PathType
	{
		NIX, WIN, UNC;
	}
	
	@Inject
	public PathResolver(@Named("nix.path.to.storage") String nixPath, @Named("win.path.to.storage") String winPath,@Named("unc.path.to.storage") String uncPath){
		this.nixPathToStorage=nixPath;
		this.winPathToStorage=winPath;
		this.uncPathToStorage=uncPath;
	}
	
	/**
	 * Transforms a path into a windows path, the format of the path should be specified by the path type argument
	 * @param pt
	 * @param path
	 * @return
	 */
	public String winPath(PathType pt, String path){
		log.debug(String.format("transforming %s path %s to windows Path", pt.toString(), path));
		String ret;
		
		switch (pt)
		{
			case NIX:
				ret =  winPathToStorage + path.substring(nixPathToStorage.length()).replace('/','\\');
				break;
			case WIN:
				ret =  path;
				break;				
			case UNC:
				ret = winPathToStorage + path.substring(uncPathToStorage.length());
				break;
			default :
				log.error("Unknown path type");
				ret = path;
				break;

		}
		log.info(String.format("transfored %s path %s to unc Path %s", pt.toString(), path,ret));	
		return ret;
	}
	

	/**
	 * Transforms path into a unc path, the format of path should be specified by the path type argument
	 * 
	 * @param pt
	 * @param path
	 * @return
	 */
	public String uncPath(PathType pt, String path)
	{
		
		log.debug(String.format("transforming %s path %s to unc Path", pt.toString(), path));
		String ret;
		
		switch (pt)
		{
			case NIX:
				ret =  uncPathToStorage + path.substring(nixPathToStorage.length()).replace('/','\\');
				break;
			case WIN:
				ret =  uncPathToStorage + path.substring(winPathToStorage.length());
				break;				
			case UNC:
				ret = path;
				break;
			default :
				log.error("Unknown path type");
				ret = path;
				break;

		}
		log.info(String.format("transfored %s path %s to unc Path %s", pt.toString(), path,ret));	
		return ret;
	}
	
	/**
	 * gives the path of inner relative to outer, assumers that inner is a subdirectory\file in outer
	 * 
	 * eg
	 * 
	 * getRelativePath("/my/storage/","/my/storage/path/to/resource"
	 * 
	 * returns "path/to/resource"
	 * 
	 * this will NOT work with arbitrary paths
	 * 
	 * @param outer
	 * @param inner
	 */
	public String getRelativePath(String outer, String inner){
		log.debug(String.format("getting relative path from %s to %s", outer, inner));
		
		//assumes that the passed path is indeed a sub resource of the medialocation path! wont work otherwise
		String relativePathToFile = inner.substring(outer.length());
		
		log.info(String.format("found relative path %s from %s to %s",relativePathToFile, outer, inner));
		
		return relativePathToFile;
	}
}
