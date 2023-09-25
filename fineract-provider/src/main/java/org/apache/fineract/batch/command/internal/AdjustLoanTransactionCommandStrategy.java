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
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link CommandStrategy} to adjust a transaction. It passes the contents of the body from the BatchRequest
 * to {@link LoanTransactionsApiResource} and gets back the response. This class will also catch any errors raised by
 * {@link LoanTransactionsApiResource} and map those errors to appropriate status codes in BatchResponse.
 *
 * @see CommandStrategy
 * @see BatchRequest
 * @see BatchResponse
 */
@Component
@RequiredArgsConstructor
public class AdjustLoanTransactionCommandStrategy implements CommandStrategy {

    /**
     * Loan transactions api resource {@link LoanTransactionsApiResource}.
     */
    private final LoanTransactionsApiResource loanTransactionsApiResource;

    @Override
    public BatchResponse execute(final BatchRequest request, final UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final String relativeUrl = relativeUrlWithoutVersion(request);

        // Get the loan and transaction ids for use in loanTransactionsApiResource
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrl);
        final Long loanId = Long.parseLong(pathParameters.get(1));

        final String transactionIdPathParameter = pathParameters.get(3);
        Long transactionId;
        if (transactionIdPathParameter.contains("?")) {
            transactionId = Long.parseLong(pathParameters.get(3).substring(0, pathParameters.get(3).indexOf("?")));
        } else {
            transactionId = Long.parseLong(pathParameters.get(3));
        }

        final Map<String, String> queryParameters = CommandStrategyUtils.getQueryParameters(relativeUrl);
        final String command = queryParameters.get("command");

        // Calls 'adjustLoanTransaction' function from 'loanTransactionsApiResource'
        responseBody = loanTransactionsApiResource.adjustLoanTransaction(loanId, transactionId, request.getBody(), command);

        response.setStatusCode(HttpStatus.SC_OK);

        // Sets the body of the response after retrieving the transaction
        response.setBody(responseBody);

        return response;
    }
}
