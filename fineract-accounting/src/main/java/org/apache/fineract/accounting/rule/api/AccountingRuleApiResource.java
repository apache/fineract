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
package org.apache.fineract.accounting.rule.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.rule.data.AccountingRuleData;
import org.apache.fineract.accounting.rule.data.AccountingTagRuleData;
import org.apache.fineract.accounting.rule.service.AccountingRuleReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Component;

@Path("/v1/accountingrules")
@Component
@Tag(name = "Accounting Rules", description = "It is typical scenario in MFI's that non accountants pass journal entries on a regular basis. For Ex: A branch office might deposit their entire cash at hand to their Bank account at the end of a working day. The branch office users might not understand enough of accounting to figure out which account needs to get credited and which account needs to be debited to represent this transaction.\n"
        + "\n"
        + "Enter accounting rules, an abstraction on top of manual Journal entires for enabling simpler data entry. An accounting rule can define any of the following abstractions\n"
        + "\n" + "A Simple journal entry where both the credit and debit account have been preselected\n"
        + "A Simple journal entry where either credit or debit accounts have been limited to a pre-selected list of accounts (Ex: Debit account should be one of \"Bank of America\" of \"JP Morgan\" and credit account should be \"Cash\")\n"
        + "A Compound journal entry where multiple debits and / or multiple credits may be made amongst a set of preselected list of accounts (Ex: Credit account should be either \"Bank Of America\" or \"Cash\" and debit account can be \"Employee Salary\" and/or \"Miscellenous Expenses\")\n"
        + "An accounting rule can also be optionally associated with a branch, so that only a particular Branch's users have access to the rule")
