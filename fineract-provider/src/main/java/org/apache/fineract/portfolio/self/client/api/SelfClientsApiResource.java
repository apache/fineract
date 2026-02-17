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
package org.apache.fineract.portfolio.self.client.api;

import static java.util.Objects.requireNonNull;
import static org.apache.fineract.infrastructure.contentstore.processor.DataUrlDecoderContentProcessor.DATA_URL_DECODE_RESULT_CONTENT_TYPE;
import static org.apache.fineract.infrastructure.contentstore.processor.DataUrlEncoderContentProcessor.DATA_URL_ENCODE_PARAM_CONTENT_TYPE;
import static org.apache.fineract.infrastructure.contentstore.processor.DataUrlEncoderContentProcessor.DATA_URL_ENCODE_PARAM_ENCODING;
import static org.apache.fineract.infrastructure.contentstore.processor.ImageResizeContentProcessor.IMAGE_RESIZE_PARAM_FORMAT;
import static org.apache.fineract.infrastructure.contentstore.processor.ImageResizeContentProcessor.IMAGE_RESIZE_PARAM_MAX_HEIGHT;
import static org.apache.fineract.infrastructure.contentstore.processor.ImageResizeContentProcessor.IMAGE_RESIZE_PARAM_MAX_WIDTH;
import static org.apache.fineract.infrastructure.contentstore.processor.SizeContentProcessor.SIZE_RESULT_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorContext;
import org.apache.fineract.infrastructure.contentstore.detector.ContentDetectorManager;
import org.apache.fineract.infrastructure.contentstore.processor.Base64DecoderContentProcessor;
import org.apache.fineract.infrastructure.contentstore.processor.Base64EncoderContentProcessor;
import org.apache.fineract.infrastructure.contentstore.processor.ContentProcessorContext;
import org.apache.fineract.infrastructure.contentstore.processor.DataUrlDecoderContentProcessor;
import org.apache.fineract.infrastructure.contentstore.processor.DataUrlEncoderContentProcessor;
import org.apache.fineract.infrastructure.contentstore.processor.ImageResizeContentProcessor;
import org.apache.fineract.infrastructure.contentstore.processor.SizeContentProcessor;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.documentmanagement.command.ImageCreateCommand;
import org.apache.fineract.infrastructure.documentmanagement.command.ImageDeleteCommand;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageCreateRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageCreateResponse;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageDeleteRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageDeleteResponse;
import org.apache.fineract.infrastructure.documentmanagement.service.ImageReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.api.ClientChargesApiResource;
import org.apache.fineract.portfolio.client.api.ClientTransactionsApiResource;
import org.apache.fineract.portfolio.client.api.ClientsApiResource;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.self.client.data.SelfClientDataValidator;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.portfolio.self.config.SelfServiceModuleIsEnabledCondition;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.util.StreamResponseUtil;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Path("/v1/self/clients")
@Component
@Tag(name = "Self Client", description = "")
@RequiredArgsConstructor
@Conditional(SelfServiceModuleIsEnabledCondition.class)
public class SelfClientsApiResource {

