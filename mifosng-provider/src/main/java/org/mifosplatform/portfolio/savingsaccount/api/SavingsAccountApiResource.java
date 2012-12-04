package org.mifosplatform.portfolio.savingsaccount.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
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
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.savingsaccount.command.CalculateSavingScheduleCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountCommand;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccount.service.CalculateSavingSchedule;
import org.mifosplatform.portfolio.savingsaccount.service.SavingAccountReadPlatformService;
import org.mifosplatform.portfolio.savingsaccount.service.SavingAccountWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/savingaccounts")
@Component
@Scope("singleton")
public class SavingsAccountApiResource {

    @Autowired
    private SavingAccountWritePlatformService savingAccountWritePlatformService;

    @Autowired
    private SavingAccountReadPlatformService savingAccountReadPlatformService;

    @Autowired
    private PortfolioApiDataConversionService apiDataConversionService;

    @Autowired
    private PortfolioApiJsonSerializerService apiJsonSerializerService;

    private final String entityType = "SAVINGSACCOUNT";

    @Autowired
    private PlatformSecurityContext context;

    @Autowired
    private CalculateSavingSchedule calculateSavingSchedule;

    @Autowired
    private ToApiJsonSerializer<SavingAccountData> toApiJsonSerializer;

    private static final Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("id", "status", "externalId",
            "clientId", "clientName", "productId", "productName", "productType", "currencyData", "savingsDepostiAmountPerPeriod",
            "savingsFrequencyType", "totalDepositAmount", "reccuringInterestRate", "savingInterestRate", "interestType",
            "interestCalculationMethod", "tenure", "tenureType", "projectedCommencementDate", "actualCommencementDate", "maturesOnDate",
            "projectedInterestAccuredOnMaturity", "actualInterestAccured", "projectedMaturityAmount", "actualMaturityAmount",
            "preClosureAllowed", "preClosureInterestRate", "withdrawnonDate", "rejectedonDate", "closedonDate", "isLockinPeriodAllowed",
            "lockinPeriod", "lockinPeriodType"));

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createSavingAccount(@QueryParam("command") final String commandParam, @Context final UriInfo uriInfo,
            final String jsonRequestBody) {

        final SavingAccountCommand command = this.apiDataConversionService.convertJsonToSavingAccountCommand(null, jsonRequestBody);

        if (is(commandParam, "calculateSavingSchedule")) {
            CalculateSavingScheduleCommand calculateSavingScheduleCommand = command.toCalculateSavingScheduleCommand();
            return calculateSavingSchedule(uriInfo, calculateSavingScheduleCommand);
        }

        EntityIdentifier entityIdentifier = this.savingAccountWritePlatformService.createSavingAccount(command);

        return this.toApiJsonSerializer.serialize(entityIdentifier);
    }

    private String calculateSavingSchedule(final UriInfo uriInfo, final CalculateSavingScheduleCommand command) {

        SavingScheduleData savingScheduleData = this.calculateSavingSchedule.calculateSavingSchedule(command);
        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        return this.apiJsonSerializerService.serializeSavingScheduleDataToJson(prettyPrint, responseParameters, savingScheduleData);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response updateSavingAccount(@PathParam("accountId") final Long accountId, final String jsonRequestBody) {

        final SavingAccountCommand command = this.apiDataConversionService.convertJsonToSavingAccountCommand(accountId, jsonRequestBody);

        EntityIdentifier entityIdentifier = this.savingAccountWritePlatformService.updateSavingAccount(command);

        return Response.ok().entity(entityIdentifier).build();
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllDepositAccounts(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        Collection<SavingAccountData> accounts = this.savingAccountReadPlatformService.retrieveAllSavingsAccounts();

        return this.apiJsonSerializerService.serializeSavingAccountsDataToJson(prettyPrint, responseParameters, accounts);
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveDepositAccount(@PathParam("accountId") final Long accountId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());

        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        SavingAccountData account = this.savingAccountReadPlatformService.retrieveSavingsAccount(accountId);

        return this.apiJsonSerializerService.serializeSavingAccountsDataToJson(prettyPrint, responseParameters, account);
    }

    @SuppressWarnings("unused")
    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewDepositAccountDetails(@QueryParam("clientId") final Long clientId,
            @QueryParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        SavingAccountData account = null;
        // this.savingAccountReadPlatformService.retrieveNewSavingsAccountDetails(clientId,
        // productId);

        return this.apiJsonSerializerService.serializeSavingAccountsDataToJson(prettyPrint, responseParameters, account);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }
}