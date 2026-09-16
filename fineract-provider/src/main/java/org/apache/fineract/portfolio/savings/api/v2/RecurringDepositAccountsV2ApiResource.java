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
package org.apache.fineract.portfolio.savings.api.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.springframework.stereotype.Component;

@Path("/v2/recurringdepositaccounts")
@Component
@Tag(name = "RecurringDepositAccountsV2", description = "V2 endpoint that always returns a paged response (totalFilteredRecords + pageItems) for recurring deposit accounts, removing the ambiguous paged response typing of the v1 endpoint.")
@RequiredArgsConstructor
public class RecurringDepositAccountsV2ApiResource {

    private final PlatformSecurityContext context;
    private final DepositAccountsV2ApiDelegate delegate;
    private final SqlValidator sqlValidator;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Recurring deposit applications/accounts", operationId = "retrieveAllRecurringDepositAccountsV2", description = "Get a paged list of recurring deposit accounts. Unlike the v1 endpoint, this endpoint always returns a paged response containing totalFilteredRecords and pageItems.\n\n"
            + "Example Requests:\n\n" + "recurringdepositaccounts\n\n" + "recurringdepositaccounts?fields=name")
    public Page<DepositAccountData> retrieveAllDepositAccounts(
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        final PaginationParameters parameters = PaginationParameters.builder().paged(true).limit(limit).offset(offset).orderBy(orderBy)
                .sortOrder(sortOrder).build();
        return delegate.retrieveAllDepositAccounts(DepositAccountType.RECURRING_DEPOSIT, parameters);
    }
}
