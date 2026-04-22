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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.command.CommandStrategyUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.fineract.portfolio.loanaccount.api.request.ReAgePreviewRequest;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link CommandStrategy} to calculate the reage preview for the given term for a particular loan by its
 * loan id. It passes the contents of the body from the BatchRequest to {@link LoanTransactionsApiResource} and gets
 * back the response. This class will also catch any errors raised by {@link LoanTransactionsApiResource} and map those
 * errors to appropriate status codes in BatchResponse.
 *
 * @see CommandStrategy
 * @see BatchRequest
 * @see BatchResponse
 */
@Component
@RequiredArgsConstructor
public class GetReagePreviewByLoanIdCommandStrategy implements CommandStrategy {

    /**
     * Loan transactions api resource {@link LoanTransactionsApiResource}.
     */
    private final LoanTransactionsApiResource loanTransactionsApiResource;

    /**
     * The toApiJsonSerializer to convert json to object
     */
    private final DefaultToApiJsonSerializer<LoanScheduleData> toApiJsonSerializer;

    @Override
    public BatchResponse execute(final BatchRequest request, final UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final String relativeUrl = relativeUrlWithoutVersion(request);

        // Expected pattern - loans/" + NUMBER + "/transactions/reage-preview?queryParam1=<blah>&queryParam2=<blah>&....
        // Get the loan external id
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrl);
        final Long loanId = Long.parseLong(pathParameters.get(1));
        Map<String, String> queryParameters = new HashMap<>();
        if (relativeUrl.indexOf('?') > 0) {
            queryParameters = CommandStrategyUtils.getQueryParameters(relativeUrl);
        }

        // Build ReAgePreviewRequest from query parameters using generic utility
        final ReAgePreviewRequest reAgePreviewRequest = CommandStrategyUtils.buildRequestFromQueryParameters(queryParameters,
                ReAgePreviewRequest.class);

        // Calls 'previewReAgeSchedule' function from 'loanTransactionsApiResource' using external id
        response.setStatusCode(HttpStatus.SC_OK);
        // Sets the body of the response after getting reage preview
        response.setBody(toApiJsonSerializer.serialize(loanTransactionsApiResource.previewReAgeSchedule(loanId, reAgePreviewRequest)));

        return response;
    }
}
