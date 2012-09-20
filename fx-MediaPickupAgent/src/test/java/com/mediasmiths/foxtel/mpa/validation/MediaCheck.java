package com.mediasmiths.foxtel.mpa.validation;

import static com.medismiths.foxtel.mpa.MediaPickupConfig.MEDIA_DIGEST_ALGORITHM;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.generated.MaterialExchange.FileMediaType;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;
import com.medismiths.foxtel.mpa.MaterialEnvelope;
import com.medismiths.foxtel.mpa.Util;

public class MediaCheck {

	private static Logger logger = Logger.getLogger(MediaCheck.class);

	@Inject
	public MediaCheck() {

	}

	public boolean mediaCheck(File mxf, MaterialEnvelope description) {
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

		// TODO : FX-29 caluclate checksum

		BigInteger checksum = media.getChecksum();

		try {
			if (digest.isEqual(checksum.toString(64).getBytes(),
					IOUtils.toByteArray(new FileInputStream(mxf)))) {
				// TODO FX-29 replace naive stupid implementation that reads the
				// entire file into memory (see java.nio)

				// also messagedigest and checksum arnt really the same thing
				// are they, this whole method is likely to change
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

	@Named(MEDIA_DIGEST_ALGORITHM)
	private final MessageDigest digest = DigestUtils.getDigest("md5"); // for
																		// validating

}
