package org.mifosng.oauth;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth.provider.token.OAuthAccessProviderToken;
import org.springframework.security.oauth.provider.token.OAuthProviderToken;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenImpl;

public class CustomTokenServices implements CustomOAuthProviderTokenServices {
	
	private final ConcurrentHashMap<String, OAuthProviderTokenImpl> requestTokenStore = new ConcurrentHashMap<String, OAuthProviderTokenImpl>();
	private final RemovableInMemoryProviderTokenServices delegate;

	public CustomTokenServices(RemovableInMemoryProviderTokenServices delegate) {
		this.delegate = delegate;
	}

	@Override
	public OAuthProviderToken getToken(String token)
			throws AuthenticationException {
		return this.delegate.getToken(token);
	}

	@Override
	public OAuthProviderToken createUnauthorizedRequestToken(
			String consumerKey, String callbackUrl)
			throws AuthenticationException {
		return this.delegate.createUnauthorizedRequestToken(consumerKey, callbackUrl);
	}

	@Override
	public void authorizeRequestToken(String requestToken, String verifier,
			Authentication authentication) throws AuthenticationException {
		this.delegate.authorizeRequestToken(requestToken, verifier, authentication);
	}

	@Override
	public OAuthAccessProviderToken createAccessToken(String requestToken)
			throws AuthenticationException {
		OAuthProviderTokenImpl accessToken = (OAuthProviderTokenImpl)this.delegate.createAccessToken(requestToken);
		this.requestTokenStore.put(requestToken, accessToken);
		return accessToken;
	}

	@Override
	public OAuthProviderToken getTokenByNonEncodedKey(String oauthToken) {
		return this.requestTokenStore.get(oauthToken);
	}

	@Override
	public void removeTokenByNonEncodedKey(String oauthToken) {
		OAuthProviderTokenImpl accessToken = this.requestTokenStore.remove(oauthToken);
		if (accessToken != null) {
			String acccesTokenValue = accessToken.getValue();
			this.delegate.removeAccessToken(acccesTokenValue);
		} else {
			this.delegate.removeAccessToken(oauthToken);
		}
	}

}
