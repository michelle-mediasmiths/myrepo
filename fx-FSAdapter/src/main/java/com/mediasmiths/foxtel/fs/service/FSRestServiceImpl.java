package com.mediasmiths.foxtel.fs.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.fs.model.DeleteRequest;
import com.mediasmiths.foxtel.fs.model.DeleteResponse;
import com.mediasmiths.foxtel.fs.model.MoveRequest;
import com.mediasmiths.foxtel.fs.model.MoveResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.IOException;

public class FSRestServiceImpl implements FSRestService
{

	@Inject
	@Named("nix.path.to.storage")
	private String storageRoot;

	private final static Logger log = Logger.getLogger(FSRestService.class);

	@Override
	@PUT
	@Path("/cleanup")
	@Produces("application/xml")
	public boolean cleanup(String filepath) throws FSAdapterException
	{
		log.info(String.format("Received cleanup request for path %s", filepath));
		
		int fileIndex = 1;
		int filesDeleted = 0;
		
		boolean filesRemaining = true;
		String currentFile = filepath;
		int fileExtensionIndex = filepath.lastIndexOf(".");
		
		//Remove file and all assocaited filenames (filename_1 ... filename_x)
		
		while (filesRemaining)
		{
			if (pathAllowed(currentFile))
			{
	
				File f = new File(currentFile);
	
				if (!f.exists())
				{
					log.info(String.format("file %s does not exist, exiting cleanup", currentFile));
					filesRemaining = false;
				}
				else
				{
					boolean deleted = f.delete();
	
					if (deleted)
					{
						log.info(String.format("Deleted file %s", f.getAbsolutePath()));
						filesDeleted ++;
					}
					else
					{
						log.error(String.format("Failed to delete file %s", f.getAbsolutePath()));
					}
	
					currentFile = getIndexedFilePath(filepath, fileIndex, fileExtensionIndex);
					fileIndex ++;
				}
			}
			else
			{
				throw new FSAdapterException(String.format("Not allowed to operate on specified path (%s)", currentFile));
			}
		}
		
		return (filesDeleted > 0);
	}
	
	@Override
	@PUT
	@Path("/selectMostRecent")
	@Produces("application/xml")
	public boolean selectMostRecent(String filepath) throws FSAdapterException
	{
		log.info(String.format("Received selectMostRecent request for path %s", filepath));
		
		// Select file with highest index (eg filename_x) and rename to filename
		// Remove all other files
		
		int highestIndex = 0;
		
		boolean filesRemaining = true;
		String currentFile = filepath;
		int fileExtensionIndex = filepath.lastIndexOf(".");
		
		//Remove file and all assocaited filenames (filename_1 ... filename_x)
		
		while (filesRemaining)
		{
			if (pathAllowed(currentFile))
			{
				File f = new File(currentFile);
	
				if (!f.exists())
				{
					filesRemaining = false;
					highestIndex--; //file at filename_highestIndex doesnt exist, so decrement highestIndex
				}
				else
				{
					highestIndex ++;
					currentFile = getIndexedFilePath(filepath, highestIndex, fileExtensionIndex);
				}
			}
			else
			{
				throw new FSAdapterException(String.format("Not allowed to operate on specified path (%s)", currentFile));
			}
		}
		
		for (int fileIndex = 1; fileIndex < highestIndex ; fileIndex++)
		{
			currentFile = getIndexedFilePath(filepath, fileIndex, fileExtensionIndex);

			if (pathAllowed(currentFile))
			{
				File f = new File(currentFile);
	
				if (!f.exists())
				{
					log.warn(String.format("file %s does not exist, cannot delete", currentFile));
				}
				else
				{
					boolean deleted = f.delete();
					
					if (deleted)
					{
						log.info(String.format("Deleted file %s", f.getAbsolutePath()));
					}
					else
					{
						log.error(String.format("Failed to delete file %s", f.getAbsolutePath()));
					}
					
					currentFile = getIndexedFilePath(filepath, fileIndex, fileExtensionIndex);
				}
			}
			else
			{
				throw new FSAdapterException(String.format("Not allowed to operate on specified path (%s)", currentFile));
			}
		}

		currentFile = getIndexedFilePath(filepath, highestIndex, fileExtensionIndex);
		File mostRecentFile = new File(currentFile);
		File originalFile = new File(filepath);

		log.info("Deleting "+originalFile);
		DeleteRequest dr = new DeleteRequest();
		dr.setPath(filepath);
		deleteFile(dr);
		log.info("Moving "+currentFile+" to "+filepath);

		return moveFileToFile(mostRecentFile,originalFile).isSuccessful();
	}


	private String getIndexedFilePath(final String filepath, final int fileCount, final int fileExtensionIndex)
	{
		return (filepath.substring(0, fileExtensionIndex) + "_" + fileCount + filepath.substring(fileExtensionIndex, filepath.length()));
	}


