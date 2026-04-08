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
package org.apache.fineract.batch.command.internal;

import static org.apache.fineract.batch.command.CommandStrategyUtils.relativeUrlWithoutVersion;

import com.google.common.base.Splitter;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.command.CommandStrategyUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.loanaccount.api.LoanChargesApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link CommandStrategy} and Create Charge for a Loan by external id. It passes the contents of the body
 * from the BatchRequest to {@link LoanChargesApiResource} and gets back the response. This class will also catch any
 * errors raised by {@link LoanChargesApiResource} and map those errors to appropriate status codes in BatchResponse.
 *
 * @see CommandStrategy
 * @see BatchRequest
 * @see BatchResponse
 */
@Component
@RequiredArgsConstructor
public class CreateChargeByLoanExternalIdCommandStrategy implements CommandStrategy {

    /**
     * Loan charges api resource {@link LoanChargesApiResource}.
     */
    private final LoanChargesApiResource loanChargesApiResource;

    @Override
    public BatchResponse execute(BatchRequest request, @SuppressWarnings("unused") final UriInfo uriInfo) {

        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final String relativeUrl = relativeUrlWithoutVersion(request);

        // Expected pattern - loans\/external-id\/[\w\d_-]+\/charges
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrl);
        final String loanExternalIdPathParameter = pathParameters.get(2);
        String loanExternalId;
        if (loanExternalIdPathParameter.contains("?")) {
            loanExternalId = pathParameters.get(2).substring(0, pathParameters.get(2).indexOf("?"));
        } else {
            loanExternalId = pathParameters.get(2);
        }

        final Map<String, String> queryParameters = CommandStrategyUtils.getQueryParameters(relativeUrl);
        final String command = queryParameters.get("command");

        // Calls 'executeLoanCharge' function from 'LoanChargesApiResource'
        // to create a new charge for based on loan external id
        responseBody = loanChargesApiResource.executeLoanCharge(loanExternalId, command, request.getBody());

        response.setStatusCode(HttpStatus.SC_OK);
        // Sets the body of the response after Charge has been successfully
        // created
        response.setBody(responseBody);

        return response;
    }
}
