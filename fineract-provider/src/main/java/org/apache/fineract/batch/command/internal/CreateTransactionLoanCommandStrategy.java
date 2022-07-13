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

import com.google.common.base.Splitter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link CommandStrategy} and handles the creation of a transaction for a Loan. It passes the contents of
 * the body from the BatchRequest to {@link LoanTransactionsApiResource} and gets back the response. This class will
 * also catch any errors raised by {@link LoanTransactionsApiResource} and map those errors to appropriate status codes
 * in BatchResponse.
 *
 * @author Mohit Sinha
 *
 * @see CommandStrategy
 * @see BatchRequest
 * @see BatchResponse
 */
@Component
@RequiredArgsConstructor
public class CreateTransactionLoanCommandStrategy implements CommandStrategy {

    private final LoanTransactionsApiResource loanTransactionsApiResource;

    @Override
    public BatchResponse execute(BatchRequest request, @SuppressWarnings("unused") UriInfo uriInfo) {

        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final List<String> pathParameters = Splitter.on('/').splitToList(request.getRelativeUrl());
        Long loanId = Long.parseLong(pathParameters.get(1));

        Pattern commandPattern = Pattern.compile("^?command=[a-zA-Z]+");
        Matcher commandMatcher = commandPattern.matcher(pathParameters.get(2));

        if (!commandMatcher.find()) {
            // This would only occur if the CommandStrategyProvider is incorrectly configured.
            response.setRequestId(request.getRequestId());
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
            response.setBody(
                    "Resource with method " + request.getMethod() + " and relativeUrl " + request.getRelativeUrl() + " doesn't exist");
            return response;
        }
        String commandQueryParam = commandMatcher.group(0);
        String command = commandQueryParam.substring(commandQueryParam.indexOf("=") + 1);

        responseBody = loanTransactionsApiResource.executeLoanTransaction(loanId, command, request.getBody());

        response.setStatusCode(200);
        // Sets the body of the response after Charge has been successfully
        // created
        response.setBody(responseBody);

        return response;
    }
}
