package com.medismiths.foxtel.mpa.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.generated.MaterialExchange.FileMediaType;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;
import com.mediasmiths.foxtel.generated.MaterialExchange.MediaType;
import com.medismiths.foxtel.mpa.MaterialEnvelope;
import com.medismiths.foxtel.mpa.Util;

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

	private final MessageDigest digest; // for validating file checksums

	@Inject
	public MatchMaker(@Named("media.digest.algorithm") String digestAlgorithm) {
		digest = DigestUtils.getDigest(digestAlgorithm); // throws
															// IllegalArgumentException
															// if the supplied
															// algorithm doesnt
															// exist
	}

	/**
	 * Matches a Material with its mxf file
	 * 
	 * @param envelope
	 * @return a File object referencing this materials media, or null if it has
	 *         not been seen yet
	 */
	public synchronized File matchXML(MaterialEnvelope envelope) {

		String xmlAbsolutePath = envelope.getFile().getAbsolutePath();
		String basename = FilenameUtils.getBaseName(xmlAbsolutePath);
		String path = FilenameUtils.getPath(xmlAbsolutePath);
		UnmatchedFile mxfFile = new UnmatchedFile(new File(path
				+ IOUtils.DIR_SEPARATOR + basename
				+ FilenameUtils.EXTENSION_SEPARATOR + "mxf"));

		if (mxfs.contains(mxfFile)) {
			// remove from list of as yet unmatched mxfs
			mxfs.remove(mxfFile);

			if (mediaCheck(mxfFile.getFile(), envelope)) { // TODO should this
															// check be
															// performed here or
															// at the importer
															// stage?
				return mxfFile.getFile();
			} else {
				return null;
			}
		} else {
			logger.info(String.format(
					"Have not yet seen the media file for %s", envelope
							.getFile().getAbsolutePath()));
			// add xml to list of seen xmls
			xmls.put(
					new UnmatchedFile(System.currentTimeMillis(), envelope
							.getFile()), envelope);
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
		String basename = FilenameUtils.getBaseName(mxfAbsolutePath);
		String path = FilenameUtils.getPath(mxfAbsolutePath);
		UnmatchedFile xmlFile = new UnmatchedFile(new File(path
				+ IOUtils.DIR_SEPARATOR + basename
				+ FilenameUtils.EXTENSION_SEPARATOR + "xml"));

		// look for the xml file corresponding to this mxf
		if (xmls.containsKey(xmlFile)) {
			logger.info(String.format(
					"found an xml file %s for media file file %s", xmlFile
							.getFile().getAbsolutePath(), mxf.getAbsolutePath()));

			MaterialEnvelope material = xmls.get(xmlFile);
			// remove from list of of as yet unmatched xmls
			xmls.remove(xmlFile);

			return material;
		} else {
			logger.info(String.format("Have not yet seen the xml file for %s",
					mxf.getAbsolutePath()));
			// add mxf to list of seen mxfs
			mxfs.add(new UnmatchedFile(System.currentTimeMillis(), mxf));
			return null;
		}
	}

	private boolean mediaCheck(File mxf, MaterialEnvelope description) {
		// check mxf matched descriptioin.
		MaterialType material = Util.getMaterialTypeForMaterial(description
				.getMessage());

		if (!(material.getMedia() instanceof FileMediaType)) {
			logger.error("Unknown media type");
			return false;
		} else {
			FileMediaType media = (FileMediaType) material.getMedia();
			return fileSizeMatches(mxf, media) && checkSumMatches(mxf, media);
		}
		// TODO check file format? (could be quite difficult!)
	}

	protected boolean fileSizeMatches(File mxf, FileMediaType media) {

		long fileSize = mxf.length();
		long expectedSize = media.getFileSize().longValue();

		if (fileSize == expectedSize) {
			logger.debug(String.format(
					"File size of %s is %d expected size is %d",
					mxf.getAbsolutePath(), fileSize, expectedSize));
			return true;
		} else {
			logger.warn(String.format(
					"File size of %s is %d expected size is %d",
					mxf.getAbsolutePath(), fileSize, expectedSize));
			return false;
		}

	}

	protected boolean checkSumMatches(File mxf, FileMediaType media) {

		BigInteger checksum = media.getChecksum();

		try {
			if (digest.isEqual(checksum.toString(64).getBytes(),
					IOUtils.toByteArray(new FileInputStream(mxf)))) {
				// TODO : replace naive stupid implementation that reads the
				// entire file into memory (see java.nio)
				logger.debug(String.format("Checksum passes %s",
						mxf.getAbsolutePath()));
				return true;
			} else {
				logger.warn(String.format("Checksum failure %s",
						mxf.getAbsolutePath()));
				return false;
			}

		} catch (IOException e) {
			logger.error(
					String.format(
							"IOException calculating media checksum for %s, sanity check fails",
							mxf.getAbsolutePath()), e);
			return false;
		}
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
					.getKey().getFile().getAbsolutePath(), age));
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
					.getFile().getAbsolutePath(), age));
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

	class UnmatchedFile {

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			UnmatchedFile other = (UnmatchedFile) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (file == null) {
				if (other.file != null) {
					return false;
				}
			} else if (!file.equals(other.file)) {
				return false;
			}
			return true;
		}

		private final long timeSeen;
		private final File file;

		public UnmatchedFile(long timeSeen, File file) {
			this.timeSeen = timeSeen;
			this.file = file;
		}

		public UnmatchedFile(File file) {
			this(0L, file);
		}

		public long getTimeSeen() {
			return timeSeen;
		}

		public File getFile() {
			return file;
		}

		private MatchMaker getOuterType() {
			return MatchMaker.this;
		}
	}
}
