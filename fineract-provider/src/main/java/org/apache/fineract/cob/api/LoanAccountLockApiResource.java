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
package org.apache.fineract.cob.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.data.LoanAccountLockResponseDTO;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.springframework.stereotype.Component;

@Path("/v1/loans")
@Component
@Tag(name = "Loan Account Lock", description = "")
@RequiredArgsConstructor
public class LoanAccountLockApiResource {

    private final LoanAccountLockService loanAccountLockService;

    @GET
    @Path("locked")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List locked loan accounts", description = "Returns the locked loan IDs")
    public LoanAccountLockResponseDTO retrieveLockedAccounts(@QueryParam("page") Integer pageParam,
            @QueryParam("limit") Integer limitParam) {
        int page = Objects.requireNonNullElse(pageParam, 0);
        int limit = Objects.requireNonNullElse(limitParam, 50);

        return new LoanAccountLockResponseDTO(page, limit, loanAccountLockService.getLockedLoanAccountByPage(page, limit));
    }
}
