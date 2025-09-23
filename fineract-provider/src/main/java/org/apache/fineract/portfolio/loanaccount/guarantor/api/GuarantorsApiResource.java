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

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.guarantor.GuarantorConstants;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.CreateGuarantorsCommand;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.DeleteGuarantorsCommand;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.UpdateGuarantorsCommand;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.UploadTemplateCommand;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.CreateGuarantorsRequest;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.CreateGuarantorsResponse;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.DeleteGuarantorsRequest;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.DeleteGuarantorsResponse;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.UpdateGuarantorsRequest;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.UpdateGuarantorsResponse;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.UploadTemplateRequest;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.UploadTemplateResponse;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorType;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorEnumerations;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.validation.constraints.Locale;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Path("/v1/loans/{loanId}/guarantors")
@Component
@Tag(name = "Guarantors", description = """
        Guarantors are individuals or entities that commit to repaying a loan if the primary borrower defaults. In the banking domain, they serve as an additional layer of security for lenders, helping to mitigate credit risk and improve loan eligibility for borrowers. This feature is especially important in microfinance and inclusive banking, where borrowers may lack traditional forms of collateral.
        `The Apache Fineract` supports the full lifecycle management of guarantors, including creation, validation, and association with loan accounts, enabling financial institutions to manage credit guarantees transparently and effectively.""")
@RequiredArgsConstructor
public class GuarantorsApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSION = "GUARANTOR";

    private final GuarantorReadPlatformService guarantorReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PlatformSecurityContext context;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final CommandPipeline commandPipeline;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public GuarantorData newGuarantorTemplate(@PathParam("loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        final List<EnumOptionData> guarantorTypeOptions = GuarantorEnumerations.guarantorType(GuarantorType.values());
        final Collection<CodeValueData> allowedClientRelationshipTypes = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(GuarantorConstants.GUARANTOR_RELATIONSHIP_CODE_NAME);
        final Collection<PortfolioAccountData> accountLinkingOptions = null;
        return GuarantorData.template(guarantorTypeOptions, allowedClientRelationshipTypes, accountLinkingOptions);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public List<GuarantorData> retrieveGuarantorDetails(@PathParam("loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        return this.guarantorReadPlatformService.retrieveGuarantorsForValidLoan(loanId);
    }

    @GET
    @Path("{guarantorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public GuarantorData retrieveGuarantorDetails(@Context final UriInfo uriInfo, @PathParam("loanId") final Long loanId,
            @PathParam("guarantorId") final Long guarantorId) {
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
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public CreateGuarantorsResponse createGuarantor(
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId,
            @Valid final CreateGuarantorsRequest request) {

        CreateGuarantorsRequest data = request.withLoanId(loanId);

        final CreateGuarantorsCommand command = new CreateGuarantorsCommand();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        command.setPayload(data);

        final Supplier<CreateGuarantorsResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @PUT
    @Path("{guarantorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public UpdateGuarantorsResponse updateGuarantor(
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId,
            @PathParam("guarantorId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.digits}") final Long guarantorId,
            @Valid final UpdateGuarantorsRequest request) {
        UpdateGuarantorsRequest data = request.withLoanIdAndGuarantorId(loanId, guarantorId);

        final UpdateGuarantorsCommand command = new UpdateGuarantorsCommand();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        command.setPayload(data);

        final Supplier<UpdateGuarantorsResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @DELETE
    @Path("{guarantorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public DeleteGuarantorsResponse deleteGuarantor(
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId,
            @PathParam("guarantorId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorId.digits}") final Long guarantorId,
            @QueryParam("guarantorFundingId") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorFundingId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.guarantorFundingId.digits}") final Long guarantorFundingId) {

        final DeleteGuarantorsRequest data = DeleteGuarantorsRequest.fromParameters(loanId, guarantorId, guarantorFundingId);

        final DeleteGuarantorsCommand command = new DeleteGuarantorsCommand();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        command.setPayload(data);

        final Supplier<DeleteGuarantorsResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @GET
    @Path("accounts/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public GuarantorData accountsTemplate(@QueryParam("clientId") final Long clientId, @PathParam("loanId") final Long loanId) {

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
    public Response getGuarantorTemplate(@QueryParam("officeId") final Long officeId, @QueryParam("dateFormat") final String dateFormat,
            @PathParam("loanId") final Long loanId) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.GUARANTORS.toString(), officeId, null, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload guarantor template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public UploadTemplateResponse postGuarantorTemplate(
            @FormDataParam("file") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.upload.template.file.required}") InputStream uploadedInputStream,
            @FormDataParam("file") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.upload.template.fileDetail.required}") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") @NotBlank(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.locale.notBlank}") @Size(max = 50, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.locale.size}") @Locale final String locale,
            @FormDataParam("dateFormat") @Size(max = 20, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.date.format.size}") final String dateFormat,
            @PathParam("loanId") @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.notNull}") @PositiveOrZero(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.positiveOrZero}") @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.loanId.digits}") final Long loanId) {

        final UploadTemplateRequest data = UploadTemplateRequest.fromParameters(uploadedInputStream, fileDetail, locale, dateFormat);

        final UploadTemplateCommand command = new UploadTemplateCommand();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        command.setPayload(data);

        final Supplier<UploadTemplateResponse> response = commandPipeline.send(command);

        return response.get();
    }
}
