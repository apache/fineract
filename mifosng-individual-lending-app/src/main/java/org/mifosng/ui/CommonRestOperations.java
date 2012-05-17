package org.mifosng.ui;

import org.springframework.security.oauth.consumer.ProtectedResourceDetails;

public interface CommonRestOperations {

	void logout(String accessToken);

	void updateProtectedResource(ProtectedResourceDetails loadProtectedResourceDetailsById);
}