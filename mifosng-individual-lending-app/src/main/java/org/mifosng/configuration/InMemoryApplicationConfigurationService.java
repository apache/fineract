package org.mifosng.configuration;

import org.springframework.stereotype.Service;

/**
 * When application goes down or rebooted, configuration values return to defaults provided here.
 */
@Service
public class InMemoryApplicationConfigurationService implements ApplicationConfigurationService {

	// demo url
//	private String platformApiUrl = "https://ec2-46-137-62-163.eu-west-1.compute.amazonaws.com:8443/mifosng-provider/api/v1/";
	// dev url
	private String platformApiUrl = "https://localhost:8443/mifosng-provider/api/v1/";
			
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