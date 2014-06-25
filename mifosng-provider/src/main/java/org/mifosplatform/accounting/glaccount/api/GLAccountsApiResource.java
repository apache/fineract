/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.api;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.accounting.common.AccountingConstants;
import org.mifosplatform.accounting.common.AccountingDropdownReadPlatformService;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.glaccount.service.GLAccountReadPlatformService;
import org.mifosplatform.accounting.journalentry.data.JournalEntryAssociationParametersData;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/glaccounts")
@Component
@Scope("singleton")
public class GLAccountsApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "name", "parentId", "glCode",
            "disabled", "manualEntriesAllowed", "type", "usage", "description", "assetHeaderAccountOptions",
            "liabilityHeaderAccountOptions", "equityHeaderAccountOptions", "incomeHeaderAccountOptions", "expenseHeaderAccountOptions",
            "nameDecorated", "tagId", "allowedAssetsTagOptions", "allowedLiabilitiesTagOptions", "allowedEquityTagOptions",
            "allowedIncomeTagOptions", "allowedExpensesTagOptions", "creditAccounts", "debitAccounts"));

    private final String resourceNameForPermission = "GLACCOUNT";

    private final GLAccountReadPlatformService glAccountReadPlatformService;
    private final AccountingDropdownReadPlatformService dropdownReadPlatformService;
    private final DefaultToApiJsonSerializer<GLAccountData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public GLAccountsApiResource(final PlatformSecurityContext context, final GLAccountReadPlatformService glAccountReadPlatformService,
            final DefaultToApiJsonSerializer<GLAccountData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final AccountingDropdownReadPlatformService dropdownReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiJsonSerializerService = toApiJsonSerializer;
        this.glAccountReadPlatformService = glAccountReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewAccountDetails(@Context final UriInfo uriInfo, @QueryParam("type") final Integer type) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        GLAccountData glAccountData = this.glAccountReadPlatformService.retrieveNewGLAccountDetails(type);
        glAccountData = handleTemplate(glAccountData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, glAccountData, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllAccounts(@Context final UriInfo uriInfo, @QueryParam("type") final Integer type,
            @QueryParam("searchParam") final String searchParam, @QueryParam("usage") final Integer usage,
            @QueryParam("manualEntriesAllowed") final Boolean manualEntriesAllowed, @QueryParam("disabled") final Boolean disabled,
            @QueryParam("fetchRunningBalance") final boolean runningBalance) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);
        JournalEntryAssociationParametersData associationParametersData = new JournalEntryAssociationParametersData(false, runningBalance);
        final List<GLAccountData> glAccountDatas = this.glAccountReadPlatformService.retrieveAllGLAccounts(type, searchParam, usage,
                manualEntriesAllowed, disabled, associationParametersData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, glAccountDatas, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{glAccountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveAccount(@PathParam("glAccountId") final Long glAccountId, @Context final UriInfo uriInfo,
            @QueryParam("fetchRunningBalance") final boolean runningBalance) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        JournalEntryAssociationParametersData associationParametersData = new JournalEntryAssociationParametersData(false, runningBalance);
        GLAccountData glAccountData = this.glAccountReadPlatformService.retrieveGLAccountById(glAccountId, associationParametersData);
        if (settings.isTemplate()) {
            glAccountData = handleTemplate(glAccountData);
        }

        return this.apiJsonSerializerService.serialize(settings, glAccountData, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createGLAccount(final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createGLAccount().withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{glAccountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateGLAccount(@PathParam("glAccountId") final Long glAccountId, final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateGLAccount(glAccountId).withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{glAccountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteGLAccount(@PathParam("glAccountId") final Long glAccountId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteGLAccount(glAccountId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    private GLAccountData handleTemplate(final GLAccountData glAccountData) {
        final List<EnumOptionData> accountTypeOptions = this.dropdownReadPlatformService.retrieveGLAccountTypeOptions();
        final List<EnumOptionData> usageOptions = this.dropdownReadPlatformService.retrieveGLAccountUsageOptions();
        final List<GLAccountData> assetHeaderAccountOptions = defaultIfEmpty(this.glAccountReadPlatformService
                .retrieveAllEnabledHeaderGLAccounts(GLAccountType.ASSET));
        final List<GLAccountData> liabilityHeaderAccountOptions = defaultIfEmpty(this.glAccountReadPlatformService
                .retrieveAllEnabledHeaderGLAccounts(GLAccountType.LIABILITY));
        final List<GLAccountData> equityHeaderAccountOptions = defaultIfEmpty(this.glAccountReadPlatformService
                .retrieveAllEnabledHeaderGLAccounts(GLAccountType.EQUITY));
        final List<GLAccountData> incomeHeaderAccountOptions = defaultIfEmpty(this.glAccountReadPlatformService
                .retrieveAllEnabledHeaderGLAccounts(GLAccountType.INCOME));
        final List<GLAccountData> expenseHeaderAccountOptions = defaultIfEmpty(this.glAccountReadPlatformService
                .retrieveAllEnabledHeaderGLAccounts(GLAccountType.EXPENSE));
        final Collection<CodeValueData> allowedAssetsTagOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(AccountingConstants.ASSESTS_TAG_OPTION_CODE_NAME);
        final Collection<CodeValueData> allowedLiabilitiesTagOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(AccountingConstants.LIABILITIES_TAG_OPTION_CODE_NAME);
        final Collection<CodeValueData> allowedEquityTagOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(AccountingConstants.EQUITY_TAG_OPTION_CODE_NAME);
        final Collection<CodeValueData> allowedIncomeTagOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(AccountingConstants.INCOME_TAG_OPTION_CODE_NAME);
        final Collection<CodeValueData> allowedExpensesTagOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(AccountingConstants.EXPENSES_TAG_OPTION_CODE_NAME);

        return new GLAccountData(glAccountData, accountTypeOptions, usageOptions, assetHeaderAccountOptions, liabilityHeaderAccountOptions,
                equityHeaderAccountOptions, incomeHeaderAccountOptions, expenseHeaderAccountOptions, allowedAssetsTagOptions,
                allowedLiabilitiesTagOptions, allowedEquityTagOptions, allowedIncomeTagOptions, allowedExpensesTagOptions);
    }

    private List<GLAccountData> defaultIfEmpty(final List<GLAccountData> list) {
        List<GLAccountData> returnList = null;
        if (list != null && !list.isEmpty()) {
            returnList = list;
        }
        return returnList;
    }
}
