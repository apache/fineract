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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.paymenttype.command.CreatePaymentTypeCommand;
import org.apache.fineract.portfolio.paymenttype.command.DeletePaymentTypeCommand;
import org.apache.fineract.portfolio.paymenttype.command.UpdatePaymentTypeCommand;
import org.apache.fineract.portfolio.paymenttype.data.CreatePaymentTypeRequest;
import org.apache.fineract.portfolio.paymenttype.data.DeletePaymentTypeRequest;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeResponse;
import org.apache.fineract.portfolio.paymenttype.data.UpdatablePaymentTypeResponse;
import org.apache.fineract.portfolio.paymenttype.data.UpdatePaymentTypeRequest;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.mapper.PaymentTypeResponseMapper;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/paymenttypes")
@Component
@Tag(name = "Payment Type", description = "This defines the payment type")
@RequiredArgsConstructor
public class PaymentTypeApiResource {

    private final CommandPipeline commandPipeline;
    private final PaymentTypeRepositoryWrapper paymentTypeRepository;
    private final PaymentTypeResponseMapper paymentTypeResponseMapper;
    private final PaymentTypeReadPlatformService readPlatformService;

    @GET
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all Payment Types", description = "Retrieve list of payment types")
    public List<PaymentTypeResponse> getAllPaymentTypes(
            @QueryParam("onlyWithCode") @Parameter(description = "onlyWithCode") final boolean onlyWithCode) {
        return onlyWithCode ? paymentTypeResponseMapper.map(readPlatformService.retrieveAllPaymentTypesWithCode())
                : paymentTypeResponseMapper.map(readPlatformService.retrieveAllPaymentTypes());
    }

    @GET
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a Payment Type", description = "Retrieves a payment type")
    public PaymentTypeResponse retrieveOnePaymentType(
            @PathParam("paymentTypeId") @Parameter(description = "paymentTypeId") final Long paymentTypeId) {
        return paymentTypeResponseMapper.map(paymentTypeRepository.findOneWithNotFoundDetection(paymentTypeId));
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Payment Type", description = "Creates a new Payment type\n\n" + "Mandatory Fields: name\n\n"
            + "Optional Fields: Description, isCashPayment,Position")
    public UpdatablePaymentTypeResponse createPaymentType(
            @HeaderParam("Idempotency-Key") @Valid CreatePaymentTypeRequest paymentTypeRequest) {
        CreatePaymentTypeCommand commandPaymentType = new CreatePaymentTypeCommand();
        commandPaymentType.setId(UUID.randomUUID());
        commandPaymentType.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        commandPaymentType.setPayload(paymentTypeRequest);

        Supplier<UpdatablePaymentTypeResponse> result = commandPipeline.send(commandPaymentType);

        return result.get();
    }

    @PUT
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Payment Type", description = "Updates a Payment Type")
    public UpdatablePaymentTypeResponse updatePaymentType(
            @HeaderParam("Idempotency-Key") @PathParam("paymentTypeId") @Parameter(description = "paymentTypeId") final Long paymentTypeId,
            @Valid UpdatePaymentTypeRequest paymentTypeRequest) {
        final UpdatePaymentTypeCommand commandPaymentType = new UpdatePaymentTypeCommand();
        commandPaymentType.setId(UUID.randomUUID());
        commandPaymentType.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        commandPaymentType.setPayload(paymentTypeRequest.setId(paymentTypeId));

        final Supplier<UpdatablePaymentTypeResponse> result = commandPipeline.send(commandPaymentType);

        return result.get();
    }

    @DELETE
    @Path("{paymentTypeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Payment Type", description = "Deletes payment type")
    public UpdatablePaymentTypeResponse deleteCode(
            @HeaderParam("Idempotency-Key") @PathParam("paymentTypeId") @Parameter(description = "paymentTypeId") final Long paymentTypeId) {
        final DeletePaymentTypeCommand commandPaymentType = new DeletePaymentTypeCommand();
        commandPaymentType.setId(UUID.randomUUID());
        commandPaymentType.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        commandPaymentType.setPayload(new DeletePaymentTypeRequest(paymentTypeId));

        final Supplier<UpdatablePaymentTypeResponse> result = commandPipeline.send(commandPaymentType);

        return result.get();
    }
}
