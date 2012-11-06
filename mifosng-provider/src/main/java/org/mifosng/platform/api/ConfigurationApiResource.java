package org.mifosng.platform.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.OrganisationCurrencyCommand;
import org.mifosng.platform.api.data.ConfigurationData;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.configuration.service.ConfigurationReadPlatformService;
import org.mifosng.platform.configuration.service.ConfigurationWritePlatformService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/configurations")
@Component
@Scope("singleton")
public class ConfigurationApiResource {

	@Autowired
	private ConfigurationReadPlatformService configurationReadPlatformService;

	@Autowired
	private ConfigurationWritePlatformService configurationWritePlatformService;

	@Autowired
	private PortfolioApiDataConversionService apiDataConversionService;
	
	@Autowired
	private PortfolioApiJsonSerializerService apiJsonSerializerService;
	
	@GET
	@Path("currency")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveCurrencyDataForConfiguration(@Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("selectedCurrencyOptions", "currencyOptions"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		ConfigurationData configurationData = this.configurationReadPlatformService.retrieveCurrencyConfiguration();
		
		return this.apiJsonSerializerService.serializeConfigurationDataToJson(prettyPrint, responseParameters, configurationData);
	}

	@PUT
	@Path("currency")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateAllowedCurrenciesForOrganisation(final String jsonRequestBody) {

		OrganisationCurrencyCommand command = this.apiDataConversionService.convertJsonToOrganisationCurrencyCommand(jsonRequestBody);
		
		this.configurationWritePlatformService.updateOrganisationCurrencies(command);

		return Response.ok().entity(command).build();
	}
}