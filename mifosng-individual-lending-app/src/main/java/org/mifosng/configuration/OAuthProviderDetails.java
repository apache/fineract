package org.mifosng.configuration;

public class OAuthProviderDetails {

	private final String requestTokenUrl;
	private final String userAuthorizationUrl;
	private final String accessTokenUrl;
	private final String sharedSecret;
	private final String consumerkey;
	private final String providerBaseUrl;

	public OAuthProviderDetails(String providerBaseUrl, String requestTokenUrl,
			String userAuthorizationUrl, String accessTokenUrl,
			String consumerkey, String sharedSecret) {
		this.providerBaseUrl = providerBaseUrl;
		this.requestTokenUrl = requestTokenUrl;
		this.userAuthorizationUrl = userAuthorizationUrl;
		this.accessTokenUrl = accessTokenUrl;
		this.consumerkey = consumerkey;
		this.sharedSecret = sharedSecret;
	}

	public OAuthProviderDetails(String oauthProviderUrl, String consumerkey,
			String sharedSecret) {
		this.providerBaseUrl = oauthProviderUrl;
		this.consumerkey = consumerkey;
		this.sharedSecret = sharedSecret;
		this.accessTokenUrl = "";
		this.userAuthorizationUrl = "";
		this.requestTokenUrl = "";
	}

	public String getRequestTokenUrl() {
		return requestTokenUrl;
	}

	public String getUserAuthorizationUrl() {
		return userAuthorizationUrl;
	}

	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public String getConsumerkey() {
		return consumerkey;
	}

	public String getProviderBaseUrl() {
		return providerBaseUrl;
	}
}