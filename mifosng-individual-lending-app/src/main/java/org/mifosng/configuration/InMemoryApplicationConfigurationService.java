package org.mifosng.configuration;

import org.springframework.stereotype.Service;

/**
 * When application goes down or rebooted, configuration values return to defaults provided here.
 */
@Service
public class InMemoryApplicationConfigurationService implements ApplicationConfigurationService {

	private String platformApiUrl = "http://localhost:8080/mifosng-provider/api/v1/";
			
	public InMemoryApplicationConfigurationService() {
		//
	}

	@Override
	public void update(final String platformApiUrl) {
		this.platformApiUrl = platformApiUrl;
		if (!this.platformApiUrl.endsWith("/")) {
			this.platformApiUrl = this.platformApiUrl.concat("/");
		}
	}

	@Override
	public String retrievePlatformApiUrl() {
		return this.platformApiUrl;
	}
}