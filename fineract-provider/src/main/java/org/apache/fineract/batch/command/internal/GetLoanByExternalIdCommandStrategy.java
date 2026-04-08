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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.api.MutableUriInfo;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link CommandStrategy} and get loan by external id. It passes the contents of the body from the
 * BatchRequest to {@link LoansApiResource} and gets back the response. This class will also catch any errors raised by
 * {@link LoansApiResource} and map those errors to appropriate status codes in BatchResponse.
 */
@Component
@RequiredArgsConstructor
public class GetLoanByExternalIdCommandStrategy implements CommandStrategy {

    /**
     * Loans api resource {@link LoansApiResource}.
     */
    private final LoansApiResource loansApiResource;

    @Override
    public BatchResponse execute(final BatchRequest request, final UriInfo uriInfo) {
        final MutableUriInfo parameterizedUriInfo = new MutableUriInfo(uriInfo);

        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        // Expected pattern - loans\/external-id\/[\w\d_-]+\?associations=transactions
        final String relativeUrl = relativeUrlWithoutVersion(request);
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrl);
        final String loanExternalIdPathParameter = pathParameters.get(2);

        String loanExternalId;

        // uriInfo will contain the query parameter value(s) that are sent in the actual batch uri.
        // for example: batches?enclosingTransaction=true
        // But the query parameters that are sent in the batch relative url has to be sent to
        // LoansApiResource.retrieveLoan
        // To use the relative url query parameters
        // - Parse and fetch the query parameters sent in the relative url
        // (loans/external-id/ff62fc65-1bba-4bb0-b090-5f9ecf0a66f1?fields=id,principal,annualInterestRate)
        // - Add them to the UriInfo query parameters list
        // - Call loansApiResource.retrieveLoan(loanExternalId, false, uriInfo)
        // - Remove the relative url query parameters from UriInfo in the finally (after loan details are retrieved)
        Map<String, String> queryParameters = null;
        if (loanExternalIdPathParameter.indexOf('?') > 0) {
            loanExternalId = StringUtils.substringBefore(loanExternalIdPathParameter, "?");
            queryParameters = getQueryParameters(relativeUrl);

            // Add the query parameters sent in the relative URL to UriInfo
            addQueryParametersToUriInfo(parameterizedUriInfo, queryParameters);
        } else {
            loanExternalId = loanExternalIdPathParameter;
        }

        // Calls 'retrieveLoan' function from 'LoansApiResource' to
        // get the loan details based on the loan external id
        boolean staffInSelectedOfficeOnly = false;
        String associations = null;
        String exclude = null;
        String fields = null;
        if (queryParameters != null && queryParameters.size() > 0) {
            if (queryParameters.containsKey("associations")) {
                associations = queryParameters.get("associations");
            }
            if (queryParameters.containsKey("exclude")) {
                exclude = queryParameters.get("exclude");
            }
            if (queryParameters.containsKey("fields")) {
                fields = queryParameters.get("fields");
            }
            if (queryParameters.containsKey("staffInSelectedOfficeOnly")) {
                staffInSelectedOfficeOnly = BooleanUtils.toBoolean(queryParameters.get("staffInSelectedOfficeOnly"));
            }
        }

        responseBody = loansApiResource.retrieveLoan(loanExternalId, staffInSelectedOfficeOnly, associations, exclude, fields,
                parameterizedUriInfo);

        response.setStatusCode(HttpStatus.SC_OK);

        // Sets the response after retrieving the loan
        response.setBody(responseBody);

        return response;
    }

    /**
     * Get query parameters from relative URL.
     *
     * @param relativeUrl
     *            the relative URL
     * @return the query parameters in a map
     */
    private Map<String, String> getQueryParameters(final String relativeUrl) {
        final String queryParameterStr = StringUtils.substringAfter(relativeUrl, "?");
        final String[] queryParametersArray = StringUtils.split(queryParameterStr, "&");
        final Map<String, String> queryParametersMap = new HashMap<>();
        for (String parameterStr : queryParametersArray) {
            String[] keyValue = StringUtils.split(parameterStr, "=");
            queryParametersMap.put(keyValue[0], keyValue[1]);
        }
        return queryParametersMap;
    }

    /**
     * Add query parameters(received in the relative URL) to URI info query parameters.
     *
     * @param uriInfo
     *            the URI info
     * @param queryParameters
     *            the query parameters
     */
    private void addQueryParametersToUriInfo(final MutableUriInfo uriInfo, final Map<String, String> queryParameters) {
        for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
            uriInfo.addAdditionalQueryParameter(entry.getKey(), entry.getValue());
        }
    }
}
