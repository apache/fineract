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
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.http.HttpStatus;

/**
 * Base for batching a breach, delinquency or near breach action on a working capital loan.
 *
 * <p>
 * The action itself travels in the request body, not as a query parameter, so the sub-request carries nothing to read
 * beyond the loan the URL names. In both {@code working-capital-loans/{loanId}/<actions>} and
 * {@code working-capital-loans/external-id/{loanExternalId}/<actions>} that loan is the second-to-last path segment.
 */
@RequiredArgsConstructor
abstract class WorkingCapitalLoanActionCommandStrategy implements CommandStrategy {

    private final DefaultToApiJsonSerializer<CommandProcessingResult> toApiJsonSerializer;

    @Override
    public BatchResponse execute(final BatchRequest request, @SuppressWarnings("unused") final UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final String relativeUrl = relativeUrlWithoutVersion(request);
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrl);
        final String loanIdentifier = pathParameters.get(pathParameters.size() - 2);

        final CommandProcessingResult commandProcessingResult = createAction(loanIdentifier, request.getBody());

        response.setStatusCode(HttpStatus.SC_OK);
        response.setBody(toApiJsonSerializer.serialize(commandProcessingResult));

        return response;
    }

    /**
     * Creates the action on the loan the URL names.
     *
     * @param loanIdentifier
     *            the loan id or loan external id, as it appears in the URL
     * @param apiRequestBodyAsJson
     *            the sub-request body naming the action
     */
    protected abstract CommandProcessingResult createAction(String loanIdentifier, String apiRequestBodyAsJson);
}
