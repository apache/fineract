package org.mifosng.ui.configuration;

public class OauthSettingsFormBean {

	private String oauthProviderUrl;
	private String sharedSecret;
	private String consumerkey;

	public String getOauthProviderUrl() {
		return oauthProviderUrl;
	}

	public void setOauthProviderUrl(String oauthProviderUrl) {
		this.oauthProviderUrl = oauthProviderUrl;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	public String getConsumerkey() {
		return consumerkey;
	}

	public void setConsumerkey(String consumerkey) {
		this.consumerkey = consumerkey;
	}
}