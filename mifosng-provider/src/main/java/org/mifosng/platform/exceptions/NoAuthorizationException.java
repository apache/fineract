package org.mifosng.platform.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.data.ErrorResponse;

public class NoAuthorizationException extends RuntimeException {

	private final List<ErrorResponse> authorizationErrors;
	
	public NoAuthorizationException(final String message) {
		super(message);
		this.authorizationErrors = new ArrayList<ErrorResponse>();
	}
	
	public NoAuthorizationException(List<ErrorResponse> authorizationErrors, final String message) {
		super(message);
		this.authorizationErrors = authorizationErrors;
	}

	public List<ErrorResponse> getAuthorizationErrors() {
		return authorizationErrors;
	}
}