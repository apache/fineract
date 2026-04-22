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
package org.apache.fineract.portfolio.workingcapitalloan.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanBreachScheduleService;
import org.springframework.stereotype.Component;

@Path("/v1/working-capital-loans/{loanId}/breach-schedule")
@Component
@Tag(name = "Working Capital Loan Breach Schedule", description = "Manages breach schedule periods for Working Capital loans")
@RequiredArgsConstructor
public class WorkingCapitalLoanBreachScheduleApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "WORKINGCAPITALLOAN";

    private final PlatformSecurityContext context;
    private final WorkingCapitalLoanBreachScheduleService breachScheduleService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Breach Schedule", description = "Retrieves the breach schedule periods for a Working Capital loan")
    public List<WorkingCapitalLoanBreachScheduleData> retrieveBreachSchedule(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return breachScheduleService.retrieveBreachSchedule(loanId);
    }

}
