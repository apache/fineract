package org.mifosng.ui.migration;

import java.net.URI;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth.consumer.client.OAuthRestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MigrationTriggerController {
	
	private final OAuthRestTemplate oauthRestServiceTemplate;
	private final ApplicationConfigurationService applicationConfigurationService;

	@Autowired
	public MigrationTriggerController(final OAuthRestTemplate oauthRestServiceTemplate, ApplicationConfigurationService applicationConfigurationService) {
		this.oauthRestServiceTemplate = oauthRestServiceTemplate;
		this.applicationConfigurationService = applicationConfigurationService;
	}
	
	@RequestMapping(value = "/migration/trigger/clients/creocore", method = RequestMethod.GET)
	public String migrateClientInformation() {
		
		URI restUri = URI
				.create(getBaseServerUrl().concat("api/protected/import/trigger/clients/creocore"));

		this.oauthRestServiceTemplate.getForEntity(restUri, null);
		return "redirect:/home";
	}
	
	@RequestMapping(value = "/migration/trigger/loans/creocore", method = RequestMethod.GET)
	public String migrateLoanInformation() {
		
		URI restUri = URI
				.create(getBaseServerUrl().concat("api/protected/import/trigger/loans/creocore"));

		this.oauthRestServiceTemplate.getForEntity(restUri, null);
		return "redirect:/home";
	}
	
	@RequestMapping(value = "/migration/trigger/repayments/creocore", method = RequestMethod.GET)
	public String migrateLoanRepaymentsInformation() {
		
		URI restUri = URI
				.create(getBaseServerUrl().concat("api/protected/import/trigger/repayments/creocore"));

		this.oauthRestServiceTemplate.getForEntity(restUri, null);
		return "redirect:/home";
	}

	private String getBaseServerUrl() {
		return this.applicationConfigurationService.retrieveOAuthProviderDetails().getProviderBaseUrl();
	}
}