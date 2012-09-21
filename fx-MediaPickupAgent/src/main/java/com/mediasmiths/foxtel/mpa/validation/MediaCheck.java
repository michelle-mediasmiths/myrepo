package com.mediasmiths.foxtel.mpa.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mediasmiths.foxtel.generated.MaterialExchange.FileMediaType;
import com.mediasmiths.foxtel.generated.MaterialExchange.MaterialType;
import com.mediasmiths.foxtel.mpa.MaterialEnvelope;
import com.mediasmiths.foxtel.mpa.Util;

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

			if (fileSizeMatches(mxf, media)) {
				logger.debug(String
						.format("Filesize of media file %s matches Material description %s",
								mxf.getAbsolutePath(), description.getFile()
										.getAbsolutePath()));
				if (checkSumMatches(mxf, media)) {
					logger.debug(String
							.format("Checksum of media file %s matches Material description %s",
									mxf.getAbsolutePath(), description
											.getFile().getAbsolutePath()));
					return true;
				} else {
					logger.warn(String
							.format("Checksum of media file %s did not match Material description %s",
									mxf.getAbsolutePath(), description
											.getFile().getAbsolutePath()));
					return false;
				}
			} else {
				logger.warn(String
						.format("Filesize of media file %s did not match Material description %s",
								mxf.getAbsolutePath(), description.getFile()
										.getAbsolutePath()));
				return false;
			}
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

	protected boolean checkSumMatches(File mxf, FileMediaType media) {

		// TODO : FX-29 calculate checksum
		try {

			String expectedMd5 = media.getChecksum().toString(16);
			String actualMd5 = DigestUtils.md5Hex(new FileInputStream(mxf));
			return (expectedMd5.equals(actualMd5));

		} catch (FileNotFoundException e) {
			logger.fatal(
					"FileNotFoundException calculating md5Hex (this really should not have happened as we are supposed to have checked the file exists already)",
					e);
			return false;
		} catch (IOException e) {
			logger.error("IOException calculating md5Hex", e);
			return true;
		}
	}

}
