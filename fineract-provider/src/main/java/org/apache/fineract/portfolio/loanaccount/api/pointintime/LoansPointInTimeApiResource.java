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
package org.apache.fineract.portfolio.loanaccount.api.pointintime;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.DateParam;
import org.apache.fineract.portfolio.loanaccount.api.pointintime.data.RetrieveLoansPointInTimeExternalIdsRequest;
import org.apache.fineract.portfolio.loanaccount.api.pointintime.data.RetrieveLoansPointInTimeRequest;
import org.apache.fineract.portfolio.loanaccount.data.LoanPointInTimeData;
import org.springframework.stereotype.Component;

@Path("/v1/loans/at-date")
@Component
@Tag(name = "LoansPointInTime", description = "API to enable clients to retrieve Loan states in a specific point in time")
@RequiredArgsConstructor
public class LoansPointInTimeApiResource implements LoansPointInTimeApi {

    private final LoansPointInTimeApiDelegate delegate;

    @GET
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Override
    public LoanPointInTimeData retrieveLoanPointInTime(@PathParam("loanId") @Parameter(description = "loanId", required = true) Long loanId,
            @QueryParam("date") @Parameter(description = "date", required = true) DateParam dateParam,
            @QueryParam("dateFormat") @Parameter(description = "dateFormat") final String dateFormat,
            @QueryParam("locale") @Parameter(description = "locale") final String locale) {
        return delegate.retrieveLoanPointInTime(loanId, dateParam, dateFormat, locale);
    }

    @GET
    @Path("external-id/{loanExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Override
    public LoanPointInTimeData retrieveLoanPointInTimeByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) String loanExternalIdStr,
            @QueryParam("date") @Parameter(description = "date", required = true) DateParam dateParam,
            @QueryParam("dateFormat") @Parameter(description = "dateFormat") final String dateFormat,
            @QueryParam("locale") @Parameter(description = "locale") final String locale) {
        return delegate.retrieveLoanPointInTimeByExternalId(loanExternalIdStr, dateParam, dateFormat, locale);
    }

    @POST
    @Path("search")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Override
    public List<LoanPointInTimeData> retrieveLoansPointInTime(@Parameter RetrieveLoansPointInTimeRequest request) {
        return delegate.retrieveLoansPointInTime(request);
    }

    @POST
    @Path("search/external-id")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Override
    public List<LoanPointInTimeData> retrieveLoansPointInTimeByExternalIds(@Parameter RetrieveLoansPointInTimeExternalIdsRequest request) {
        return delegate.retrieveLoansPointInTimeByExternalIds(request);
    }

}
