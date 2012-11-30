package org.mifosplatform.portfolio.charge.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.PortfolioCommandSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.service.ChargeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/charges")
@Component
@Scope("singleton")
public class ChargesApiResource {

    private final String resourceNameForPermissions = "CHARGE";
    
    private final PlatformSecurityContext context;
    private final ChargeReadPlatformService readPlatformService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioCommandSerializerService commandSerializerService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    
    @Autowired
    public ChargesApiResource(final PlatformSecurityContext context, 
            final ChargeReadPlatformService readPlatformService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioCommandSerializerService commandSerializerService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }
    
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllCharges(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<ChargeData> charges = this.readPlatformService.retrieveAllCharges();

        return this.apiJsonSerializerService.serializeChargeDataToJson(prettyPrint, responseParameters, charges);
    }

    @GET
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCharge(@PathParam("chargeId") final Long chargeId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());

        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

        ChargeData charge = this.readPlatformService.retrieveCharge(chargeId);
        if (template) {
            ChargeData templateData = this.readPlatformService.retrieveNewChargeDetails();
            charge = new ChargeData(charge, templateData);
        }

        return this.apiJsonSerializerService.serializeChargeDataToJson(prettyPrint, responseParameters, charge);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewChargeDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());

        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final ChargeData chargeData = this.readPlatformService.retrieveNewChargeDetails();

        return this.apiJsonSerializerService.serializeChargeDataToJson(prettyPrint, responseParameters, chargeData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createCharge(final String apiRequestBodyAsJson) {
        
        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "CREATE_CHARGE");
        context.authenticatedUser().validateHasPermissionTo("CREATE_CHARGE", allowedPermissions);

        final ChargeDefinitionCommand command = this.apiDataConversionService.convertApiRequestJsonToChargeDefinitionCommand(null, apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);
        
        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "charges", null, commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @PUT
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateCharge(@PathParam("chargeId") final Long chargeId, final String apiRequestBodyAsJson) {
        
        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "UPDATE_CHARGE");
        context.authenticatedUser().validateHasPermissionTo("UPDATE_CHARGE", allowedPermissions);
        
        final ChargeDefinitionCommand command = this.apiDataConversionService.convertApiRequestJsonToChargeDefinitionCommand(chargeId, apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);
        
        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "charges", chargeId, commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @DELETE
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteCharge(@PathParam("chargeId") final Long chargeId) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "DELETE_CHARGE");
        context.authenticatedUser().validateHasPermissionTo("DELETE_CHARGE", allowedPermissions);
        
        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("DELETE", "charges", chargeId, "{}");

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }
}