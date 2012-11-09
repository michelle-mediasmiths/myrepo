package com.mediasmiths.foxtel.fs.service;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.fs.model.DeleteRequest;
import com.mediasmiths.foxtel.fs.model.DeleteResponse;
import com.mediasmiths.foxtel.fs.model.MoveRequest;
import com.mediasmiths.foxtel.fs.model.MoveResponse;

public class FSRestServiceImpl implements FSRestService
{

	@Inject
	@Named("nix.path.to.storage")
	private String storageRoot;

	private final static Logger log = Logger.getLogger(FSRestService.class);

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

			if (to.isFile() && to.exists())
			{
				log.error(String.format("Target file %s for move already exists", to.getAbsolutePath()));
				throw new FSAdapterException("Target file for move already exists");
			}

			if (from.isDirectory() && to.isFile())
			{
				log.error(String.format(
						"Target file %s is a file but source was a folder %s",
						to.getAbsolutePath(),
						from.getAbsolutePath()));
				throw new FSAdapterException("Target path of a directory move is a file");
			}

			if (from.isDirectory())
			{
				try
				{
					FileUtils.moveDirectoryToDirectory(from, to, true);
					log.info(String.format("Move from %s to %s complete", from.getAbsolutePath(), to.getAbsolutePath()));
					return new MoveResponse(true);
				}
				catch (IOException e)
				{
					String message = String.format(
							"Error moving directory %s to %s",
							from.getAbsolutePath(),
							to.getAbsolutePath());
					log.error(message, e);
					throw new FSAdapterException(message, e);
				}
			}
			else
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

		}
		else
		{
			throw new FSAdapterException(String.format(
					"Not allowed to operate on one of the specified paths (%s %s)",
					mr.getFrom(),
					mr.getTo()));
		}
	}

}
