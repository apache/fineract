package org.mifosplatform.organisation.monetary.api;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.PortfolioApiDataConversionService;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.api.PortfolioCommandSerializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.command.CurrencyCommand;
import org.mifosplatform.organisation.monetary.data.ConfigurationData;
import org.mifosplatform.organisation.monetary.service.OrganisationCurrencyReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/currencies")
@Component
@Scope("singleton")
public class CurrenciesApiResource {
    
    private final String resourceNameForPermissions = "CURRENCY";
    
    private final PlatformSecurityContext context;
    private final OrganisationCurrencyReadPlatformService readPlatformService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioCommandSerializerService commandSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public CurrenciesApiResource(final PlatformSecurityContext context, 
            final OrganisationCurrencyReadPlatformService readPlatformService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioCommandSerializerService commandSerializerService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCurrencies(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final ConfigurationData configurationData = this.readPlatformService.retrieveCurrencyConfiguration();

        return this.apiJsonSerializerService.serializeConfigurationDataToJson(prettyPrint, responseParameters, configurationData);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateCurrencies(final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "UPDATE_CURRENCY");
        context.authenticatedUser().validateHasPermissionTo("UPDATE_CURRENCY", allowedPermissions);

        final CurrencyCommand command = this.apiDataConversionService.convertApiRequestJsonToCurrencyCommand(apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);
        
        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "currencies", null,
                commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }
}