package org.mifosng.ui;

import java.io.IOException;

import org.mifosng.data.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

public class JaxRs311ErrorResponseHandler implements ResponseErrorHandler {

	private final ResponseErrorHandler delegateResponseErrorHandler;

	public JaxRs311ErrorResponseHandler(
			final ResponseErrorHandler delegateResponseErrorHandler) {
		this.delegateResponseErrorHandler = delegateResponseErrorHandler;
	}

	@Override
	public boolean hasError(final ClientHttpResponse response) throws IOException {
		return this.delegateResponseErrorHandler.hasError(response);
	}

	@Override
	public void handleError(final ClientHttpResponse response) throws IOException {

		HttpStatus statusCode = response.getStatusCode();
		MediaType contentType = response.getHeaders().getContentType();

		Jaxb2RootElementHttpMessageConverter converter = new Jaxb2RootElementHttpMessageConverter();
		ErrorResponse errorResponse = (ErrorResponse) converter.read(
				ErrorResponse.class, response);

		switch (statusCode.series()) {
		case CLIENT_ERROR:
			throw new Jax2bRestClientException(statusCode, contentType, errorResponse);
		case SERVER_ERROR:
			throw new Jax2bRestServerException(statusCode, contentType,
					errorResponse);
		default:
			throw new RestClientException("Unknown status code [" + statusCode
					+ "]");
		}
	}

}