	@Override
	@PUT
	@Path("/delete")
	@Produces("application/xml")
	public DeleteResponse deleteFile(DeleteRequest dr) throws FSAdapterException
	{

		log.info(String.format("Received delete request for path %s", dr.getPath()));

		if (pathAllowed(dr.getPath()))
		{

			File f = new File(dr.getPath());

			if (!f.exists())
			{
				log.warn("received delete request for non existent file");
				// returning a sucess status as the callers intent was for the file to no longer exists,
				// it does not exists, therefore the caller should be satisfied
				return new DeleteResponse(true);
			}
			else
			{
				if (f.isDirectory())
				{
					try
					{
						FileUtils.deleteDirectory(f);
						log.info(String.format("Deleted directory %s", f.getAbsolutePath()));
						return new DeleteResponse(true);
					}
					catch (IOException e)
					{
						log.error(String.format("IOException deleting file %s", f.getAbsolutePath()), e);
						return new DeleteResponse(false);
					}
				}
				else
				{

					boolean deleted = f.delete();

					if (deleted)
					{
						log.info(String.format("Deleted file %s", f.getAbsolutePath()));
					}
					else
					{
						log.error(String.format("Failed to delete file %s", f.getAbsolutePath()));
					}

					return new DeleteResponse(deleted);
				}
			}
		}
		else
		{
			throw new FSAdapterException(String.format("Not allowed to operate on specified path (%s)", dr.getPath()));
		}
	}


	@Override
	@GET
	@Path("/ping")
	@Produces("text/plain")
	public String ping()
	{
		return "ping";
	}


	/**
	 * checks if the specified path is under the configured storage root, as we dont want to act upon files not within it
	 *
	 * @param path
	 *
	 * @return
	 */
	private boolean pathAllowed(String path)
	{
		if (path.startsWith(storageRoot))
		{
			log.debug(String.format("allowed to operate on path %s", path));
			return true;
		}
		else
		{
			log.warn(String.format("allowed to operate on path %s", path));
			return false;
		}
	}


	@Override
	@PUT
	@Path("/move")
	@Produces("application/xml")
	public MoveResponse movefile(MoveRequest mr) throws FSAdapterException
	{
		log.info(String.format("Received move request from path %s to %s", mr.getFrom(), mr.getTo()));

		if (pathAllowed(mr.getFrom()) && pathAllowed(mr.getTo()))
		{
			File from = new File(mr.getFrom());
			File to = new File(mr.getTo());

			if (from.equals(to))
			{
				log.info(String.format("Move from %s to %s not required, source and destination are equal",
				                       from.getAbsolutePath(),
				                       to.getAbsolutePath()));
				return new MoveResponse(true);
			}

			if (to.isFile() && to.exists())
			{
				log.error(String.format("Target file %s for move already exists", to.getAbsolutePath()));
				throw new FSAdapterException("Target file for move already exists");
			}

			if (from.isDirectory() && to.isFile())
			{
				return moveDirectoryToFile(from, to);
			}

			if (from.isDirectory())
			{
				return moveDirectoryToDirectory(from, to);
			}
			else
			{
				String fromPath = from.getAbsolutePath();
				String toPath = to.getAbsolutePath();

				if (to.isDirectory() &&
				    FilenameUtils.getFullPathNoEndSeparator(fromPath).equals(FilenameUtils.getFullPathNoEndSeparator(toPath)))
				{
					return moveDirectoryToSelf(fromPath, toPath);
				}
				else if (to.isDirectory())
				{

					return moveFileToDirectory(from, to, fromPath, toPath);
				}
				else
				{

					return moveFileToFile(from, to);
				}
			}
		}
		else
		{
			throw new FSAdapterException(String.format("Not allowed to operate on one of the specified paths (%s %s)",
			                                           mr.getFrom(),
			                                           mr.getTo()));
		}
	}


	private MoveResponse moveDirectoryToSelf(final String fromPath, final String toPath)
	{
		log.info(String.format("Move from %s to %s is the same folder", fromPath, toPath));
		return new MoveResponse(true);
	}


	private MoveResponse moveFileToFile(final File from, final File to) throws FSAdapterException
	{
		try
		{
			FileUtils.moveFile(from, to);
			log.info(String.format("Move from %s to %s complete", from.getAbsolutePath(), to.getAbsolutePath()));
			return new MoveResponse(true);
		}
		catch (IOException e)
		{
			String message = String.format("Error moving file %s to %s", from.getAbsolutePath(), to.getAbsolutePath());
			log.error(message, e);
			throw new FSAdapterException(message, e);
		}
	}


	private MoveResponse moveFileToDirectory(final File from,
	                                         final File to,
	                                         final String fromPath,
	                                         final String toPath) throws FSAdapterException
	{
		log.debug(String.format("Moving file from %s to directory %s", fromPath, toPath));
		try
		{
			FileUtils.moveFileToDirectory(from, to, true);
			log.info(String.format("Move from %s to %s complete", from.getAbsolutePath(), to.getAbsolutePath()));
			return new MoveResponse(true);
		}
		catch (IOException e)
		{
			String message = String.format("Error moving file %s to %s", from.getAbsolutePath(), to.getAbsolutePath());
			log.error(message, e);
			throw new FSAdapterException(message, e);
		}
	}


	private MoveResponse moveDirectoryToDirectory(final File from, final File to) throws FSAdapterException
	{
		try
		{
			FileUtils.moveDirectoryToDirectory(from, to, true);
			log.info(String.format("Move from %s to %s complete", from.getAbsolutePath(), to.getAbsolutePath()));
			return new MoveResponse(true);
		}
		catch (IOException e)
		{
			String message = String.format("Error moving directory %s to %s", from.getAbsolutePath(), to.getAbsolutePath());
			log.error(message, e);
			throw new FSAdapterException(message, e);
		}
	}


	private MoveResponse moveDirectoryToFile(final File from, final File to) throws FSAdapterException
	{
		log.error(String.format("Target file %s is a file but source was a folder %s",
		                        to.getAbsolutePath(),
		                        from.getAbsolutePath()));
		throw new FSAdapterException("Target path of a directory move is a file");
	}
}
