package org.mifosplatform.portfolio.loanaccount.api;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.PortfolioApiDataConversionService;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.service.ChargeReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.data.LoanAccountData;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargeData;
import org.mifosplatform.portfolio.loanaccount.service.LoanWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/charges")
@Component
@Scope("singleton")
public class LoanChargesApiResource {

    private final PlatformSecurityContext context;
    private final LoanWritePlatformService loanWritePlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer;
//    private final ApiRequestParameterHelper apiRequestParameterHelper;
//    private final FromJsonHelper fromJsonHelper;
//    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public LoanChargesApiResource(final PlatformSecurityContext context, final LoanWritePlatformService loanWritePlatformService,
            final ChargeReadPlatformService chargeReadPlatformService,
            final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer,
//            final ApiRequestParameterHelper apiRequestParameterHelper, 
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioApiDataConversionService apiDataConversionService
    // , final FromJsonHelper fromJsonHelper,
    // final PortfolioCommandSourceWritePlatformService
    // commandsSourceWritePlatformService
            ) {
        this.context = context;
        this.loanWritePlatformService = loanWritePlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
//        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.apiDataConversionService = apiDataConversionService;
//        this.fromJsonHelper = fromJsonHelper;
//        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addLoanCharge(@PathParam("loanId") final Long loanId, final String jsonRequestBody) {

        final LoanChargeCommand command = this.apiDataConversionService.convertJsonToLoanChargeCommand(null, loanId, jsonRequestBody);

        final EntityIdentifier identifier = this.loanWritePlatformService.addLoanCharge(command);

        return Response.ok().entity(identifier).build();
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewLoanChargeDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("LOAN");

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final boolean feeChargesOnly = false;
        final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableCharges(feeChargesOnly);
        final LoanChargeData loanChargeTemplate = LoanChargeData.template(chargeOptions);

        return this.apiJsonSerializerService.serializeLoanChargeDataToJson(prettyPrint, responseParameters, loanChargeTemplate);
    }

    @GET
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId,
            @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("LOAN");

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final LoanChargeData loanCharge = this.chargeReadPlatformService.retrieveLoanChargeDetails(loanChargeId, loanId);

        return this.apiJsonSerializerService.serializeLoanChargeDataToJson(prettyPrint, responseParameters, loanCharge);
    }

    @PUT
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId,
            final String jsonRequestBody) {

        final LoanChargeCommand command = this.apiDataConversionService.convertJsonToLoanChargeCommand(loanChargeId, loanId,
                jsonRequestBody);

        final EntityIdentifier identifier = this.loanWritePlatformService.updateLoanCharge(command);

        return this.toApiJsonSerializer.serialize(identifier);
    }

    @POST
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String waiveLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId,
            @QueryParam("command") final String commandParam) {

        final LoanChargeCommand command = LoanChargeCommand.forWaiver(loanChargeId, loanId);

        String json = "";
        if (is(commandParam, "waive")) {
            final EntityIdentifier identifier = this.loanWritePlatformService.waiveLoanCharge(command);
            json = this.toApiJsonSerializer.serialize(identifier);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return json;
    }

    @DELETE
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId) {

        final EntityIdentifier identifier = this.loanWritePlatformService.deleteLoanCharge(loanId, loanChargeId);
        return this.toApiJsonSerializer.serialize(identifier);
    }
}