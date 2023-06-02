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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link CommandStrategy} and handles the creation of a transaction for a Loan using External Id. It passes
 * the contents of the body from the BatchRequest to {@link LoanTransactionsApiResource} and gets back the response.
 * This class will also catch any errors raised by {@link LoanTransactionsApiResource} and map those errors to
 * appropriate status codes in BatchResponse.
 *
 * @see CommandStrategy
 * @see BatchRequest
 * @see BatchResponse
 */
@Component
@RequiredArgsConstructor
public class CreateTransactionByLoanExternalIdCommandStrategy implements CommandStrategy {

    /**
     * Loan transactions api resource {@link LoanTransactionsApiResource}.
     */
    private final LoanTransactionsApiResource loanTransactionsApiResource;

    @Override
    public BatchResponse execute(BatchRequest request, @SuppressWarnings("unused") UriInfo uriInfo) {

        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        // Expected Pattern - loans\/external-id\/[a-zA-Z0-9_-]*\/transactions\?command=[\w]+
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrlWithoutVersion(request));
        final String loanExternalId = pathParameters.get(2);

        final Pattern commandPattern = Pattern.compile("^?command=[a-zA-Z]+");
        final Matcher commandMatcher = commandPattern.matcher(pathParameters.get(3));

        if (!commandMatcher.find()) {
            // This would only occur if the CommandStrategyProvider is incorrectly configured.
            response.setRequestId(request.getRequestId());
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
            response.setBody(
                    "Resource with method " + request.getMethod() + " and relativeUrl " + request.getRelativeUrl() + " doesn't exist");
            return response;
        }
        final String commandQueryParam = commandMatcher.group(0);
        final String command = commandQueryParam.substring(commandQueryParam.indexOf("=") + 1);

        responseBody = loanTransactionsApiResource.executeLoanTransaction(loanExternalId, command, request.getBody());

        response.setStatusCode(HttpStatus.SC_OK);
        // Sets the body of the response after Charge has been successfully
        // created
        response.setBody(responseBody);

        return response;
    }
}
