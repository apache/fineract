package org.mifosng.ui.reporting;

import java.net.URI;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth.consumer.client.OAuthRestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

@Service
public class ReportingRestOperationsImpl implements ReportingRestOperations {

	private final OAuthRestTemplate oauthRestServiceTemplate;
	private final ApplicationConfigurationService applicationConfigurationService;

	@Autowired
	public ReportingRestOperationsImpl(
			final OAuthRestTemplate oauthRestServiceTemplate, final ApplicationConfigurationService applicationConfigurationService) {
		this.oauthRestServiceTemplate = oauthRestServiceTemplate;
		this.applicationConfigurationService = applicationConfigurationService;
	}

	private HttpEntity<Object> emptyRequest() {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		HttpEntity<Object> requestEntity = new HttpEntity<Object>(
				requestHeaders);
		return requestEntity;
	}
	
	private String getBaseServerUrl() {
		return this.applicationConfigurationService.retrieveOAuthProviderDetails().getProviderBaseUrl();
	}

	@Override
	public void hackToForceAuthentication() {
		try {
			URI restUri = URI
					.create(getBaseServerUrl().concat("api/v1/reports/forceauth"));

			this.oauthRestServiceTemplate.exchange(restUri, HttpMethod.GET, emptyRequest(), null);
		} catch (HttpStatusCodeException e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}
}