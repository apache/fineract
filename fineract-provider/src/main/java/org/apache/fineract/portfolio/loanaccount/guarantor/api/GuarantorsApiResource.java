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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.guarantor.GuarantorConstants;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.CreateGuarantorsCommand;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.DeleteGuarantorsCommand;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.UpdateGuarantorsCommand;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.CreateGuarantorsRequest;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.CreateGuarantorsResponse;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.DeleteGuarantorsResponse;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.UpdateGuarantorsRequest;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.UpdateGuarantorsResponse;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorType;
import org.apache.fineract.portfolio.loanaccount.guarantor.mapper.GuarantorCommandMapper;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorEnumerations;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Path("/v1/loans/{loanId}/guarantors")
@Component
@Tag(name = "Guarantors", description = """
        Guarantors are individuals or entities that commit to repaying a loan if the primary borrower defaults. In the banking domain, they serve as an additional layer of security for lenders, helping to mitigate credit risk and improve loan eligibility for borrowers. This feature is especially important in microfinance and inclusive banking, where borrowers may lack traditional forms of collateral.
        `The Apache Fineract` supports the full lifecycle management of guarantors, including creation, validation, and association with loan accounts, enabling financial institutions to manage credit guarantees transparently and effectively.""")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@RequiredArgsConstructor
