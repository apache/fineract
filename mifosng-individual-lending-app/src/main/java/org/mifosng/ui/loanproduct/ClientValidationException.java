package org.mifosng.ui.loanproduct;

import java.util.List;

import org.mifosng.data.ErrorResponse;

public class ClientValidationException extends RuntimeException {

	private final List<ErrorResponse> validationErrors;

	public ClientValidationException(List<ErrorResponse> validationErrors) {
		super("Validation errors exist");
		this.validationErrors = validationErrors;
	}

	public List<ErrorResponse> getValidationErrors() {
		return validationErrors;
	}
}