@RequiredArgsConstructor
public class AccountingRuleApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("id", "officeId", "officeName", "accountToDebitId", "accountToCreditId", "name", "description", "systemDefined",
                    "allowedCreditTagOptions", "allowedDebitTagOptions", "debitTags", "creditTags", "creditAccounts", "debitAccounts",
                    "allowMultipleCreditEntries", "allowMultipleDebitEntries", "tag"));

    private static final String RESOURCE_NAME_FOR_PERMISSION = "ACCOUNTINGRULE";

    private final AccountingRuleReadPlatformService accountingRuleReadPlatformService;
    private final GLAccountReadPlatformService accountReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final DefaultToApiJsonSerializer<AccountingRuleData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Accounting Rule Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Request:\n" + "\n" + "accountingrules/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountingRuleApiResourceSwagger.GetAccountRulesTemplateResponse.class))) })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        AccountingRuleData accountingRuleData = null;
        accountingRuleData = handleTemplate(accountingRuleData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, accountingRuleData, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Accounting Rules", description = "Returns the list of defined accounting rules.\n" + "\n"
            + "Example Requests:\n" + "\n" + "accountingrules")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountingRuleApiResourceSwagger.GetAccountRulesResponse.class)))) })
    public String retrieveAllAccountingRules(@Context final UriInfo uriInfo) {

        final AppUser currentUser = this.context.authenticatedUser();
        currentUser.validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        boolean isAssociationParametersExists = false;
        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("all")) {
                isAssociationParametersExists = true; // If true, retrieve
                                                      // additional fields for
                                                      // journal entry form.
            }
        }
        final List<AccountingRuleData> accountingRuleDatas = this.accountingRuleReadPlatformService
                .retrieveAllAccountingRules(hierarchySearchString, isAssociationParametersExists);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, accountingRuleDatas, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountingRuleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Accounting rule", description = "Returns the details of a defined Accounting rule.\n" + "\n"
            + "Example Requests:\n" + "\n" + "accountingrules/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountingRuleData.class))) })
    public String retreiveAccountingRule(
            @PathParam("accountingRuleId") @Parameter(description = "accountingRuleId") final Long accountingRuleId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        AccountingRuleData accountingRuleData = this.accountingRuleReadPlatformService.retrieveAccountingRuleById(accountingRuleId);
        if (settings.isTemplate()) {
            accountingRuleData = handleTemplate(accountingRuleData);
        }

        return this.apiJsonSerializerService.serialize(settings, accountingRuleData, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create/Define a Accounting rule", description = "Define a new Accounting rule.\n" + "\n" + "Mandatory Fields\n"
            + "name, officeId,\n" + "accountToDebit OR debitTags,\n" + "accountToCredit OR creditTags.\n" + "\n" + "Optional Fields\n"
            + "description")
    @RequestBody(content = @Content(schema = @Schema(implementation = AccountingRuleApiResourceSwagger.PostAccountingRulesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountingRuleApiResourceSwagger.PostAccountingRulesResponse.class))) })
    public String createAccountingRule(@Parameter(hidden = true) final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createAccountingRule().withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{accountingRuleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Accounting Rule", description = "Updates the details of a Accounting rule.")
    @RequestBody(content = @Content(schema = @Schema(implementation = AccountingRuleApiResourceSwagger.PutAccountingRulesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountingRuleApiResourceSwagger.PutAccountingRulesResponse.class))) })
    public String updateAccountingRule(
            @PathParam("accountingRuleId") @Parameter(description = "accountingRuleId") final Long accountingRuleId,
            @Parameter(hidden = true) final String jsonRequestBody) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateAccountingRule(accountingRuleId).withJson(jsonRequestBody)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{accountingRuleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Accounting Rule", description = "Deletes a Accounting rule.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountingRuleApiResourceSwagger.DeleteAccountingRulesResponse.class))) })
    public String deleteAccountingRule(
            @PathParam("accountingRuleId") @Parameter(description = "accountingRuleId") final Long accountingRuleId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteAccountingRule(accountingRuleId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    private AccountingRuleData handleTemplate(AccountingRuleData accountingRuleData) {
        final List<GLAccountData> allowedAccounts = this.accountReadPlatformService.retrieveAllEnabledDetailGLAccounts();
        final List<OfficeData> allowedOffices = (List<OfficeData>) this.officeReadPlatformService.retrieveAllOfficesForDropdown();
        final Collection<CodeValueData> allowedTagOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(AccountingConstants.ASSESTS_TAG_OPTION_CODE_NAME);

        allowedTagOptions
                .addAll(this.codeValueReadPlatformService.retrieveCodeValuesByCode(AccountingConstants.LIABILITIES_TAG_OPTION_CODE_NAME));
        allowedTagOptions
                .addAll(this.codeValueReadPlatformService.retrieveCodeValuesByCode(AccountingConstants.EQUITY_TAG_OPTION_CODE_NAME));
        allowedTagOptions
                .addAll(this.codeValueReadPlatformService.retrieveCodeValuesByCode(AccountingConstants.INCOME_TAG_OPTION_CODE_NAME));
        allowedTagOptions
                .addAll(this.codeValueReadPlatformService.retrieveCodeValuesByCode(AccountingConstants.EXPENSES_TAG_OPTION_CODE_NAME));

        if (accountingRuleData == null) {

            final Collection<CodeValueData> allowedCreditTagOptions = allowedTagOptions;
            final Collection<CodeValueData> allowedDebitTagOptions = allowedTagOptions;

            accountingRuleData = new AccountingRuleData().setAllowedOffices(allowedOffices).setAllowedAccounts(allowedAccounts)
                    .setAllowedCreditTagOptions(allowedCreditTagOptions).setAllowedDebitTagOptions(allowedDebitTagOptions);

        } else {

            final Collection<CodeValueData> allowedCreditTagOptions;
            final Collection<CodeValueData> allowedDebitTagOptions;

            if (accountingRuleData.getCreditTags() != null) {
                allowedCreditTagOptions = retrieveSelectedTags(allowedTagOptions, accountingRuleData.getCreditTags());
            } else {
                allowedCreditTagOptions = allowedTagOptions;
            }

            if (accountingRuleData.getDebitTags() != null) {
                allowedDebitTagOptions = retrieveSelectedTags(allowedTagOptions, accountingRuleData.getDebitTags());
            } else {
                allowedDebitTagOptions = allowedTagOptions;
            }

            accountingRuleData = new AccountingRuleData().setId(accountingRuleData.getId()).setOfficeId(accountingRuleData.getOfficeId())
                    .setOfficeName(accountingRuleData.getOfficeName()).setName(accountingRuleData.getName())
                    .setDescription(accountingRuleData.getDescription()).setSystemDefined(accountingRuleData.isSystemDefined())
                    .setAllowMultipleCreditEntries(accountingRuleData.isAllowMultipleCreditEntries())
                    .setAllowMultipleDebitEntries(accountingRuleData.isAllowMultipleDebitEntries()).setAllowedAccounts(allowedAccounts)
                    .setAllowedOffices(allowedOffices).setAllowedCreditTagOptions(allowedCreditTagOptions)
                    .setAllowedDebitTagOptions(allowedDebitTagOptions);

        }
        return accountingRuleData;
    }

    private Collection<CodeValueData> retrieveSelectedTags(final Collection<CodeValueData> allowedTagOptions,
            final List<AccountingTagRuleData> existedTags) {
        final Collection<CodeValueData> tempOptions = new ArrayList<>(allowedTagOptions);
        final Map<Long, CodeValueData> selectedTags = new HashMap<>();
        for (final AccountingTagRuleData accountingTagRuleData : existedTags) {
            for (final CodeValueData codeValueData : tempOptions) {
                if (codeValueData.getId().equals(accountingTagRuleData.getTag().getId())) {
                    selectedTags.put(codeValueData.getId(), codeValueData);
                }
            }
        }
        tempOptions.removeAll(selectedTags.values());
        return tempOptions;
    }
}
