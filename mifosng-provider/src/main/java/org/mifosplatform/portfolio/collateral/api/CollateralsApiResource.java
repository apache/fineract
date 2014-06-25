/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collateral.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.collateral.data.CollateralData;
import org.mifosplatform.portfolio.collateral.service.CollateralReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/collaterals")
@Component
@Scope("singleton")
public class CollateralsApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "type", "value", "description",
            "allowedCollateralTypes", "currency"));

    private final String resourceNameForPermission = "COLLATERAL";

    private final CollateralReadPlatformService collateralReadPlatformService;
    private final DefaultToApiJsonSerializer<CollateralData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public CollateralsApiResource(final PlatformSecurityContext context, final CollateralReadPlatformService collateralReadPlatformService,
            final DefaultToApiJsonSerializer<CollateralData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiJsonSerializerService = toApiJsonSerializer;
        this.collateralReadPlatformService = collateralReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String newCollateralTemplate(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
        final CollateralData collateralData = CollateralData.template(codeValues);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, collateralData, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCollateralDetails(@Context final UriInfo uriInfo, @PathParam("loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final List<CollateralData> CollateralDatas = this.collateralReadPlatformService.retrieveCollateralsForValidLoan(loanId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.apiJsonSerializerService.serialize(settings, CollateralDatas, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCollateralDetails(@Context final UriInfo uriInfo, @PathParam("loanId") final Long loanId,
            @PathParam("collateralId") final Long CollateralId) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        CollateralData CollateralData = this.collateralReadPlatformService.retrieveCollateral(loanId, CollateralId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService
                    .retrieveCodeValuesByCode(CollateralApiConstants.COLLATERAL_CODE_NAME);
            CollateralData = CollateralData.template(CollateralData, codeValues);
        }

        return this.apiJsonSerializerService.serialize(settings, CollateralData, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createCollateral(@PathParam("loanId") final Long loanId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCollateral(loanId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateCollateral(@PathParam("loanId") final Long loanId, @PathParam("collateralId") final Long collateralId,
            final String jsonRequestBody) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCollateral(loanId, collateralId).withJson(jsonRequestBody)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteCollateral(@PathParam("loanId") final Long loanId, @PathParam("collateralId") final Long collateralId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCollateral(loanId, collateralId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }
}