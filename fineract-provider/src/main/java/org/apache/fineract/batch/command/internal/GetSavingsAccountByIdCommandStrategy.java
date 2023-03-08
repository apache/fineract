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

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.command.CommandStrategyUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.api.MutableUriInfo;
import org.apache.fineract.portfolio.savings.api.SavingsAccountsApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSavingsAccountByIdCommandStrategy implements CommandStrategy {

    private final SavingsAccountsApiResource savingsAccountsApiResource;

    @Override
    public BatchResponse execute(BatchRequest batchRequest, UriInfo uriInfo) {
        final MutableUriInfo parameterizedUriInfo = new MutableUriInfo(uriInfo);
        final BatchResponse response = new BatchResponse();

        response.setRequestId(batchRequest.getRequestId());
        response.setHeaders(batchRequest.getHeaders());

        final String relativeUrl = batchRequest.getRelativeUrl();

        final Long savingsAccountId;
        Map<String, String> queryParameters = new HashMap<>();
        if (relativeUrl.indexOf('?') > 0) {
            savingsAccountId = Long.parseLong(StringUtils.substringBetween(relativeUrl, "/", "?"));
            queryParameters = CommandStrategyUtils.getQueryParameters(relativeUrl);
            CommandStrategyUtils.addQueryParametersToUriInfo(parameterizedUriInfo, queryParameters);
        } else {
            savingsAccountId = Long.parseLong(StringUtils.substringAfter(relativeUrl, "/"));
        }

        String staffInSelectedOfficeOnly = null;
        String chargeStatus = null;
        if (!queryParameters.isEmpty()) {
            if (queryParameters.containsKey("staffInSelectedOfficeOnly")) {
                staffInSelectedOfficeOnly = queryParameters.get("staffInSelectedOfficeOnly");
            }
            if (queryParameters.containsKey("chargeStatus")) {
                chargeStatus = queryParameters.get("chargeStatus");
            }
        }

        final String responseBody = savingsAccountsApiResource.retrieveOne(savingsAccountId,
                Boolean.parseBoolean(staffInSelectedOfficeOnly), chargeStatus, uriInfo);

        response.setStatusCode(HttpStatus.SC_OK);

        response.setBody(responseBody);

        return response;
    }
}
