/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountTemplateReadPlatformService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Path("/v1/savingsaccounts")
@Component
@Tag(name = "Savings Account", description = "Savings accounts are instances of a particular savings product created for an individual or group.")
@RequiredArgsConstructor
public class SavingsAccountsApiResource {

    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final SavingsAccountTemplateReadPlatformService savingsAccountTemplateReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingsAccountData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final SqlValidator sqlValidator;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Savings Account Template", operationId = "retrieveSavingsAccountTemplate")
    public String template(@QueryParam("clientId") final Long clientId, @QueryParam("groupId") final Long groupId,
            @QueryParam("productId") final Long productId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);
        final SavingsAccountData savingsAccount = savingsAccountTemplateReadPlatformService.retrieveTemplate(clientId, groupId, productId,
                staffInSelectedOfficeOnly);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, savingsAccount, SavingsApiSetConstants.SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List savings applications/accounts", operationId = "retrieveAllSavings")
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("externalId") final String externalId,
            @QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @QueryParam("orderBy") final String orderBy, @QueryParam("sortOrder") final String sortOrder) {
        context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);
        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        sqlValidator.validate(externalId);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).externalId(externalId).offset(offset)
                .orderBy(orderBy).sortOrder(sortOrder).build();
        final Page<SavingsAccountData> products = savingsAccountReadPlatformService.retrieveAll(searchParameters);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, products, SavingsApiSetConstants.SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Submit new savings application", operationId = "submitSavingsApplication")
    public String submitApplication(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingsAccount().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Savings Account", operationId = "retrieveSavingsAccount")
    public SavingsAccountData retrieveOne(@PathParam("accountId") final Long accountId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("chargeStatus") final String chargeStatus,
            @QueryParam("associations") final String associations, @Context final UriInfo uriInfo) {
        return retrieveSavingAccount(accountId, null, staffInSelectedOfficeOnly, chargeStatus, uriInfo);
    }

    @GET
    @Path("/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Savings Account by External ID", operationId = "retrieveSavingsAccountByExternalId")
    public SavingsAccountData retrieveOne(@PathParam("externalId") final String externalId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("chargeStatus") final String chargeStatus,
            @QueryParam("associations") final String associations, @Context final UriInfo uriInfo) {
        return retrieveSavingAccount(null, externalId, staffInSelectedOfficeOnly, chargeStatus, uriInfo);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Modify a savings application", operationId = "updateSavingsAccount")
    public String update(@PathParam("accountId") final Long accountId, @Parameter(hidden = true) final String apiRequestBodyAsJson,
            @QueryParam("command") final String commandParam) {
        return updateSavingAccount(accountId, null, apiRequestBodyAsJson, commandParam);
    }

    @PUT
    @Path("/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Savings Account by External ID", operationId = "updateSavingsAccountByExternalId")
    public String update(@PathParam("externalId") final String externalId, @Parameter(hidden = true) final String apiRequestBodyAsJson,
            @QueryParam("command") final String commandParam) {
        return updateSavingAccount(null, externalId, apiRequestBodyAsJson, commandParam);
    }

    @POST
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Handle savings commands", operationId = "handleSavingsCommands")
    public String handleCommands(@PathParam("accountId") final Long accountId, @QueryParam("command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return handleCommands(accountId, null, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Handle Savings Account Commands by External ID", operationId = "handleSavingsCommandsByExternalId")
    public String handleCommands(@PathParam("externalId") final String externalId, @QueryParam("command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return handleCommands(null, externalId, commandParam, apiRequestBodyAsJson);
    }

    @DELETE
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a savings application", operationId = "deleteSavingsAccount")
    public String delete(@PathParam("accountId") final Long accountId) {
        return deleteSavingAccount(accountId, null);
    }

    @DELETE
    @Path("/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Savings Account by External ID", operationId = "deleteSavingsAccountByExternalId")
    public String delete(@PathParam("externalId") final String externalId) {
        return deleteSavingAccount(null, externalId);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getSavingsTemplate(@QueryParam("officeId") final Long officeId, @QueryParam("staffId") final Long staffId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.SAVINGS_ACCOUNT.toString(), officeId, staffId, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload savings template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postSavingsTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = bulkImportWorkbookService.importWorkbook(GlobalEntityType.SAVINGS_ACCOUNT.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return toApiJsonSerializer.serialize(importDocumentId);
    }

    @GET
    @Path("transactions/downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getSavingsTransactionTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.SAVINGS_TRANSACTIONS.toString(), officeId, null, dateFormat);
    }

    @POST
    @Path("transactions/uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload savings transaction template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postSavingsTransactionTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = bulkImportWorkbookService.importWorkbook(GlobalEntityType.SAVINGS_TRANSACTIONS.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return toApiJsonSerializer.serialize(importDocumentId);
    }

    private SavingsAccountData retrieveSavingAccount(Long accountId, String externalId, boolean staffInSelectedOfficeOnly,
            String chargeStatus, UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);
        if (!(is(chargeStatus, "all") || is(chargeStatus, "active") || is(chargeStatus, "inactive"))) {
            throw new UnrecognizedQueryParamException("status", chargeStatus, new Object[] { "all", "active", "inactive" });
        }
        ExternalId accountExternalId = ExternalIdFactory.produce(externalId);
        accountId = getResolvedAccountId(accountId, accountExternalId);
        final SavingsAccountData savingsAccount = savingsAccountReadPlatformService.retrieveOne(accountId);
        return populateTemplateAndAssociations(accountId, savingsAccount, staffInSelectedOfficeOnly, chargeStatus, uriInfo);
    }

    private String updateSavingAccount(Long accountId, String externalId, String apiRequestBodyAsJson, String commandParam) {
        ExternalId accountExternalId = ExternalIdFactory.produce(externalId);
        accountId = getResolvedAccountId(accountId, accountExternalId);
        if (is(commandParam, "updateWithHoldTax")) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson).updateWithHoldTax(accountId)
                    .build();
            final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return toApiJsonSerializer.serialize(result);
        }
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSavingsAccount(accountId).withJson(apiRequestBodyAsJson)
                .build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    private String handleCommands(Long accountId, String externalId, String commandParam, String apiRequestBodyAsJson) {
        ExternalId accountExternalId = ExternalIdFactory.produce(externalId);
        accountId = getResolvedAccountId(accountId, accountExternalId);
        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);
        CommandProcessingResult result = null;
        if (is(commandParam, "reject")) {
            result = commandsSourceWritePlatformService.logCommandSource(builder.rejectSavingsAccountApplication(accountId).build());
        } else if (is(commandParam, "approve")) {
            result = commandsSourceWritePlatformService.logCommandSource(builder.approveSavingsAccountApplication(accountId).build());
        } else if (is(commandParam, "undoapproval")) {
            result = commandsSourceWritePlatformService.logCommandSource(builder.undoSavingsAccountApplication(accountId).build());
        } else if (is(commandParam, "activate")) {
            result = commandsSourceWritePlatformService.logCommandSource(builder.savingsAccountActivation(accountId).build());
        } else if (is(commandParam, "close")) {
            result = commandsSourceWritePlatformService.logCommandSource(builder.closeSavingsAccountApplication(accountId).build());
        }
        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam,
                    new Object[] { "reject", "approve", "undoapproval", "activate", "close" });
        }
        return toApiJsonSerializer.serialize(result);
    }

    private String deleteSavingAccount(Long accountId, String externalId) {
        ExternalId accountExternalId = ExternalIdFactory.produce(externalId);
        accountId = getResolvedAccountId(accountId, accountExternalId);
        final CommandProcessingResult result = commandsSourceWritePlatformService
                .logCommandSource(new CommandWrapperBuilder().deleteSavingsAccount(accountId).build());
        return toApiJsonSerializer.serialize(result);
    }

    private Long getResolvedAccountId(Long accountId, ExternalId accountExternalId) {
        Long resolvedAccountId = accountId;
        if (resolvedAccountId == null) {
            accountExternalId.throwExceptionIfEmpty();
            resolvedAccountId = savingsAccountReadPlatformService.retrieveAccountIdByExternalId(accountExternalId);
            if (resolvedAccountId == null) {
                throw new SavingsAccountNotFoundException(resolvedAccountId);
            }
        }
        return resolvedAccountId;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    private SavingsAccountData populateTemplateAndAssociations(final Long accountId, final SavingsAccountData savingsAccount,
            final boolean staffInSelectedOfficeOnly, final String chargeStatus, final UriInfo uriInfo) {
        Collection<SavingsAccountTransactionData> transactions = null;
        Collection<SavingsAccountChargeData> charges = null;
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList("transactions", "charges"));
            }
            if (associationParameters.contains("transactions")) {
                transactions = savingsAccountReadPlatformService.retrieveAllTransactions(accountId, DepositAccountType.SAVINGS_DEPOSIT);
            }
            if (associationParameters.contains("charges")) {
                charges = savingsAccountChargeReadPlatformService.retrieveSavingsAccountCharges(accountId, chargeStatus);
            }
        }
        SavingsAccountData templateData = null;
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            templateData = savingsAccountTemplateReadPlatformService.retrieveTemplate(savingsAccount.getClientId(),
                    savingsAccount.getGroupId(), savingsAccount.getSavingsProductId(), staffInSelectedOfficeOnly);
        }
        return SavingsAccountData.withTemplateOptions(savingsAccount, templateData, transactions, charges);
    }
}
