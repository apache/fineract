package org.mifosng.ui;

import java.net.URI;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;
import org.springframework.security.oauth.consumer.client.OAuthRestTemplate;
import org.springframework.stereotype.Service;

@Service(value = "commonRestOperations")
public class CommonRestOperationsImpl implements CommonRestOperations {

	private OAuthRestTemplate oauthRestServiceTemplate;
	private final ApplicationConfigurationService applicationConfigurationService;

	@Autowired
	public CommonRestOperationsImpl(
			final OAuthRestTemplate oauthRestServiceTemplate,
			final ApplicationConfigurationService applicationConfigurationService) {
		this.oauthRestServiceTemplate = oauthRestServiceTemplate;
		this.applicationConfigurationService = applicationConfigurationService;
	}

	private String getBaseServerUrl() {
		return this.applicationConfigurationService
				.retrieveOAuthProviderDetails().getProviderBaseUrl();
	}

	@Override
	public void logout(String accessToken) {
		URI restUri = URI.create(getBaseServerUrl()
				.concat("api/protected/user/").concat(accessToken)
				.concat("/signout"));

		oauthRestServiceTemplate.exchange(restUri, HttpMethod.GET,
				emptyRequest(), null);
	}

	@Override
	public void updateProtectedResource(ProtectedResourceDetails resource) {

		this.oauthRestServiceTemplate = new OAuthRestTemplate(resource);
	}

	private HttpEntity<Object> emptyRequest() {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		HttpEntity<Object> requestEntity = new HttpEntity<Object>(requestHeaders);
		return requestEntity;
	}
}