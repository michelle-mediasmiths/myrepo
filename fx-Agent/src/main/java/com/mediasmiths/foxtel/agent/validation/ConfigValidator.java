package com.mediasmiths.foxtel.agent.validation;

import com.google.inject.Inject;
import org.apache.log4j.Logger;

import java.io.File;

public class ConfigValidator {

	private static Logger logger = Logger.getLogger(ConfigValidator.class);

	@Inject
	public ConfigValidator()
			throws ConfigValidationFailureException {

		boolean anyFailures = false;

		if (anyFailures) {
			onFailure();
		}
	}

	protected final void onFailure() throws ConfigValidationFailureException {
		logger.fatal("There are config validation failures");
		throw new ConfigValidationFailureException(); // by throwing this
														// exception guice will
														// not construct a
														// PlaceholderManager
														// (which required a
														// ConfigValidator)
	}

	protected final void configValidationPasses(String name, String value) {
		logger.info(String.format("Using %s for %s VALIDATES", value, name));
	}

	protected final void configValidationFails(String name, String value) {
		logger.error(String
				.format("Do not have read + write permissions on %s, check config value %s",
						value, name));
	}

	protected final boolean haveReadWritePermissions(String folderPath) {

		File folder = new File(folderPath);
		return (folder.canRead() && folder.canWrite());
	}

}
