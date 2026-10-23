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
package org.apache.fineract.portfolio.loanaccount.payoffpreview.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.data.PayoffScheduleData;
import org.apache.fineract.portfolio.loanaccount.payoffpreview.service.PayoffSchedulePreviewService;
import org.springframework.stereotype.Component;

@Path("/v1/payoff-schedule-preview")
@Component
@Tag(name = "Payoff Schedule Preview", description = "Computes a progressive-loan payoff schedule from raw inputs, without creating or referencing a loan and without persisting anything.")
@RequiredArgsConstructor
public class PayoffSchedulePreviewApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN";

    private final PlatformSecurityContext context;
    private final PayoffSchedulePreviewService payoffSchedulePreviewService;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Compute a payoff schedule preview", operationId = "calculatePayoffSchedulePreview", description = """
            Computes a declining-balance, equal-installment schedule with the progressive loan engine, either over a term or by \
            applying a fixed payment each period until the amount is paid off. The term must use the frequency's unit and be an \
            exact multiple of it, as for a regular loan. Due dates are not moved for holidays or non-working days. Requires the \
            LOAN read permission.""")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = PayoffSchedulePreviewApiResourceSwagger.PostPayoffSchedulePreviewRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PayoffSchedulePreviewApiResourceSwagger.PostPayoffSchedulePreviewResponse.class)))
    public PayoffScheduleData calculateSchedulePreview(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return payoffSchedulePreviewService.generatePreview(apiRequestBodyAsJson);
    }
}
