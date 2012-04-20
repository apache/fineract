package org.mifosng.oauth;

import org.springframework.security.oauth.provider.token.OAuthProviderToken;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenServices;

/**
 * This is just a temporary hack, should pass oauth access token, access secret, consumer key, consumer secret to get access to authenticated user permissions 
 */
public interface CustomOAuthProviderTokenServices extends OAuthProviderTokenServices {
	
	OAuthProviderToken getTokenByNonEncodedKey(String oauthToken);
	
	void removeTokenByNonEncodedKey(String oauthToken);
	
}
