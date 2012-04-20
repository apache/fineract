package org.mifosng.oauth;

import java.util.HashMap;
import java.util.Map;

import org.mifosng.platform.oauthconsumer.domain.OauthConsumerDetail;
import org.mifosng.platform.oauthconsumer.domain.OauthConsumerDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth.common.OAuthException;
import org.springframework.security.oauth.provider.ConsumerDetails;
import org.springframework.security.oauth.provider.ConsumerDetailsService;
import org.springframework.security.oauth.provider.InvalidOAuthParametersException;

public class MifosNgConsumerDetailsService implements ConsumerDetailsService {

	private Map<String, ConsumerDetails> consumerDetailsStore = new HashMap<String, ConsumerDetails>();
	
	@Autowired
	private OauthConsumerDetailRepository oauthConsumerDetailRepository;
	
	/**
	 * consumer service is used for by spring security oauth for all requests so makes sense to store consumer details in memory
	 * rather than query the database based on each request. 
	 */
	@Override
	public ConsumerDetails loadConsumerByConsumerKey(final String consumerKey)
			throws OAuthException {
		
	    ConsumerDetails detailsFromInMemoryStore = consumerDetailsStore.get(consumerKey);
	    OauthConsumerDetail detailsFromDatabase = null;
	    if (detailsFromInMemoryStore == null) {
	    	detailsFromDatabase = this.oauthConsumerDetailRepository.findByConsumerKey(consumerKey);
	    }
	    
	    ConsumerDetails consumerDetails = detailsFromInMemoryStore != null ? detailsFromInMemoryStore : detailsFromDatabase;
	    
		if (consumerDetails == null) {
			throw new InvalidOAuthParametersException("Consumer not found: " + consumerKey);
		} else {
			consumerDetailsStore.put(consumerKey, detailsFromDatabase);
		}
		return consumerDetails;
	}
}