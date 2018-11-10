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
package org.apache.fineract.portfolio.loanaccount.guarantor.api;

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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
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
import org.apache.fineract.portfolio.account.AccountDetailConstants;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.guarantor.GuarantorConstants;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorType;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorEnumerations;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/guarantors")
@Component
@Scope("singleton")
public class GuarantorsApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "loanId",
            "clientRelationshipType",
            "guarantorType", "firstname", "lastname", "entityId", "externalId", "officeName", "joinedDate", "addressLine1", "addressLine2",
            "city", "state", "zip", "country", "mobileNumber", "housePhoneNumber", "comment", "dob", "guarantorTypeOptions",
            "allowedClientRelationshipTypes"));

	private static final Set<String> ACCOUNT_TRANSFER_API_RESPONSE_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(AccountDetailConstants.idParamName, AccountTransfersApiConstants.transferDescriptionParamName,
					AccountTransfersApiConstants.currencyParamName));

    private final String resourceNameForPermission = "GUARANTOR";

    private final GuarantorReadPlatformService guarantorReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final DefaultToApiJsonSerializer<GuarantorData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;


    @Autowired
    public GuarantorsApiResource(final PlatformSecurityContext context, final GuarantorReadPlatformService guarantorReadPlatformService,
            final DefaultToApiJsonSerializer<GuarantorData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService,
            final LoanReadPlatformService loanReadPlatformService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiJsonSerializerService = toApiJsonSerializer;
        this.guarantorReadPlatformService = guarantorReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.portfolioAccountReadPlatformService = portfolioAccountReadPlatformService;
        this.loanReadPlatformService = loanReadPlatformService;
        this.bulkImportWorkbookService=bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService=bulkImportWorkbookPopulatorService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String newGuarantorTemplate(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final List<EnumOptionData> guarantorTypeOptions = GuarantorEnumerations.guarantorType(GuarantorType.values());
        final Collection<CodeValueData> allowedClientRelationshipTypes = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(GuarantorConstants.GUARANTOR_RELATIONSHIP_CODE_NAME);
        final Collection<PortfolioAccountData> accountLinkingOptions = null;
        final GuarantorData guarantorData = GuarantorData.template(guarantorTypeOptions, allowedClientRelationshipTypes,
                accountLinkingOptions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, guarantorData, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGuarantorDetails(@Context final UriInfo uriInfo, @PathParam("loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final List<GuarantorData> guarantorDatas = this.guarantorReadPlatformService.retrieveGuarantorsForValidLoan(loanId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.apiJsonSerializerService.serialize(settings, guarantorDatas, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{guarantorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGuarantorDetails(@Context final UriInfo uriInfo, @PathParam("loanId") final Long loanId,
            @PathParam("guarantorId") final Long guarantorId) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        GuarantorData guarantorData = this.guarantorReadPlatformService.retrieveGuarantor(loanId, guarantorId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final Collection<CodeValueData> allowedClientRelationshipTypes = this.codeValueReadPlatformService
                    .retrieveCodeValuesByCode(GuarantorConstants.GUARANTOR_RELATIONSHIP_CODE_NAME);
            final List<EnumOptionData> guarantorTypeOptions = GuarantorEnumerations.guarantorType(GuarantorType.values());
            final Collection<PortfolioAccountData> accountLinkingOptions = null;
            guarantorData = GuarantorData.templateOnTop(guarantorData, guarantorTypeOptions, allowedClientRelationshipTypes,
                    accountLinkingOptions);
        }

        return this.apiJsonSerializerService.serialize(settings, guarantorData, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createGuarantor(@PathParam("loanId") final Long loanId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createGuarantor(loanId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{guarantorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateGuarantor(@PathParam("loanId") final Long loanId, @PathParam("guarantorId") final Long guarantorId,
            final String jsonRequestBody) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateGuarantor(loanId, guarantorId).withJson(jsonRequestBody)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{guarantorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteGuarantor(@PathParam("loanId") final Long loanId, @PathParam("guarantorId") final Long guarantorId,
            @QueryParam("guarantorFundingId") final Long guarantorFundingId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteGuarantor(loanId, guarantorId, guarantorFundingId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @GET
    @Path("accounts/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String accountsTemplate(@QueryParam("clientId") final Long clientId, @PathParam("loanId") final Long loanId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(PortfolioAccountType.SAVINGS.getValue(), clientId, null);
        Collection<PortfolioAccountData> accountLinkingOptions = null;
        if (this.loanReadPlatformService.isGuaranteeRequired(loanId)) {
            accountLinkingOptions = this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);
        }
        final GuarantorData guarantorData = GuarantorData.template(null, null, accountLinkingOptions);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, guarantorData, ACCOUNT_TRANSFER_API_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getGuarantorTemplate(@QueryParam("officeId") final Long officeId,@QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.GUARANTORS.toString(), officeId,null,dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postGuarantorTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,@FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId =this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.GUARANTORS.toString(),
                uploadedInputStream,fileDetail,locale,dateFormat);
        return this.apiJsonSerializerService.serialize(importDocumentId);
    }
}