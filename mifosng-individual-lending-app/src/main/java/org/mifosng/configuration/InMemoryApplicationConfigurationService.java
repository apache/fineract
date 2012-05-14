package org.mifosng.configuration;

import org.springframework.stereotype.Service;

/**
 * When application goes down or rebooted, configuration values return to defaults provided here.
 */
@Service
public class InMemoryApplicationConfigurationService implements
		ApplicationConfigurationService {

	private String oauthProviderUrl = "http://localhost:8080/mifosng-provider/";
	private String requestTokenURL = "oauth/request_token";
	private String userAuthorizationURL = "oauth/confirm_access";
	private String accessTokenURL = "oauth/access_token";

	private String individualLendingResourceConsumerkey = "mifosng-ui-consumer-key";
	private String individualLendingConsumerSharedSecret = "testmifosng";

	public InMemoryApplicationConfigurationService() {
		//
	}

	@Override
	public OAuthProviderDetails retrieveOAuthProviderDetails() {

		String requestTokenFullUrl = this.oauthProviderUrl
				.concat(this.requestTokenURL);
		String userAuthorizationFullURL = this.oauthProviderUrl
				.concat(this.userAuthorizationURL);
		String accessTokenFullURL = this.oauthProviderUrl
				.concat(this.accessTokenURL);

		return new OAuthProviderDetails(this.oauthProviderUrl, requestTokenFullUrl,
				userAuthorizationFullURL, accessTokenFullURL,
				this.individualLendingResourceConsumerkey,
				this.individualLendingConsumerSharedSecret);
	}

	@Override
	public void update(OAuthProviderDetails newDetails) {
		this.oauthProviderUrl = newDetails.getProviderBaseUrl();
		if (!this.oauthProviderUrl.endsWith("/")) {
			this.oauthProviderUrl = this.oauthProviderUrl.concat("/");
		}
		
		this.individualLendingResourceConsumerkey = newDetails.getConsumerkey();
		this.individualLendingConsumerSharedSecret = newDetails.getSharedSecret();
	}

}