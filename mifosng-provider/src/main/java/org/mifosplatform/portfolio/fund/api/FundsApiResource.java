package org.mifosplatform.portfolio.fund.api;

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

import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.fund.service.FundReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/funds")
@Component
@Scope("singleton")
public class FundsApiResource {
    
    private final String resourceNameForPermissions = "FUND";
    
    private final PlatformSecurityContext context;
    private final FundReadPlatformService readPlatformService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    
    @Autowired
    public FundsApiResource(final PlatformSecurityContext context, 
            final FundReadPlatformService readPlatformService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveFunds(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<FundData> funds = this.readPlatformService.retrieveAllFunds();

        return this.apiJsonSerializerService.serializeFundDataToJson(prettyPrint, responseParameters, funds);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createFund(final String apiRequestBodyAsJson) {
        
        // FIXME - permissions - is funds a portfolio concept?
        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "CREATE_FUND");
        context.authenticatedUser().validateHasPermissionTo("CREATE_FUND", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "funds", null,
                apiRequestBodyAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @GET
    @Path("{fundId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveFund(@PathParam("fundId") final Long fundId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final FundData fund = this.readPlatformService.retrieveFund(fundId);

        return this.apiJsonSerializerService.serializeFundDataToJson(prettyPrint, responseParameters, fund);
    }

    @PUT
    @Path("{fundId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateFund(@PathParam("fundId") final Long fundId, final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "UPDATE_FUND");
        context.authenticatedUser().validateHasPermissionTo("UPDATE_FUND", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "funds", fundId,
                apiRequestBodyAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }
}