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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link org.apache.fineract.batch.command.CommandStrategy} to handle disburse of a loan. It passes the
 * contents of the body from the BatchRequest to {@link org.apache.fineract.portfolio.loanaccount.api.LoansApiResource}
 * and gets back the response. This class will also catch any errors raised by
 * {@link org.apache.fineract.portfolio.loanaccount.api.LoansApiResource} and map those errors to appropriate status
 * codes in BatchResponse.
 *
 * @author Rishabh Shukla
 *
 * @see org.apache.fineract.batch.command.CommandStrategy
 * @see org.apache.fineract.batch.domain.BatchRequest
 * @see org.apache.fineract.batch.domain.BatchResponse
 */
@Component
@RequiredArgsConstructor
public class DisburseLoanCommandStrategy implements CommandStrategy {

    private final LoansApiResource loansApiResource;

    @Override
    public BatchResponse execute(final BatchRequest request, @SuppressWarnings("unused") UriInfo uriInfo) {

        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrlWithoutVersion(request));
        final Long loanId = Long.parseLong(pathParameters.get(1).substring(0, pathParameters.get(1).indexOf("?")));

        // Calls 'disburse' function from 'LoansApiResource' to disburse a
        // loan
        responseBody = loansApiResource.stateTransitions(loanId, "disburse", request.getBody());

        response.setStatusCode(HttpStatus.SC_OK);

        // Sets the body of the response after the successful disbursal of
        // the loan
        response.setBody(responseBody);

        return response;
    }

}
