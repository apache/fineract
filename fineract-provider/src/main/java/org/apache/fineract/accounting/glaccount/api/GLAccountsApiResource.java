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
package org.apache.fineract.accounting.glaccount.api;

import java.io.InputStream;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.*;
import io.swagger.models.parameters.HeaderParameter;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.journalentry.data.JournalEntryAssociationParametersData;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/glaccounts")
@Component
@Scope("singleton")
@Api(value = "General Ledger Account", description = "Ledger accounts represent an Individual account within an Organizations Chart Of Accounts(COA) and are assigned a name and unique number by which they can be identified. \n" + "All transactions relating to a company's assets, liabilities, owners' equity, revenue and expenses are recorded against these accounts")
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
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;



    @Autowired
    public GLAccountsApiResource(final PlatformSecurityContext context, final GLAccountReadPlatformService glAccountReadPlatformService,
            final DefaultToApiJsonSerializer<GLAccountData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final AccountingDropdownReadPlatformService dropdownReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiJsonSerializerService = toApiJsonSerializer;
        this.glAccountReadPlatformService = glAccountReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.bulkImportWorkbookService=bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService=bulkImportWorkbookPopulatorService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve GL Accounts Template", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Request:\n" + "\n" + "glaccounts/template\n" + "glaccounts/template?type=1\n" + "\n" + "type is optional and integer value from 1 to 5.\n" + "\n" + "1.Assets \n" + "2.Liabilities \n" + "3.Equity \n" + "4.Income \n" + "5.Expenses")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = GLAccountsApiResourceSwagger.GetGLAccountsTemplateResponse.class)})
    public String retrieveNewAccountDetails(@Context final UriInfo uriInfo, @QueryParam("type") @ApiParam(value = "type") final Integer type) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        GLAccountData glAccountData = this.glAccountReadPlatformService.retrieveNewGLAccountDetails(type);
        glAccountData = handleTemplate(glAccountData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, glAccountData, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List General Ledger Accounts", notes = "ARGUMENTS\n" + "type Integer optional manualEntriesAllowed boolean optional usage Integer optional disabled boolean optional parentId Long optional tagId Long optional\n" + "Example Requests:\n" + "\n" + "glaccounts\n" + "\n" + "\n" + "glaccounts?type=1&manualEntriesAllowed=true&usage=1&disabled=false\n" + "\n" + "glaccounts?fetchRunningBalance=true")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = GLAccountsApiResourceSwagger.GetGLAccountsResponse.class, responseContainer = "list")})
    public String retrieveAllAccounts(@Context final UriInfo uriInfo, @QueryParam("type") @ApiParam(value = "type") final Integer type,
            @QueryParam("searchParam") @ApiParam(value = "searchParam") final String searchParam, @QueryParam("usage") @ApiParam(value = "usage") final Integer usage,
            @QueryParam("manualEntriesAllowed") @ApiParam(value = "manualEntriesAllowed") final Boolean manualEntriesAllowed, @QueryParam("disabled") @ApiParam(value = "disabled") final Boolean disabled,
            @QueryParam("fetchRunningBalance") @ApiParam(value = "fetchRunningBalance") final boolean runningBalance) {

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
    @ApiOperation(value = "Retrieve a General Ledger Account", notes = "Example Requests:\n" + "\n" + "glaccounts/1\n" + "\n" + "\n" + "glaccounts/1?template=true\n" + "\n" + "\n" + "glaccounts/1?fields=name,glCode\n" + "\n" + "\n" + "glaccounts/1?fetchRunningBalance=true")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = GLAccountsApiResourceSwagger.GetGLAccountsResponse.class)})
    public String retreiveAccount(@PathParam("glAccountId") @ApiParam(value = "glAccountId") final Long glAccountId, @Context final UriInfo uriInfo,
            @QueryParam("fetchRunningBalance") @ApiParam(value = "fetchRunningBalance") final boolean runningBalance) {

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
    @ApiOperation(value = "Create a General Ledger Account", notes = "Note: You may optionally create Hierarchical Chart of Accounts by using the \"parentId\" property of an Account\n" + "Mandatory Fields: \n" + "name, glCode, type, usage and manualEntriesAllowed")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "body", value = "body", dataType = "body", dataTypeClass = GLAccountsApiResourceSwagger.PostGLAccountsRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = GLAccountsApiResourceSwagger.PostGLAccountsResponse.class)})
    public String createGLAccount(@ApiParam(hidden = true) final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createGLAccount().withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{glAccountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update an Accounting closure", notes = "Once an accounting closure is created, only the comments associated with it may be edited\n" + "\n")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "body", value = "body", dataType = "body", dataTypeClass = GLAccountsApiResourceSwagger.PutGLAccountsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = GLAccountsApiResourceSwagger.PutGLAccountsResponse.class)})
    public String updateGLAccount(@PathParam("glAccountId") @ApiParam(value = "glAccountId") final Long glAccountId, @ApiParam(hidden = true) final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateGLAccount(glAccountId).withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{glAccountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete an accounting closure", notes = "Note: Only the latest accounting closure associated with a branch may be deleted.")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = GLAccountsApiResourceSwagger.DeleteGLAccountsRequest.class)})
    public String deleteGLAccount(@PathParam("glAccountId") @ApiParam(value = "glAccountId") final Long glAccountId) {

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

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getGlAccountsTemplate(@QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.CHART_OF_ACCOUNTS.toString(),null,null,dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postGlAccountsTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") final String locale, @FormDataParam("dateFormat") final String dateFormat){
        Long importDocumentId= bulkImportWorkbookService.importWorkbook(GlobalEntityType.CHART_OF_ACCOUNTS.toString(), uploadedInputStream,fileDetail,
                locale,dateFormat);
        return this.apiJsonSerializerService.serialize(importDocumentId);
    }
}
