package com.mediasmiths.foxtel.pathresolver;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class PathResolver
{
	// eg /storage
	final List<String> nixPathsToStorage;
	// eg f:\
	final List<String> winPathsToStorage;
	// eg \\foxtel\storage
	final List<String> uncPathsToStorage;
	// eg ftp://user:pass@host/storage
	final List<String> ftpPathsToStorage;
	private final static Logger log = Logger.getLogger(PathResolver.class);

	public enum PathType
	{
		NIX, WIN, UNC, FTP;
	}

	@Inject
	public PathResolver(
			@Named("nix.paths.to.storage") String nixPath,
			@Named("win.paths.to.storage") String winPath,
			@Named("unc.paths.to.storage") String uncPath,
			@Named("ftp.paths.to.storage") String ftpPath)
	{

		this.nixPathsToStorage = Arrays.asList(nixPath.split(","));
		this.winPathsToStorage = Arrays.asList(winPath.split(","));
		this.uncPathsToStorage = Arrays.asList(uncPath.split(","));
		this.ftpPathsToStorage = Arrays.asList(ftpPath.split(","));

		if (nixPathsToStorage.size() != winPathsToStorage.size())
		{
			throw new IllegalArgumentException("Number of nix and win paths do not match");
		}

		if (nixPathsToStorage.size() != uncPathsToStorage.size())
		{
			throw new IllegalArgumentException("Number of nix and unc paths do not match");
		}

		if (nixPathsToStorage.size() != ftpPathsToStorage.size())
		{
			throw new IllegalArgumentException("Number of nix and ftp paths do not match");
		}

		if (winPathsToStorage.size() != uncPathsToStorage.size())
		{
			throw new IllegalArgumentException("Number of win and unc paths do not match");
		}

		if (winPathsToStorage.size() != ftpPathsToStorage.size())
		{
			throw new IllegalArgumentException("Number of win and ftp paths do not match");
		}

		if (uncPathsToStorage.size() != ftpPathsToStorage.size())
		{
			throw new IllegalArgumentException("Number of unc and ftp paths do not match");
		}
	}

	/**
	 * Transforms a path into a windows path, the format of the path should be specified by the path type argument
	 * 
	 * @param pt
	 * @param path
	 * @return
	 */
	public String winPath(PathType pt, String path) throws UnknownPathException
	{
		log.debug(String.format("transforming %s path %s to windows Path", pt.toString(), path));
		String ret;

		path = preprocessPath(pt,path);
		int index = getIndexForPath(pt, path);
		String winPathToStorage = winPathsToStorage.get(index);
		String nixPathToStorage = nixPathsToStorage.get(index);
		String uncPathToStorage = uncPathsToStorage.get(index);
		String ftpPathToStorage = ftpPathsToStorage.get(index);

		switch (pt)
		{
			case NIX:
				ret = winPathToStorage + path.substring(nixPathToStorage.length()).replace('/', '\\');
				break;
			case WIN:
				ret = path;
				break;
			case UNC:
				ret = winPathToStorage + path.substring(uncPathToStorage.length());
				break;
			case FTP:
				ret = winPathToStorage + path.substring(ftpPathToStorage.length()).replace('/', '\\');
				break;
			default:
				log.error("Unknown path type");
				ret = path;
				break;

		}
		log.info(String.format("transfored %s path %s to win Path %s", pt.toString(), path, ret));
		return ret;
	}

	private int getIndexForPath(PathType pt, String path) throws UnknownPathException
	{
		int index;

		switch (pt)
		{
			case FTP:
				index = getIndexForPath(ftpPathsToStorage, path);
				break;
			case NIX:
				index = getIndexForPath(nixPathsToStorage, path);
				break;
			case UNC:
				index = getIndexForPath(uncPathsToStorage, path);
				break;
			case WIN:
				index = getIndexForPath(winPathsToStorage, path);
				break;
			default:
				{
					log.fatal("Uncovered enum value in switch statement at PathResolver.getIndexForPath");
					throw new IllegalArgumentException("Unrecognised path type");
				}
		}

		if (index == -1)
		{
			throw new UnknownPathException(String.format("Path %s was not a reconigsed path of type %s", path, pt.toString()));
		}

		return index;
	}

	/**
	 * finds the index of the entry in paths that is the longest suffix of path. returns -1 if there are no suffixes of path in paths
	 * 
	 * @param paths
	 * @param path
	 * @return
	 */
	protected int getIndexForPath(List<String> paths, String path)
	{
		int widestMatchingPrefix = -1;
		int index = -1;
		if (log.isTraceEnabled())
		{
			log.trace(String.format("Fetching index to use for path %s",path));
		}
		
		for (int i = 0; i < paths.size(); i++)
		{
			String candidate = paths.get(i);
			
			if (log.isTraceEnabled())
			{	
				log.trace(String.format("Candidate %d is {%s}",i,candidate));
			}
			
			
			if (path.startsWith(candidate))
			{

				if (candidate.length() > widestMatchingPrefix)
				{
					if (log.isTraceEnabled())
					{
						log.debug(String.format(
								"Picked candidate %s (%d) as better prefix than candidate (%d) for path %s",
								candidate,
								i,
								index,
								path));
					}
					widestMatchingPrefix = candidate.length();
					index = i;
				}
			}
		}

		return index;
	}

	/**
	 * Transforms path into a unc path, the format of path should be specified by the path type argument
	 * 
	 * @param pt
	 * @param path
	 * @return
	 */
	public String uncPath(PathType pt, String path) throws UnknownPathException
	{

		log.debug(String.format("transforming %s path %s to unc Path", pt.toString(), path));
		String ret;

		path = preprocessPath(pt,path);
		int index = getIndexForPath(pt, path);
		String winPathToStorage = winPathsToStorage.get(index);
		String nixPathToStorage = nixPathsToStorage.get(index);
		String uncPathToStorage = uncPathsToStorage.get(index);
		String ftpPathToStorage = ftpPathsToStorage.get(index);

		switch (pt)
		{
			case NIX:
				ret = uncPathToStorage + path.substring(nixPathToStorage.length()).replace('/', '\\');
				break;
			case WIN:
				ret = uncPathToStorage + path.substring(winPathToStorage.length());
				break;
			case UNC:
				ret = path;
				break;
			case FTP:
				ret = uncPathToStorage + path.substring(ftpPathToStorage.length()).replace('/', '\\');
				break;
			default:
				log.error("Unknown path type");
				ret = path;
				break;

		}
		log.info(String.format("transfored %s path %s to unc Path %s", pt.toString(), path, ret));
		return ret;
	}
	
	/**
	 * Transforms path into a nix path, the format of path should be specified by the path type argument
	 * 
	 * @param pt
	 * @param path
	 * @return
	 */
	public String nixPath(PathType pt, String path) throws UnknownPathException
	{

		log.debug(String.format("transforming %s path %s to unc Path", pt.toString(), path));
		String ret;

		path = preprocessPath(pt,path);
		int index = getIndexForPath(pt, path);
		String winPathToStorage = winPathsToStorage.get(index);
		String nixPathToStorage = nixPathsToStorage.get(index);
		String uncPathToStorage = uncPathsToStorage.get(index);
		String ftpPathToStorage = ftpPathsToStorage.get(index);

		switch (pt)
		{
			case NIX:
				ret = path;
				break;
			case WIN:
				ret = nixPathToStorage + path.substring(winPathToStorage.length()).replace('\\', '/');;
				break;
			case UNC:
				ret = nixPathToStorage + path.substring(uncPathToStorage.length()).replace('\\', '/');;
				break;
			case FTP:
				ret = nixPathToStorage + path.substring(ftpPathToStorage.length());
				break;
			default:
				log.error("Unknown path type");
				ret = path;
				break;

		}
		log.info(String.format("transfored %s path %s to nix Path %s", pt.toString(), path, ret));
		return ret;
	}


	/**
	 * The paths from the user may need preprocessing in some fashion, this method is to handle that
	 * @param pt
	 * @param path
	 * @return
	 */
	protected String preprocessPath(PathType pt, String path)
	{
		switch (pt)
		{
			case FTP:
				{
					//ftp paths from the user will be of the form ftp://user:pass@host:port/path/to/file
					//return just the /path/to/file portion
					URI ftpUri = URI.create(path);
					return ftpUri.getPath();					
				}
			default:
				return path;
		}
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
	public String getRelativePath(String outer, String inner)
	{
		log.debug(String.format("getting relative path from %s to %s", outer, inner));

		// assumes that the passed path is indeed a sub resource of the medialocation path! wont work otherwise
		String relativePathToFile = inner.substring(outer.length());

		log.info(String.format("found relative path %s from %s to %s", relativePathToFile, outer, inner));

		return relativePathToFile;
	}
}
