package org.mifosng.oauth;

import java.util.HashMap;
import java.util.Map;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.mifosng.configuration.OAuthProviderDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth.common.signature.SharedConsumerSecret;
import org.springframework.security.oauth.consumer.BaseProtectedResourceDetails;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;
import org.springframework.security.oauth.consumer.ProtectedResourceDetailsService;
import org.springframework.stereotype.Service;

@Service(value="mifosNgInMemoryResourceDetailsService")
public class MifosNgProtectedResourceDetailsService implements
		ProtectedResourceDetailsService, RefreshableProtectedResourceDetailsService {

	private Map<String, ProtectedResourceDetails> resourceDetailsStore = new HashMap<String, ProtectedResourceDetails>();
	private final ApplicationConfigurationService applicationConfigurationService;

	@Autowired
	public MifosNgProtectedResourceDetailsService(ApplicationConfigurationService applicationConfigurationService) {
		this.applicationConfigurationService = applicationConfigurationService;
		addHardcodedMifosNgResourceIdToStore();
	}

	private void addHardcodedMifosNgResourceIdToStore() {
		BaseProtectedResourceDetails individualLendingResourceDetails = new BaseProtectedResourceDetails();
		individualLendingResourceDetails.setId("protectedMifosNgServices");

		OAuthProviderDetails oauthProviderDetails = applicationConfigurationService
				.retrieveOAuthProviderDetails();

		individualLendingResourceDetails
				.setRequestTokenURL(oauthProviderDetails.getRequestTokenUrl());
		individualLendingResourceDetails
				.setUserAuthorizationURL(oauthProviderDetails
						.getUserAuthorizationUrl());
		individualLendingResourceDetails.setAccessTokenURL(oauthProviderDetails
				.getAccessTokenUrl());

		individualLendingResourceDetails.setConsumerKey(oauthProviderDetails
				.getConsumerkey());
		individualLendingResourceDetails
				.setSharedSecret(new SharedConsumerSecret(oauthProviderDetails
						.getSharedSecret()));

		resourceDetailsStore.put(
				individualLendingResourceDetails.getId(),
				individualLendingResourceDetails);
	}

	@Override
	public ProtectedResourceDetails loadProtectedResourceDetailsById(
			final String id) throws IllegalArgumentException {
		return resourceDetailsStore.get(id);
	}

	@Override
	public void refresh() {
		resourceDetailsStore.clear();
		addHardcodedMifosNgResourceIdToStore();
	}

	public ApplicationConfigurationService getApplicationConfigurationService() {
		return applicationConfigurationService;
	}
}