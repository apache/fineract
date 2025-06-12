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
package org.apache.fineract.portfolio.collateralmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.collateralmanagement.command.LoanCollateralDeletelCommand;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralDeleteRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralDeletelResponse;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralResponseData;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/loan-collateral-management")
@Component
@Tag(name = "Loan Collateral Management", description = "Loan Collateral Management is for managing collateral operations")
@RequiredArgsConstructor
public class LoanCollateralManagementApiResource {

    private final LoanCollateralManagementReadPlatformService loanCollateralManagementReadPlatformService;
    private final CommandPipeline pipeline;

    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(description = "Delete Loan Collateral", summary = "Delete Loan Collateral")
    public LoanCollateralDeletelResponse deleteLoanCollateral(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            @PathParam("loanId") final Long loanId, @PathParam("id") @Parameter(description = "loan collateral id") final Long id) {

        LoanCollateralDeletelCommand command = new LoanCollateralDeletelCommand();
        initCommand(idempotencyKey, command);

        LoanCollateralDeleteRequest request = new LoanCollateralDeleteRequest();
        request.setLoanId(loanId);
        request.setCollateralId(id);
        command.setPayload(request);

        Supplier<LoanCollateralDeletelResponse> result = pipeline.send(command);
        return result.get();
    }

    @GET
    @Path("{collateralId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(description = "Get Loan Collateral Details", summary = "Get Loan Collateral Details")
    public LoanCollateralResponseData getLoanCollateral(
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {
        return this.loanCollateralManagementReadPlatformService.getLoanCollateralResponseData(collateralId);
    }

    private void initCommand(String idempotencyKey, Command command) {
        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));
        command.setTenantId(tenantIdentifier);
        command.setIdempotencyKey(idempotencyKey);
    }
}
