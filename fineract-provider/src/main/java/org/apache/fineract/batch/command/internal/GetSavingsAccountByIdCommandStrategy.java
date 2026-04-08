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

import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.command.CommandStrategyUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.MutableUriInfo;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.savings.api.SavingsAccountsApiResource;
import org.apache.fineract.portfolio.savings.api.SavingsApiSetConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSavingsAccountByIdCommandStrategy implements CommandStrategy {

    private final SavingsAccountsApiResource savingsAccountsApiResource;
    private final DefaultToApiJsonSerializer<SavingsAccountData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Override
    public BatchResponse execute(BatchRequest batchRequest, UriInfo uriInfo) {
        final MutableUriInfo parameterizedUriInfo = new MutableUriInfo(uriInfo);
        final String relativeUrl = relativeUrlWithoutVersion(batchRequest);

        final long savingsAccountId;
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
        String associations = null;
        if (!queryParameters.isEmpty()) {
            if (queryParameters.containsKey("staffInSelectedOfficeOnly")) {
                staffInSelectedOfficeOnly = queryParameters.get("staffInSelectedOfficeOnly");
            }
            if (queryParameters.containsKey("chargeStatus")) {
                chargeStatus = queryParameters.get("chargeStatus");
            }
            if (queryParameters.containsKey("associations")) {
                associations = queryParameters.get("associations");
            }
        }

        SavingsAccountData savingsAccountData = savingsAccountsApiResource.retrieveOne(savingsAccountId,
                Boolean.parseBoolean(staffInSelectedOfficeOnly), chargeStatus, associations, uriInfo);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final String responseBody = toApiJsonSerializer.serialize(settings, savingsAccountData,
                SavingsApiSetConstants.SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS);

        return new BatchResponse().setRequestId(batchRequest.getRequestId()).setStatusCode(HttpStatus.SC_OK).setBody(responseBody)
                .setHeaders(batchRequest.getHeaders());
    }
}
