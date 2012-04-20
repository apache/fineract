package org.mifosng.configuration;

public interface ApplicationConfigurationService {

	OAuthProviderDetails retrieveOAuthProviderDetails();

	void update(OAuthProviderDetails newDetails);

}
