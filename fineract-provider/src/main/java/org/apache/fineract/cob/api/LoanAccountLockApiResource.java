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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.data.LoanAccountLockResponseDTO;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.springframework.stereotype.Component;

@Path("/loans")
@Component
@Tag(name = "Loan Account Lock", description = "")
@RequiredArgsConstructor
public class LoanAccountLockApiResource {

    private static final Set<String> LOAN_ACCOUNT_LOCK_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("page", "limit", "content"));

    private final LoanAccountLockService loanAccountLockService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<LoanAccountLockResponseDTO> businessStepConfigSerializeService;

    @GET
    @Path("locked")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List locked loan accounts", description = "Returns the locked loan IDs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanAccountLockApiResourceSwagger.GetLoanAccountLockResponse.class)))) })
    public String retrieveLockedAccounts(@Context final UriInfo uriInfo, @QueryParam("page") String page,
            @QueryParam("limit") String limit) {

        List<LoanAccountLock> lockedLoanAccounts = loanAccountLockService.getLockedLoanAccountByPage(Integer.parseInt(page),
                Integer.parseInt(limit));
        LoanAccountLockResponseDTO response = new LoanAccountLockResponseDTO();
        response.setPage(Integer.parseInt(page));
        response.setLimit(Integer.parseInt(limit));
        response.setContent(lockedLoanAccounts);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return businessStepConfigSerializeService.serialize(settings, response, LOAN_ACCOUNT_LOCK_RESPONSE_DATA_PARAMETERS);
    }
}
