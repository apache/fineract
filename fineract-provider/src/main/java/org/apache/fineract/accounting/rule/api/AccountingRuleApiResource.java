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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/accountingrules")
@Component
@Scope("singleton")
public class AccountingRuleApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "officeId", "officeName",
            "accountToDebitId", "accountToCreditId", "name", "description", "systemDefined", "allowedCreditTagOptions",
            "allowedDebitTagOptions", "debitTags", "creditTags", "creditAccounts", "debitAccounts", "allowMultipleCreditEntries",
            "allowMultipleDebitEntries", "tag"));

    private final String resourceNameForPermission = "ACCOUNTINGRULE";

    private final AccountingRuleReadPlatformService accountingRuleReadPlatformService;
    private final GLAccountReadPlatformService accountReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final DefaultToApiJsonSerializer<AccountingRuleData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public AccountingRuleApiResource(final PlatformSecurityContext context,
            final AccountingRuleReadPlatformService accountingRuleReadPlatformService,
            final DefaultToApiJsonSerializer<AccountingRuleData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, final GLAccountReadPlatformService accountReadPlatformService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final CodeValueReadPlatformService codeValueReadPlatformService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiJsonSerializerService = toApiJsonSerializer;
        this.accountingRuleReadPlatformService = accountingRuleReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.accountReadPlatformService = accountReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        AccountingRuleData accountingRuleData = null;
        accountingRuleData = handleTemplate(accountingRuleData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, accountingRuleData, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllAccountingRules(@Context final UriInfo uriInfo) {

        final AppUser currentUser = this.context.authenticatedUser();
        currentUser.validateHasReadPermission(this.resourceNameForPermission);

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
        final List<AccountingRuleData> accountingRuleDatas = this.accountingRuleReadPlatformService.retrieveAllAccountingRules(
                hierarchySearchString, isAssociationParametersExists);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, accountingRuleDatas, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountingRuleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveAccountingRule(@PathParam("accountingRuleId") final Long accountingRuleId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);
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
    public String createAccountingRule(final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createAccountingRule().withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{accountingRuleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateAccountingRule(@PathParam("accountingRuleId") final Long accountingRuleId, final String jsonRequestBody) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateAccountingRule(accountingRuleId).withJson(jsonRequestBody)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{accountingRuleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteAccountingRule(@PathParam("accountingRuleId") final Long accountingRuleId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteAccountingRule(accountingRuleId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    private AccountingRuleData handleTemplate(AccountingRuleData accountingRuleData) {
        final List<GLAccountData> allowedAccounts = this.accountReadPlatformService.retrieveAllEnabledDetailGLAccounts();
        final List<OfficeData> allowedOffices = (List<OfficeData>) this.officeReadPlatformService.retrieveAllOfficesForDropdown();
        final Collection<CodeValueData> allowedTagOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(AccountingConstants.ASSESTS_TAG_OPTION_CODE_NAME);

        allowedTagOptions.addAll(this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(AccountingConstants.LIABILITIES_TAG_OPTION_CODE_NAME));
        allowedTagOptions.addAll(this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(AccountingConstants.EQUITY_TAG_OPTION_CODE_NAME));
        allowedTagOptions.addAll(this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(AccountingConstants.INCOME_TAG_OPTION_CODE_NAME));
        allowedTagOptions.addAll(this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(AccountingConstants.EXPENSES_TAG_OPTION_CODE_NAME));

        if (accountingRuleData == null) {

            final Collection<CodeValueData> allowedCreditTagOptions = allowedTagOptions;
            final Collection<CodeValueData> allowedDebitTagOptions = allowedTagOptions;

            accountingRuleData = new AccountingRuleData(allowedAccounts, allowedOffices, allowedCreditTagOptions, allowedDebitTagOptions);

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

            accountingRuleData = new AccountingRuleData(accountingRuleData, allowedAccounts, allowedOffices, allowedCreditTagOptions,
                    allowedDebitTagOptions);
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