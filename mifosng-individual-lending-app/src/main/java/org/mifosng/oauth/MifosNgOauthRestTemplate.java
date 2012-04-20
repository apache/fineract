package org.mifosng.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth.consumer.ProtectedResourceDetailsService;
import org.springframework.security.oauth.consumer.client.OAuthRestTemplate;
import org.springframework.stereotype.Service;

@Service(value="oauthRestServiceTemplate")
public class MifosNgOauthRestTemplate extends OAuthRestTemplate {

	@Autowired
	public MifosNgOauthRestTemplate(ProtectedResourceDetailsService protectedResourceDetailsService) {
		super(protectedResourceDetailsService.loadProtectedResourceDetailsById("protectedMifosNgServices"));
	}

}
