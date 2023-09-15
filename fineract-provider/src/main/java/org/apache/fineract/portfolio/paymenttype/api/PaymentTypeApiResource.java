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
package org.apache.fineract.portfolio.paymenttype.api;

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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/paymenttypes")
@Component
@Tag(name = "Payment Type", description = "This defines the payment type")
@RequiredArgsConstructor
public class PaymentTypeApiResource {

    private final PlatformSecurityContext securityContext;
    private final DefaultToApiJsonSerializer<PaymentTypeData> jsonSerializer;
    private final PaymentTypeReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper;

    @GET
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all Payment Types", description = "Retrieve list of payment types")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentTypeApiResourceSwagger.GetPaymentTypesResponse.class)))) })
    public String getAllPaymentTypes(@Context final UriInfo uriInfo,
            @QueryParam("onlyWithCode") @Parameter(description = "onlyWithCode") final boolean onlyWithCode) {
        this.securityContext.authenticatedUser().validateHasReadPermission(PaymentTypeApiResourceConstants.ENTITY_NAME);
        Collection<PaymentTypeData> paymentTypes = null;
        if (onlyWithCode) {
            paymentTypes = this.readPlatformService.retrieveAllPaymentTypesWithCode();
        } else {
            paymentTypes = this.readPlatformService.retrieveAllPaymentTypes();
        }
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, paymentTypes, PaymentTypeApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a Payment Type", description = "Retrieves a payment type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PaymentTypeApiResourceSwagger.GetPaymentTypesPaymentTypeIdResponse.class))) })
    public String retrieveOnePaymentType(@PathParam("paymentTypeId") @Parameter(description = "paymentTypeId") final Long paymentTypeId,
            @Context final UriInfo uriInfo) {
        this.securityContext.authenticatedUser().validateHasReadPermission(PaymentTypeApiResourceConstants.ENTITY_NAME);
        this.paymentTypeRepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
        final PaymentTypeData paymentTypes = this.readPlatformService.retrieveOne(paymentTypeId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, paymentTypes, PaymentTypeApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Payment Type", description = "Creates a new Payment type\n\n" + "Mandatory Fields: name\n\n"
            + "Optional Fields: Description, isCashPayment,Position")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = PaymentTypeApiResourceSwagger.PostPaymentTypesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PaymentTypeApiResourceSwagger.PostPaymentTypesResponse.class))) })
    public String createPaymentType(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createPaymentType().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

    @PUT
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Payment Type", description = "Updates a Payment Type")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = PaymentTypeApiResourceSwagger.PutPaymentTypesPaymentTypeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PaymentTypeApiResourceSwagger.PutPaymentTypesPaymentTypeIdResponse.class))) })
    public String updatePaymentType(@PathParam("paymentTypeId") @Parameter(description = "paymentTypeId") final Long paymentTypeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePaymentType(paymentTypeId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Payment Type", description = "Deletes payment type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PaymentTypeApiResourceSwagger.DeletePaymentTypesPaymentTypeIdResponse.class))) })
    public String deleteCode(@PathParam("paymentTypeId") @Parameter(description = "paymentTypeId") final Long paymentTypeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deletePaymentType(paymentTypeId).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

}
