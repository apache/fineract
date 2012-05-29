package org.mifosng.platform.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.data.ConfigurationData;
import org.mifosng.platform.api.commands.OrganisationCurrencyCommand;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.configuration.service.ConfigurationReadPlatformService;
import org.mifosng.platform.configuration.service.ConfigurationWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/configurations")
@Component
@Scope("singleton")
public class ConfigurationApiResource {

	private String allowedFieldList = "";
	private String filterName = "myFilter";

	@Autowired
	private ConfigurationReadPlatformService configurationReadPlatformService;

	@Autowired
	private ConfigurationWritePlatformService configurationWritePlatformService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;

	@GET
	@Path("currency")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveCurrencyDataForConfiguration(@Context UriInfo uriInfo) {

		ConfigurationData configurationData = this.configurationReadPlatformService
				.retrieveCurrencyConfiguration();

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(configurationData,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@PUT
	@Path("currency")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateAllowedCurrenciesForOrganisation(
			OrganisationCurrencyCommand command) {

		this.configurationWritePlatformService
				.updateOrganisationCurrencies(command);

		return Response.ok().entity(command).build();
	}
}