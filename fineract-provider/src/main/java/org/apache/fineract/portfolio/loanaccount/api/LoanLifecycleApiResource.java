/*
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
package org.apache.fineract.portfolio.loanaccount.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.service.LoanLifecycleAction;
import org.apache.fineract.portfolio.loanaccount.service.LoanLifecycleFacade;
import org.apache.fineract.portfolio.loanaccount.service.LoanLifecycleSnapshot;
import org.springframework.stereotype.Component;

/**
 * Additive lifecycle API for clients migrating from the command-based loan resource.
 *
 * <p>Use {@code GET /v1/loans/{loanId}/lifecycle} to retrieve lifecycle state and permitted actions. Use
 * {@code POST /v1/loans/{loanId}/lifecycle-actions} with an {@code action} field and the same action-specific fields
 * accepted by the corresponding legacy loan command. For example, {@code {"action":"APPROVE", "approvedOnDate":
 * "01 January 2026", "dateFormat":"dd MMMM yyyy", "locale":"en"}} replaces
 * {@code POST /v1/loans/{loanId}?command=approve}.
 *
 * <p>Lifecycle action requests are dispatched through the existing command service. This preserves action-specific
 * {@code LOAN} permissions, audit records, maker-checker behavior, and the platform's {@code Idempotency-Key} handling.
 */
@Path("/v1/loans/{loanId}")
@Component
@RequiredArgsConstructor
public class LoanLifecycleApiResource {

    private static final String LOAN_RESOURCE = "LOAN";
    private static final String ACTION = "action";

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final LoanLifecycleFacade loanLifecycleFacade;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Path("lifecycle")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve loan lifecycle", operationId = "retrieveLoanLifecycle")
    public LoanLifecycleResponse retrieve(@PathParam("loanId") @Parameter(required = true) Long loanId) {
        context.authenticatedUser().validateHasReadPermission(LOAN_RESOURCE);
        return LoanLifecycleResponse.from(loanLifecycleFacade.retrieve(loanId));
    }

    @POST
    @Path("lifecycle-actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Execute a loan lifecycle action", operationId = "executeLoanLifecycleAction")
    public LoanLifecycleActionResponse execute(@PathParam("loanId") @Parameter(required = true) Long loanId,
            @Parameter(hidden = true) String apiRequestBodyAsJson) {
        JsonElement parsedRequest = fromJsonHelper.parse(apiRequestBodyAsJson);
        if (!parsedRequest.isJsonObject() || !parsedRequest.getAsJsonObject().has(ACTION)) {
            throw new IllegalArgumentException("Loan lifecycle action request must include an action field");
        }

        JsonObject request = parsedRequest.getAsJsonObject();
        LoanLifecycleAction action = LoanLifecycleAction.valueOf(request.get(ACTION).getAsString().toUpperCase());
        request.remove(ACTION);

        CommandWrapper command = commandFor(action, loanId, fromJsonHelper.toJson(request));
        CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(command);
        return new LoanLifecycleActionResponse(action, result, LoanLifecycleResponse.from(loanLifecycleFacade.retrieve(loanId)));
    }

    private CommandWrapper commandFor(LoanLifecycleAction action, Long loanId, String requestJson) {
        CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(requestJson);
        return switch (action) {
            case APPROVE -> builder.approveLoanApplication(loanId).build();
            case REJECT -> builder.rejectLoanApplication(loanId).build();
            case WITHDRAW -> builder.withdrawLoanApplication(loanId).build();
            case DISBURSE -> builder.disburseLoanApplication(loanId).build();
            case REPAY -> builder.loanRepaymentTransaction(loanId).build();
            case CLOSE -> builder.closeLoanTransaction(loanId).build();
        };
    }

    public record LoanLifecycleResponse(Long loanId, LoanStatus status, List<LoanLifecycleAction> permittedActions) {

        static LoanLifecycleResponse from(LoanLifecycleSnapshot snapshot) {
            return new LoanLifecycleResponse(snapshot.loanId(), snapshot.status(), snapshot.permittedActions());
        }
    }

    public record LoanLifecycleActionResponse(LoanLifecycleAction action, CommandProcessingResult command,
            LoanLifecycleResponse lifecycle) {}
}
