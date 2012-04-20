package org.mifosng.oauth;

import org.springframework.security.oauth.provider.token.InMemoryProviderTokenServices;

public class RemovableInMemoryProviderTokenServices extends
		InMemoryProviderTokenServices {

	public void removeAccessToken(String acccesTokenValue) {
		super.removeToken(acccesTokenValue);
	}
}