public class GuarantorsApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSION = "GUARANTOR";

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
    private final CommandPipeline commandPipeline;
    private final GuarantorCommandMapper mapper;

    @GET
    @Path("template")
    @Operation(summary = "Retrieve Guarantor Account Linking Template", description = """
            Retrieves a JSON template with metadata required to link a guarantor’s savings account to a loan.
            Accepts `loanId` as a path parameter to associate the template contextually.
            Returns possible `guarantorType` options, valid `client relationship` types, and placeholder for account linking options.
            Primarily used to pre-fill UI forms or assist in constructing valid guarantor linking requests.
            This API is part of The Apache Fineract platform's guarantor management module.""")
    public GuarantorData newGuarantorTemplate(
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        final List<EnumOptionData> guarantorTypeOptions = GuarantorEnumerations.guarantorType(GuarantorType.values());
        final Collection<CodeValueData> allowedClientRelationshipTypes = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(GuarantorConstants.GUARANTOR_RELATIONSHIP_CODE_NAME);
        final Collection<PortfolioAccountData> accountLinkingOptions = null;
        return GuarantorData.template(guarantorTypeOptions, allowedClientRelationshipTypes, accountLinkingOptions);
    }

    @GET
    @Operation(summary = "Retrieve Guarantor Details", description = """
            Retrieves a guarantor details from the database.
            This API is part of The Apache Fineract platform's guarantor management module.""")
    public List<GuarantorData> retrieveGuarantorDetails(
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        return this.guarantorReadPlatformService.retrieveGuarantorsForValidLoan(loanId);
    }

    @GET
    @Path("{guarantorId}")
    @Operation(summary = "Retrieve Guarantor Details with Optional Template Metadata", description = """
            Fetches details of a specific guarantor associated with a given loan using the `loanId` and `guarantorId` path parameters.
            If the request includes the `template=true` query parameter, the response is enriched with metadata such as `guarantorType` options,
            valid client relationship types, and placeholder savings account linking options.
            This enriched data helps pre-fill UI forms for updating guarantor information.
            This API is part of The Apache Fineract platform's guarantor management module.""")
    public GuarantorData retrieveGuarantorDetails(@Context final UriInfo uriInfo,
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId,
            @PathParam("guarantorId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.digits}") final Long guarantorId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

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
        return guarantorData;
    }

    @POST
    @Operation(summary = "Add a Guarantor to a Loan Account", description = """
            Creates and attaches a new guarantor to the specified loan account.
            The guarantor can be either an existing client or an external individual.
            The request body must include the relevant guarantor details based on the guarantor type.
            Validation is enforced on `loanId` and `request fields`.""")
    public CreateGuarantorsResponse createGuarantor(
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId,
            @Valid final CreateGuarantorsRequest request) {

        final CreateGuarantorsCommand command = mapper.toCommand(loanId, request);
        final Supplier<CreateGuarantorsResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @PUT
    @Path("{guarantorId}")
    @Operation(summary = "Update an Existing Guarantor details on a Loan Account", description = """
            Updates the details of an existing guarantor associated with a specified loan account.
            The guarantor may be an existing client or an external person.
            Both the `loanId` and `guarantorId` must be valid and correspond to an existing loan-guarantor relationship.
            The request body must include updated guarantor information, and validations are applied on all relevant fields.
            This API is part of The Apache Fineract platform's guarantor management capabilities.""")
    public UpdateGuarantorsResponse updateGuarantor(
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId,
            @PathParam("guarantorId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.digits}") final Long guarantorId,
            @Valid final UpdateGuarantorsRequest request) {

        final UpdateGuarantorsCommand command = mapper.toCommand(loanId, guarantorId, request);
        final Supplier<UpdateGuarantorsResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @DELETE
    @Path("{guarantorId}")
    @Operation(summary = "Delete a Guarantor from a Loan Account", description = """
            Removes an existing guarantor associated with the specified loan account.
            You can delete either the entire guarantor record or a specific guarantor funding detail by passing the optional `guarantorFundingId` query parameter.
            Both `loanId` and `guarantorId` must reference a valid loan and guarantor relationship.
            Validation is enforced on all input parameters. The guarantor data always
            stays in the database and it never deleted, only the relevant guarantor flags are set to `false`
            This API is part of The Apache Fineract platform's guarantor management capabilities.""")
    public DeleteGuarantorsResponse deleteGuarantor(
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId,
            @PathParam("guarantorId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.digits}") final Long guarantorId,
            @QueryParam("guarantorFundingId") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorFundingId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorFundingId.digits}") final Long guarantorFundingId) {

        final DeleteGuarantorsCommand command = mapper.toCommand(loanId, guarantorId, guarantorFundingId);
        final Supplier<DeleteGuarantorsResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @GET
    @Path("accounts/template")
    @Operation(summary = "Retrieve Guarantor Account Linking Options Template", description = """
            Provides a template containing available savings accounts for a client that can be linked as guarantor funding sources for a loan.
            Requires both `clientId` and `loanId` parameters.
            Only returns account linking options if the specified loan requires a guarantee.
            Useful for populating UI forms during guarantor creation or modification.
            This API is part of The Apache Fineract platform's guarantor management capabilities.""")
    public GuarantorData accountsTemplate(
            @QueryParam("clientId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.clientId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.clientId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.clientId.digits}") final Long clientId,
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(PortfolioAccountType.SAVINGS.getValue(), clientId, null);
        Collection<PortfolioAccountData> accountLinkingOptions = null;
        if (this.loanReadPlatformService.isGuaranteeRequired(loanId)) {
            accountLinkingOptions = this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);
        }
        return GuarantorData.template(null, null, accountLinkingOptions);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    @Operation(summary = "Retrieve Guarantor Account Linking Options Template", description = "Generates and downloads an Excel template for linking savings accounts as guarantor funding sources for a loan. "
            + "Requires `loanId` as a path parameter and optionally accepts `officeId` and `dateFormat` as query parameters. "
            + "This template assists in bulk creation or modification of guarantor information, especially in contexts where a loan requires a guarantee. "
            + "Useful for UI integrations to pre-fill form options for account linkage. "
            + "This API is part of The Apache Fineract platform's guarantor management capabilities.")
    public Response getGuarantorTemplate(
            @QueryParam("officeId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.officeId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.officeId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.officeId.digits}") final Long officeId,
            @QueryParam("dateFormat") final String dateFormat,
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.GUARANTORS.toString(), officeId, null, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload guarantor template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    @Operation(summary = "Upload Guarantor Account Linking Template", description = "Allows uploading of an Excel template used for linking guarantor savings accounts to a loan. "
            + "Requires `loanId` as a path parameter and accepts `file` as a multipart form-data field. "
            + "Optional fields include `locale` and `dateFormat` for regional formatting. "
            + "This endpoint is used to process bulk guarantor account linking data and store it for further processing. "
            + "It supports use cases such as bulk creation or update of guarantor relationships where savings accounts serve as collateral. "
            + "This API is part of The Apache Fineract platform's guarantor management module.")
    public String postGuarantorTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat,
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.GUARANTORS.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return this.apiJsonSerializerService.serialize(importDocumentId);
    }
}
