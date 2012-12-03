package org.mifosplatform.organisation.office.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import org.mifosplatform.organisation.office.command.OfficeCommand;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/offices")
@Component
@Scope("singleton")
public class OfficesApiResource {

    private final String resourceNameForPermissions = "OFFICE";
    
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService readPlatformService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioCommandSerializerService commandSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public OfficesApiResource(final PlatformSecurityContext context, 
            final OfficeReadPlatformService readPlatformService,
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
    public String retrieveOffices(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<OfficeData> offices = this.readPlatformService.retrieveAllOffices();

        return this.apiJsonSerializerService.serializeOfficeDataToJson(prettyPrint, responseParameters, offices);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOfficeTemplate(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        
        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        OfficeData office = this.readPlatformService.retrieveNewOfficeTemplate();
        
        final List<OfficeLookup> allowedParents = new ArrayList<OfficeLookup>(this.readPlatformService.retrieveAllOfficesForLookup());
        office = OfficeData.appendedTemplate(office, allowedParents);

        return this.apiJsonSerializerService.serializeOfficeDataToJson(prettyPrint, responseParameters, office);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createOffice(final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "CREATE_OFFICE");
        context.authenticatedUser().validateHasPermissionTo("CREATE_OFFICE", allowedPermissions);

        final OfficeCommand command = this.apiDataConversionService.convertApiRequestJsonToOfficeCommand(null, apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);
        
        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "offices", null,
                commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @GET
    @Path("{officeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveOffice(@PathParam("officeId") final Long officeId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

        OfficeData office = this.readPlatformService.retrieveOffice(officeId);
        if (template) {
            List<OfficeLookup> allowedParents = this.readPlatformService.retrieveAllowedParents(officeId);
            office = OfficeData.appendedTemplate(office, allowedParents);
        }

        return this.apiJsonSerializerService.serializeOfficeDataToJson(prettyPrint, responseParameters, office);
    }

    @PUT
    @Path("{officeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateOffice(@PathParam("officeId") final Long officeId, final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "UPDATE_OFFICE");
        context.authenticatedUser().validateHasPermissionTo("UPDATE_OFFICE", allowedPermissions);

        final OfficeCommand command = this.apiDataConversionService.convertApiRequestJsonToOfficeCommand(officeId, apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);
        
        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "offices", officeId,
                commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }
}