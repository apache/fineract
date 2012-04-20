package org.mifosng.platform.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.data.ErrorResponse;

public class NewDataValidationException extends RuntimeException {

	private final List<ErrorResponse> validationErrors;
	
	public NewDataValidationException(final String message) {
		super(message);
		this.validationErrors = new ArrayList<ErrorResponse>();
	}
	
	public NewDataValidationException(List<ErrorResponse> errors, final String message) {
		super(message);
		this.validationErrors = errors;
	}

	public List<ErrorResponse> getValidationErrors() {
		return validationErrors;
	}
}