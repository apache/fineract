package org.mifosng.ui.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.mifosng.configuration.OAuthProviderDetails;
import org.mifosng.data.ConfigurationData;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.command.UpdateOrganisationCurrencyCommand;
import org.mifosng.oauth.RefreshableProtectedResourceDetailsService;
import org.mifosng.ui.CommonRestOperations;
import org.mifosng.ui.loanproduct.ClientValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth.consumer.ProtectedResourceDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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

	@ExceptionHandler(ClientValidationException.class)
	public @ResponseBody
	Collection<ErrorResponse> validationException(ClientValidationException ex,
			HttpServletResponse response) {

		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType("application/json");

		return ex.getValidationErrors();
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "org/configuration/edit", method = RequestMethod.GET)
	public @ResponseBody ConfigurationData viewConfigurationDetails() {
		
		List<CurrencyData> selectedCurrencyOptions = new ArrayList<CurrencyData>(this.commonRestOperations.retrieveAllowedCurrencies());
		List<CurrencyData> currencyOptions = new ArrayList<CurrencyData>(this.commonRestOperations.retrieveAllPlatformCurrencies());

		// remove selected currency options
		currencyOptions.removeAll(selectedCurrencyOptions);
		
		ConfigurationData configurationData = new ConfigurationData();
		configurationData.setCurrencyOptions(currencyOptions);
		configurationData.setSelectedCurrencyOptions(selectedCurrencyOptions);
		
		return configurationData;
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "org/configuration/edit", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier updateOrganisationCurrencies(@RequestParam(value="selectedItems", required=false) String[] selectedCurrencies)  {
		
		List<String> codes = new ArrayList<String>();
		if (selectedCurrencies != null) {
			codes = Arrays.asList(selectedCurrencies);
		}
		
		UpdateOrganisationCurrencyCommand command = new UpdateOrganisationCurrencyCommand(codes);

		this.commonRestOperations.updateOrganisationCurrencies(command);
		
		return new EntityIdentifier(Long.valueOf(1));
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
	public ModelAndView updateOAuthConfiguration(
			@ModelAttribute final OauthSettingsFormBean oauthSettingsFormBean,
			final BindingResult result) {

		ModelAndView mav = new ModelAndView("redirect:/");

		OAuthProviderDetails newDetails = new OAuthProviderDetails(oauthSettingsFormBean.getOauthProviderUrl(), oauthSettingsFormBean.getConsumerkey(), oauthSettingsFormBean.getSharedSecret());
		
		this.applicationConfigurationService.update(newDetails);
		
		this.refreshableProtectedResourceDetailsService.refresh();
		
		this.commonRestOperations.updateProtectedResource(((ProtectedResourceDetailsService)refreshableProtectedResourceDetailsService).loadProtectedResourceDetailsById("protectedMifosNgServices"));

		return mav;
    }
}