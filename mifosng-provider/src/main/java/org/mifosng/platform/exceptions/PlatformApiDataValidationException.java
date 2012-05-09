package org.mifosng.platform.exceptions;

import java.util.List;

import org.mifosng.data.ApiParameterError;

public class PlatformApiDataValidationException extends RuntimeException {

	private final String globalisationMessageCode;
	private final String defaultUserMessage;
	private final List<ApiParameterError> errors;

	public PlatformApiDataValidationException(final String globalisationMessageCode, final String defaultUserMessage, List<ApiParameterError> errors) {
		this.globalisationMessageCode = globalisationMessageCode;
		this.defaultUserMessage = defaultUserMessage;
		this.errors = errors;
	}
	
	public String getGlobalisationMessageCode() {
		return globalisationMessageCode;
	}

	public String getDefaultUserMessage() {
		return defaultUserMessage;
	}

	public List<ApiParameterError> getErrors() {
		return errors;
	}
}