    private final PlatformSecurityContext context;
    private final ClientsApiResource clientApiResource;
    private final ClientChargesApiResource clientChargesApiResource;
    private final ClientTransactionsApiResource clientTransactionsApiResource;
    private final AppuserClientMapperReadService appUserClientMapperReadService;
    private final SelfClientDataValidator dataValidator;
    private final ImageReadPlatformService imageReadPlatformService;
    private final CommandPipeline commandPipeline;
    private final ImageResizeContentProcessor imageResizeContentProcessor;
    private final Base64EncoderContentProcessor base64EncoderContentProcessor;
    private final Base64DecoderContentProcessor base64DecoderContentProcessor;
    private final DataUrlEncoderContentProcessor dataUrlEncoderContentProcessor;
    private final DataUrlDecoderContentProcessor dataUrlDecoderContentProcessor;
    private final SizeContentProcessor sizeContentProcessor;
    private final ContentDetectorManager contentDetectorManager;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Clients associated to the user", description = "The list capability of clients can support pagination and sorting.\n\n"
            + "Example Requests:\n" + "\n" + "self/clients\n" + "\n" + "self/clients?fields=displayName,officeName\n" + "\n"
            + "self/clients?offset=10&limit=50\n" + "\n" + "self/clients?orderBy=displayName&sortOrder=DESC")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfClientsApiResourceSwagger.GetSelfClientsResponse.class)))
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("displayName") @Parameter(description = "displayName") final String displayName,
            @QueryParam("firstName") @Parameter(description = "firstName") final String firstname,
            @QueryParam("lastName") @Parameter(description = "lastName") final String lastname,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("status") @Parameter(description = "status") final String status,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("legalForm") final Integer legalForm) {

        final Long officeId = null;
        final String externalId = null;
        final String hierarchy = null;
        final Boolean orphansOnly = null;
        return this.clientApiResource.retrieveAll(uriInfo, officeId, externalId, displayName, firstname, lastname, status, legalForm,
                hierarchy, offset, limit, orderBy, sortOrder, orphansOnly, true);
    }

    @GET
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client", description = "Retrieves a Client\n\n" + "Example Requests:\n" + "\n" + "self/clients/1\n"
            + "\n" + "self/clients/1?fields=id,displayName,officeName")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfClientsApiResourceSwagger.GetSelfClientsClientIdResponse.class)))
    public String retrieveOne(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {

        this.dataValidator.validateRetrieveOne(uriInfo);

        validateAppuserClientsMapping(clientId);

        final boolean staffInSelectedOfficeOnly = false;
        return this.clientApiResource.retrieveOne(clientId, uriInfo, staffInSelectedOfficeOnly);
    }

    @GET
    @Path("{clientId}/accounts")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve client accounts overview", description = "An example of how a loan portfolio summary can be provided. This is requested in a specific use case of the community application.\n"
            + "It is quite reasonable to add resources like this to simplify User Interface development.\n" + "\n" + "Example Requests:\n"
            + "\n" + "self/clients/1/accounts\n" + "\n" + "\n" + "self/clients/1/accounts?fields=loanAccounts,savingsAccounts")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfClientsApiResourceSwagger.GetSelfClientsClientIdAccountsResponse.class)))
    public String retrieveAssociatedAccounts(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {

        validateAppuserClientsMapping(clientId);

        return this.clientApiResource.retrieveAssociatedAccounts(clientId, uriInfo);
    }

    @GET
    @Path("{clientId}/images")
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.TEXT_PLAIN })
    @Operation(summary = "Retrieve Client Image", description = "Optional arguments are identical to those of Get Image associated with an Entity (Binary file)\n"
            + "\n" + "Example Requests:\n" + "\n" + "self/clients/1/images")
    @ApiResponse(responseCode = "200", description = "OK")
    public Response retrieveImage(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("maxWidth") @Parameter(example = "maxWidth") final Integer maxWidth,
            @QueryParam("maxHeight") @Parameter(example = "maxHeight") final Integer maxHeight,
            @QueryParam("output") @Parameter(example = "output") final String output) {

        validateAppuserClientsMapping(clientId);

        final var content = imageReadPlatformService.retrieveImage(ClientApiConstants.clientEntityName, clientId);

        // stream base64 encoded original format
        final var detectorCtx = contentDetectorManager.detect(ContentDetectorContext.builder().fileName(content.getFileName()).build());
        final var ctx = imageResizeContentProcessor.then(base64EncoderContentProcessor).then(dataUrlEncoderContentProcessor)
                .process(new ContentProcessorContext(content.getStream(),
                        Map.of(IMAGE_RESIZE_PARAM_MAX_WIDTH, maxWidth, IMAGE_RESIZE_PARAM_MAX_HEIGHT, maxHeight, IMAGE_RESIZE_PARAM_FORMAT,
                                detectorCtx.getFormat(), DATA_URL_ENCODE_PARAM_CONTENT_TYPE, detectorCtx.getMimeType(),
                                DATA_URL_ENCODE_PARAM_ENCODING, "base64")));

        final var streamResponseData = StreamResponseUtil.StreamResponseData.builder().fileName(content.getFileName())
                .type(TEXT_PLAIN_VALUE).stream(ctx.getInputStream()).build();

        return StreamResponseUtil.ok(streamResponseData);
    }

    @GET
    @Path("{clientId}/charges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Client Charges", description = "The list capability of client charges supports pagination.\n\n"
            + "Example Requests:\n" + "\n" + "self/clients/1/charges\n\n" + "self/clients/1/charges?offset=0&limit=5")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfClientsApiResourceSwagger.GetSelfClientsClientIdChargesResponse.class)))
    public String retrieveAllClientCharges(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @DefaultValue(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL) @QueryParam(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS) @Parameter(description = "chargeStatus") final String chargeStatus,
            @QueryParam("pendingPayment") @Parameter(description = "pendingPayment") final Boolean pendingPayment,
            @Context final UriInfo uriInfo, @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset) {

        validateAppuserClientsMapping(clientId);

        return this.clientChargesApiResource.retrieveAllClientCharges(clientId, chargeStatus, pendingPayment, uriInfo, limit, offset);
    }

    @GET
    @Path("{clientId}/charges/{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client Charge", description = "Retrieves a Client Charge\n\n" + "Example Requests:\n" + "\n"
            + "self/clients/1/charges/1\n" + "\n" + "\n" + "self/clients/1/charges/1?fields=name,id")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfClientsApiResourceSwagger.GetSelfClientsClientIdChargesChargeIdResponse.class)))
    public String retrieveClientCharge(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId, @Context final UriInfo uriInfo) {

        this.dataValidator.validateClientCharges(uriInfo);

        validateAppuserClientsMapping(clientId);

        return this.clientChargesApiResource.retrieveClientCharge(clientId, chargeId, uriInfo);
    }

    @GET
    @Path("{clientId}/transactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Client Transactions", description = "The list capability of client transaction can support pagination.\n\n"
            + "Example Requests:\n" + "\n" + "self/clients/189/transactions\n\n" + "self/clients/189/transactions?offset=10&limit=50")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfClientsApiResourceSwagger.GetSelfClientsClientIdTransactionsResponse.class)))
    public String retrieveAllClientTransactions(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo, @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit) {

        validateAppuserClientsMapping(clientId);

        return this.clientTransactionsApiResource.retrieveAllClientTransactions(clientId, uriInfo, offset, limit);
    }

    @GET
    @Path("{clientId}/transactions/{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client Transaction", description = "Retrieves a Client Transaction" + "Example Requests:\n" + "\n"
            + "self/clients/1/transactions/1\n" + "\n" + "\n" + "self/clients/1/transactions/1?fields=id,officeName")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfClientsApiResourceSwagger.GetSelfClientsClientIdTransactionsTransactionIdResponse.class)))
    public String retrieveClientTransaction(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        validateAppuserClientsMapping(clientId);

        return this.clientTransactionsApiResource.retrieveClientTransaction(clientId, transactionId, uriInfo);
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appUserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }

    @POST
    @Path("{clientId}/images")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(description = "Add new client image", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public ImageCreateResponse addNewClientImage(@PathParam("clientId") final Long clientId,
            @HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream is,
            @FormDataParam("file") final FormDataContentDisposition fileDetails, @FormDataParam("file") final FormDataBodyPart filePart) {

        validateAppuserClientsMapping(clientId);

        // TODO: add proper error messages
        requireNonNull(fileDetails, "");
        requireNonNull(filePart, "");
        requireNonNull(is, "");

        final var command = new ImageCreateCommand();

        command.setPayload(ImageCreateRequest.builder().entityId(clientId).entityType(ClientApiConstants.clientEntityName)
                .fileName(fileDetails.getFileName()).size(fileSize).type(filePart.getMediaType().toString()).stream(is).build());

        final Supplier<ImageCreateResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @POST
    @Path("{clientId}/images")
    @Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public ImageCreateResponse addNewClientImage(@PathParam("clientId") final Long clientId, final InputStream body) {
        validateAppuserClientsMapping(clientId);

        final var ctx = dataUrlDecoderContentProcessor.then(base64DecoderContentProcessor).then(sizeContentProcessor).process(body);

        final String contentType = ctx.getResult(DATA_URL_DECODE_RESULT_CONTENT_TYPE);
        Long size = ctx.getResult(SIZE_RESULT_VALUE);

        final var command = new ImageCreateCommand();

        command.setPayload(ImageCreateRequest.builder().entityId(clientId).entityType(ClientApiConstants.clientEntityName)
                .fileName(UUID.randomUUID().toString()).size(size).type(contentType).stream(ctx.getInputStream()).build());

        final Supplier<ImageCreateResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @DELETE
    @Path("{clientId}/images")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public ImageDeleteResponse deleteClientImage(@PathParam("clientId") final Long clientId) {
        validateAppuserClientsMapping(clientId);

        final var command = new ImageDeleteCommand();

        command.setPayload(ImageDeleteRequest.builder().entityId(clientId).entityType(ClientApiConstants.clientEntityName).build());

        final Supplier<ImageDeleteResponse> response = commandPipeline.send(command);

        return response.get();
    }

    @GET
    @Path("{clientId}/obligeedetails")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveObligeeDetails(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

        validateAppuserClientsMapping(clientId);

        return this.clientApiResource.retrieveObligeeDetails(clientId, uriInfo);
    }

}
