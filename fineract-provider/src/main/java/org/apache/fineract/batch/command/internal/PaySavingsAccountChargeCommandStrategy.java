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
import org.apache.fineract.portfolio.savings.api.SavingsAccountChargesApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link CommandStrategy} to pay (or waive) a savings account charge via the Batch API. It passes the
 * contents of the body from the BatchRequest to {@link SavingsAccountChargesApiResource} and gets back the response.
 *
 * <p>
 * Handles URLs of the form: {@code POST
 * savingsaccounts/{savingsAccountId}/charges/{savingsAccountChargeId}?command=paycharge}
 * </p>
 *
 * @see CommandStrategy
 * @see BatchRequest
 * @see BatchResponse
 */
@Component
@RequiredArgsConstructor
public class PaySavingsAccountChargeCommandStrategy implements CommandStrategy {

    private final SavingsAccountChargesApiResource savingsAccountChargesApiResource;

    @Override
    public BatchResponse execute(final BatchRequest request, @SuppressWarnings("unused") final UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final String relativeUrl = relativeUrlWithoutVersion(request);

        // URL: savingsaccounts/{savingsAccountId}/charges/{savingsAccountChargeId}?command=paycharge
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrl);

        if (pathParameters.size() < 4) {
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
            response.setBody(
                    "Resource with method " + request.getMethod() + " and relativeUrl " + request.getRelativeUrl() + " doesn't exist");
            return response;
        }

        final Long savingsAccountId = Long.parseLong(pathParameters.get(1));

        // The charge id may have the query string appended: "47?command=paycharge"
        final String chargeIdSegment = pathParameters.get(3);
        final Long savingsAccountChargeId;
        final String command;

        if (chargeIdSegment.contains("?")) {
            savingsAccountChargeId = Long.parseLong(chargeIdSegment.substring(0, chargeIdSegment.indexOf("?")));
            final Map<String, String> queryParameters = CommandStrategyUtils.getQueryParameters(relativeUrl);
            command = queryParameters.get("command");
        } else {
            response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
            response.setBody(
                    "Resource with method " + request.getMethod() + " and relativeUrl " + request.getRelativeUrl() + " doesn't exist");
            return response;
        }

        final String responseBody = savingsAccountChargesApiResource.payOrWaiveSavingsAccountCharge(savingsAccountId,
                savingsAccountChargeId, command, request.getBody());

        response.setStatusCode(HttpStatus.SC_OK);
        response.setBody(responseBody);

        return response;
    }
}
