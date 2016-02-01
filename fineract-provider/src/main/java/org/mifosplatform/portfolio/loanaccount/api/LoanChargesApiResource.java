/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;
import org.mifosplatform.portfolio.charge.service.ChargeReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargeData;
import org.mifosplatform.portfolio.loanaccount.data.LoanInstallmentChargeData;
import org.mifosplatform.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/charges")
@Component
@Scope("singleton")
public class LoanChargesApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("id", "chargeId", "name", "penalty", "chargeTimeType", "dueAsOfDate", "chargeCalculationType", "percentage",
                    "amountPercentageAppliedTo", "currency", "amountWaived", "amountWrittenOff", "amountOutstanding", "amountOrPercentage",
                    "amount", "amountPaid", "chargeOptions", "installmentChargeData"));

    private final String resourceNameForPermissions = "LOAN";

    private final PlatformSecurityContext context;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanChargeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public LoanChargesApiResource(final PlatformSecurityContext context, final ChargeReadPlatformService chargeReadPlatformService,
            final LoanChargeReadPlatformService loanChargeReadPlatformService,
            final DefaultToApiJsonSerializer<LoanChargeData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllLoanCharges(@PathParam("loanId") final Long loanId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<LoanChargeData> loanCharges = this.loanChargeReadPlatformService.retrieveLoanCharges(loanId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanCharges, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@PathParam("loanId") final Long loanId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanAccountApplicableCharges(loanId,
                new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT });
        final LoanChargeData loanChargeTemplate = LoanChargeData.template(chargeOptions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanChargeTemplate, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final LoanChargeData loanCharge = this.loanChargeReadPlatformService.retrieveLoanChargeDetails(loanChargeId, loanId);

        final Collection<LoanInstallmentChargeData> installmentChargeData = this.loanChargeReadPlatformService
                .retrieveInstallmentLoanCharges(loanChargeId, true);

        final LoanChargeData loanChargeData = new LoanChargeData(loanCharge, installmentChargeData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanChargeData, this.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String executeLoanCharge(@PathParam("loanId") final Long loanId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

        CommandProcessingResult result = null;
        if (is(commandParam, "pay")) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().payLoanCharge(loanId, null).withJson(apiRequestBodyAsJson)
                    .build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanCharge(loanId).withJson(apiRequestBodyAsJson)
                    .build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId,
            final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanCharge(loanId, loanChargeId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String executeLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId,
            @QueryParam("command") final String commandParam, final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        CommandProcessingResult result = null;
        if (is(commandParam, "waive")) {
            final CommandWrapper commandRequest = builder.waiveLoanCharge(loanId, loanChargeId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "pay")) {
            final CommandWrapper commandRequest = builder.payLoanCharge(loanId, loanChargeId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        if (result == null) { throw new UnrecognizedQueryParamException("command", commandParam); }
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteLoanCharge(@PathParam("loanId") final Long loanId, @PathParam("chargeId") final Long loanChargeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanCharge(loanId, loanChargeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}