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
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link CommandStrategy} to handle approval of a pending loan by its external id. It passes the contents of
 * the body from the BatchRequest to {@link LoansApiResource} and gets back the response. This class will also catch any
 * errors raised by {@link LoansApiResource} and map those errors to appropriate status codes in BatchResponse.
 *
 * @see CommandStrategy
 * @see BatchRequest
 * @see BatchResponse
 */
@Component
@RequiredArgsConstructor
public class LoanStateTransistionsByExternalIdCommandStrategy implements CommandStrategy {

    /**
     * Loans api resource {@link LoansApiResource}.
     */
    private final LoansApiResource loansApiResource;

    @Override
    public BatchResponse execute(final BatchRequest request, @SuppressWarnings("unused") final UriInfo uriInfo) {

        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        // Expected pattern - loans\/external-id\/[\w\d_-]+\?command=***
        final String relativeUrl = relativeUrlWithoutVersion(request);
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrl);
        final String loanExternalIdPathParameter = pathParameters.get(2);

        final Pattern commandPattern = Pattern.compile("^?command=[a-zA-Z]+");
        final Matcher commandMatcher = commandPattern.matcher(loanExternalIdPathParameter);

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

        final String loanExternalId = StringUtils.substringBefore(loanExternalIdPathParameter, "?");

        // Calls 'approve'/'disburse' function from 'LoansApiResource' to approve/disburse loan
        responseBody = loansApiResource.stateTransitions(loanExternalId, command, request.getBody());

        response.setStatusCode(HttpStatus.SC_OK);
        // Sets the body of the response after the successful approval of a loan
        response.setBody(responseBody);

        return response;
    }

}
