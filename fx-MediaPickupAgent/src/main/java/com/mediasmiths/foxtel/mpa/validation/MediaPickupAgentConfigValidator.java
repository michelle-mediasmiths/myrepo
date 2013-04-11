package com.mediasmiths.foxtel.mpa.validation;

import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.DELIVERY_ATTEMPT_COUNT;
import static com.mediasmiths.foxtel.mpa.MediaPickupConfig.MEDIA_COMPANION_TIMEOUT;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mediasmiths.foxtel.agent.validation.ConfigValidationFailureException;
import com.mediasmiths.foxtel.agent.validation.ConfigValidator;

public class MediaPickupAgentConfigValidator extends ConfigValidator {

	private static Logger logger = Logger
			.getLogger(MediaPickupAgentConfigValidator.class);

	
	@Inject
	public MediaPickupAgentConfigValidator(
			@Named(MEDIA_COMPANION_TIMEOUT) String companionTimeout,
			@Named(DELIVERY_ATTEMPT_COUNT) String deliveryAttemptCount
			)
			throws ConfigValidationFailureException {
		super();

		boolean anyFailures = false;

		if (isValidLong(companionTimeout)) {
			configValidationPasses(MEDIA_COMPANION_TIMEOUT, companionTimeout);
		} else {
			anyFailures = true;
			configValidationFails(MEDIA_COMPANION_TIMEOUT, companionTimeout);
		}
		
		if (isValidLong(deliveryAttemptCount)) {
			configValidationPasses(DELIVERY_ATTEMPT_COUNT, companionTimeout);
		} else {
			anyFailures = true;
			configValidationFails(DELIVERY_ATTEMPT_COUNT, companionTimeout);
		}




		if (anyFailures) {
			onFailure();
		}
	}

	private boolean isValidLong(String value) {
		try {
			Long l = Long.parseLong(value); // NOSONAR
			// sonar complains about this but I don't care, there is not really
			// a better way of making this check
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}

	}

	private boolean isValidInt(String value) {
		try {
			int i = Integer.parseInt(value); // NOSONAR
			return true;
		} catch (NumberFormatException nfe) {
			return false;

		}
	}

}
