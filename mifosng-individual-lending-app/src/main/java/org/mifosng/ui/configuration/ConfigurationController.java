package org.mifosng.ui.configuration;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.mifosng.configuration.OAuthProviderDetails;
import org.mifosng.oauth.RefreshableProtectedResourceDetailsService;
import org.mifosng.ui.CommonRestOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth.consumer.ProtectedResourceDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@SessionAttributes({"oauthSettingsFormBean"})
@Controller
public class ConfigurationController {

	private final ApplicationConfigurationService applicationConfigurationService;
	private final RefreshableProtectedResourceDetailsService refreshableProtectedResourceDetailsService;
	private final CommonRestOperations commonRestOperations;

	@Autowired
	public ConfigurationController(
			final CommonRestOperations commonRestOperations, final ApplicationConfigurationService applicationConfigurationService,
			 final RefreshableProtectedResourceDetailsService refreshableProtectedResourceDetailsService) {
		this.commonRestOperations = commonRestOperations;
		this.applicationConfigurationService = applicationConfigurationService;
		this.refreshableProtectedResourceDetailsService = refreshableProtectedResourceDetailsService;
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public String accessDeniedException() {
		return "unAuthorizedAction";
	}

	@ModelAttribute("oauthSettingsFormBean")
	@RequestMapping(value = "oauth/configuration/edit", method = RequestMethod.GET)
	public ModelAndView showOAuthConfiguration() {

		OAuthProviderDetails details = this.applicationConfigurationService.retrieveOAuthProviderDetails();
		
		OauthSettingsFormBean oauthSettingsFormBean = new OauthSettingsFormBean();
		oauthSettingsFormBean.setConsumerkey(details.getConsumerkey());
		oauthSettingsFormBean.setSharedSecret(details.getSharedSecret());
		oauthSettingsFormBean.setOauthProviderUrl(details.getProviderBaseUrl());

		return new ModelAndView("configuration/oauthconfig",
				"oauthSettingsFormBean", oauthSettingsFormBean);
	}
	
	@RequestMapping(value = "oauth/configuration/edit", method = RequestMethod.POST)
	public ModelAndView updateOAuthConfiguration(@ModelAttribute final OauthSettingsFormBean oauthSettingsFormBean) {

		ModelAndView mav = new ModelAndView("redirect:/");

		OAuthProviderDetails newDetails = new OAuthProviderDetails(oauthSettingsFormBean.getOauthProviderUrl(), oauthSettingsFormBean.getConsumerkey(), oauthSettingsFormBean.getSharedSecret());
		
		this.applicationConfigurationService.update(newDetails);
		
		this.refreshableProtectedResourceDetailsService.refresh();
		
		this.commonRestOperations.updateProtectedResource(((ProtectedResourceDetailsService)refreshableProtectedResourceDetailsService).loadProtectedResourceDetailsById("protectedMifosNgServices"));

		return mav;
    }
}