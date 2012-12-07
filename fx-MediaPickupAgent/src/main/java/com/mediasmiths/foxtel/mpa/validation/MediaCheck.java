package com.mediasmiths.foxtel.mpa.validation;

import java.io.File;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.mpa.MediaEnvelope;

public class MediaCheck {

	private static Logger logger = Logger.getLogger(MediaCheck.class);

	@Inject
	public MediaCheck() {
	}

	public boolean mediaCheck(File mxf, MediaEnvelope description) {
		return true;
		//dont validate file size or checksum
	
		/*
		// check mxf matched descriptioin.
		MaterialType material = Util.getMaterialTypeForMaterial(description
				.getMessage());

		if (!(material.getMedia() instanceof FileMediaType)) {
			logger.error("Unknown media type");
			return false;
		} else {
			FileMediaType media = (FileMediaType) material.getMedia();

			if (media.getFileSize() != null) {
				if (fileSizeMatches(mxf, media)) {
					logger.debug(String
							.format("Filesize of media file %s matches Material description %s",
									mxf.getAbsolutePath(), description
											.getFile().getAbsolutePath()));

					if (media.getChecksum() != null) {
						return validateChecksum(mxf, description, media);
					} else {
						return true; // no checksum provided to validate against
					}
				} else {
					logger.warn(String
							.format("Filesize of media file %s did not match Material description %s",
									mxf.getAbsolutePath(), description
											.getFile().getAbsolutePath()));
					return false;
				}
			} else { //no filesize provided to validate against
				if (media.getChecksum() != null) {
					return validateChecksum(mxf, description, media);
				} else {
					return true;
				}
			}
		}
		*/
	}

	/*
	
	private boolean validateChecksum(File mxf, MediaEnvelope description,
			FileMediaType media) {
		if (checkSumMatches(mxf, media)) {
			logger.debug(String
					.format("Checksum of media file %s matches Material description %s",
							mxf.getAbsolutePath(), description.getFile()
									.getAbsolutePath()));
			return true;
		} else {
			logger.warn(String
					.format("Checksum of media file %s did not match Material description %s",
							mxf.getAbsolutePath(), description.getFile()
									.getAbsolutePath()));
			return false;
		}
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

	private static final int WIDTH_OF_MD5_HASH = 32;
	private static final int HEX_RADIX = 16;

	protected boolean checkSumMatches(File mxf, FileMediaType media) {

		try {

			String expectedMd5 = media.getChecksum().toString(HEX_RADIX);
			// zero pad until correct length for md5 digest

			while (expectedMd5.length() < WIDTH_OF_MD5_HASH) {
				expectedMd5 = "0" + expectedMd5;
			}

			String actualMd5 = DigestUtils.md5Hex(new FileInputStream(mxf));

			if (expectedMd5.equals(actualMd5)) {
				logger.debug(String.format(
						"expected md5 for %s - %s matched actual md5 %s",
						mxf.getAbsolutePath(), expectedMd5, actualMd5));
				return true;
			} else {
				logger.warn(String.format(
						"expected md5 for %s - %s did not match actual md5 %s",
						mxf.getAbsolutePath(), expectedMd5, actualMd5));
				return false;
			}

		} catch (IOException e) {
			logger.error("IOException calculating md5Hex", e);
			return false;
		}
	}
*/
}
