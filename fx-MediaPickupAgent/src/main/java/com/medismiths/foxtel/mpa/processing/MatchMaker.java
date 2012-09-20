package com.medismiths.foxtel.mpa.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.medismiths.foxtel.mpa.MaterialEnvelope;

/**
 * 
 * Tracks MXF files that dont yet have xml and xml files that dont yet have mxfs
 * 
 * When prompted to match an mxf or xml will look for their companion and create
 * the corresponding pending import
 * 
 */
@Singleton
public class MatchMaker {

	private static Logger logger = Logger.getLogger(MatchMaker.class);

	// the mxfs and xmls collections hold files which we have seen
	// but have not
	// yet processed as they are still awaiting a companion file

	private final Set<UnmatchedFile> mxfs = new HashSet<UnmatchedFile>();
	private final Map<UnmatchedFile, MaterialEnvelope> xmls = new HashMap<UnmatchedFile, MaterialEnvelope>();

	@Inject
	public MatchMaker() {

	}

	/**
	 * Matches a Material with its mxf file
	 * 
	 * @param envelope
	 * @return a File object referencing this materials media, or null if it has
	 *         not been seen yet
	 */
	public synchronized String matchXML(MaterialEnvelope envelope) {

		String xmlAbsolutePath = envelope.getFile().getAbsolutePath();
		String mxfAbsolutePath = swapExtensionFor(xmlAbsolutePath,"mxf");
		
		UnmatchedFile mxfFile = new UnmatchedFile(mxfAbsolutePath);

		if (mxfs.contains(mxfFile)) {
			// remove from list of as yet unmatched mxfs
			mxfs.remove(mxfFile);

			return mxfFile.getFilePath();

		} else {
			logger.info(String.format(
					"Have not yet seen the media file for %s", envelope
							.getFile().getAbsolutePath()));
			logger.debug(String.format("Have seen the xml file %s",xmlAbsolutePath));
			// add xml to list of seen xmls
			xmls.put(
					new UnmatchedFile(System.currentTimeMillis(), xmlAbsolutePath), envelope);
			return null;
		}

	}

	/**
	 * Matches an mxf file with its Material description
	 * 
	 * @param mxf
	 *            - A file object representing the mxf
	 * @return - A MaterialEnvelope containing the material describing this mxf,
	 *         or null if it has not been seen yet
	 */
	public synchronized MaterialEnvelope matchMXF(File mxf) {

		String mxfAbsolutePath = mxf.getAbsolutePath();
		String xmlAbsolutePath = swapExtensionFor(mxfAbsolutePath,"xml");
		UnmatchedFile xmlFile = new UnmatchedFile(xmlAbsolutePath);

		// look for the xml file corresponding to this mxf
		if (xmls.containsKey(xmlFile)) {
			logger.info(String.format(
					"found an xml file %s for media file file %s", xmlAbsolutePath, mxfAbsolutePath));

			MaterialEnvelope material = xmls.get(xmlFile);
			// remove from list of of as yet unmatched xmls
			xmls.remove(xmlFile);

			return material;
		} else {
			logger.info(String.format("Have not yet seen the xml file for %s",
					mxf.getAbsolutePath()));
			logger.debug(String.format("Have seen the mxf file %s",mxfAbsolutePath));
			// add mxf to list of seen mxfs
			mxfs.add(new UnmatchedFile(System.currentTimeMillis(), mxfAbsolutePath));
			return null;
		}
	}

	public String swapExtensionFor(String abspath, String newextension) {
		String basename = FilenameUtils.getBaseName(abspath);
		String path = FilenameUtils.getFullPath(abspath);
		
		
		return ( path
				+ basename
				+ FilenameUtils.EXTENSION_SEPARATOR + newextension);
	}

	/**
	 * finds unmatched Material descriptions, purges them from the list of seen
	 * xmls and returns them for handling by the UnmatchedMaterialProcessor
	 * 
	 * @return
	 */
	public synchronized Collection<MaterialEnvelope> purgeUnmatchedMessages(
			long olderThan) {

		Map<UnmatchedFile, MaterialEnvelope> old = new HashMap<UnmatchedFile, MaterialEnvelope>();

		for (Entry<UnmatchedFile, MaterialEnvelope> entry : xmls.entrySet()) {
			long now = System.currentTimeMillis();
			long then = entry.getKey().getTimeSeen();
			long age = now - then;
			logger.trace(String.format("Saw file %s %d millis ago", entry
					.getKey().getFilePath(), age));
			if (age > olderThan) {
				old.put(entry.getKey(), entry.getValue());
			}
		}

		for (Entry<UnmatchedFile, MaterialEnvelope> entry : old.entrySet()) {
			xmls.remove(entry.getKey());
		}

		return old.values();

	}

	/**
	 * finds unmatched mxf files seen more than olderThan millis ago
	 */
	public synchronized Collection<UnmatchedFile> purgeUnmatchedMXFs(
			long olderThan) {

		List<UnmatchedFile> old = new ArrayList<UnmatchedFile>();

		// find old unmatched files
		for (UnmatchedFile mxf : mxfs) {

			long now = System.currentTimeMillis();
			long then = mxf.getTimeSeen();
			long age = now - then;

			logger.trace(String.format("Saw file %s %d millis ago", mxf
					.getFilePath(), age));
			if (age > olderThan) {
				old.add(mxf);
			}
		}

		// remove them from the list
		for (UnmatchedFile mxf : old) {
			mxfs.remove(mxf);
		}

		return old;
	}


}
