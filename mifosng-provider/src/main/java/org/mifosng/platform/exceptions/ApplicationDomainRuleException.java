package org.mifosng.platform.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.data.ErrorResponse;

public class ApplicationDomainRuleException extends RuntimeException {

	private final List<ErrorResponse> errors;
	
	public ApplicationDomainRuleException(final String message) {
		super(message);
		this.errors = new ArrayList<ErrorResponse>();
	}
	
	public ApplicationDomainRuleException(List<ErrorResponse> errors, final String message) {
		super(message);
		this.errors = errors;
	}
	
	public ApplicationDomainRuleException(List<ErrorResponse> errors) {
		this.errors = errors;
	}

	public List<ErrorResponse> getErrors() {
		return errors;
	}
}