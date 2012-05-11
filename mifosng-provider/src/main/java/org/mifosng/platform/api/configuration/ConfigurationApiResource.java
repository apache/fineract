package org.mifosng.platform.api.configuration;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.ConfigurationData;
import org.mifosng.data.command.OrganisationCurrencyCommand;
import org.mifosng.platform.configuration.service.ConfigurationReadPlatformService;
import org.mifosng.platform.configuration.service.ConfigurationWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/v1/configurations")
@Component
@Scope("singleton")
public class ConfigurationApiResource {

    @Autowired
	private ConfigurationReadPlatformService configurationReadPlatformService;
    
	@Autowired
	private ConfigurationWritePlatformService configurationWritePlatformService;

    @GET
	@Path("currency")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
	public Response retrieveCurrencyDataForConfiguration() {

    	ConfigurationData configurationData = this.configurationReadPlatformService.retrieveCurrencyConfiguration();
    	
		return Response.ok().entity(configurationData).build();
	}

	@PUT
	@Path("currency")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response updateAllowedCurrenciesForOrganisation(OrganisationCurrencyCommand command) {
		
		this.configurationWritePlatformService.updateOrganisationCurrencies(command);

		return Response.ok().entity(command).build();
	}
}