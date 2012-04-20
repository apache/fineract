package org.mifosng.platform.oauthconsumer.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OauthConsumerDetailRepository extends
		JpaRepository<OauthConsumerDetail, Long>,
		JpaSpecificationExecutor<OauthConsumerDetail> {

	OauthConsumerDetail findByConsumerKey(String consumerKey);
}
