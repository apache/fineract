package org.mifosng.ui;

import org.mifosng.data.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

public class Jax2bRestClientException extends RestClientException {

	private final HttpStatus statusCode;
	private final MediaType contentType;
	private final ErrorResponse errorResponse;

	public Jax2bRestClientException(final HttpStatus statusCode,
			final MediaType contentType, final ErrorResponse errorResponse) {
		super(statusCode.name() + ": on client http rest request.");
		this.statusCode = statusCode;
		this.contentType = contentType;
		this.errorResponse = errorResponse;
	}

	public HttpStatus getStatusCode() {
		return this.statusCode;
	}

	public MediaType getContentType() {
		return this.contentType;
	}

	public ErrorResponse getErrorResponse() {
		return this.errorResponse;
	}

}
