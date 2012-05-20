package org.mifosng.configuration;

public interface ApplicationConfigurationService {

	void update(String platformApiUrl);

	String retrievePlatformApiUrl();
}
