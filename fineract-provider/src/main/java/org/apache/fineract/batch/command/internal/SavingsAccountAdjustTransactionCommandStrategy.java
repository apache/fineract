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
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.command.CommandStrategyUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.portfolio.savings.api.SavingsAccountTransactionsApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SavingsAccountAdjustTransactionCommandStrategy implements CommandStrategy {

    private final SavingsAccountTransactionsApiResource savingsAccountTransactionsApiResource;

    @Override
    public BatchResponse execute(BatchRequest batchRequest, UriInfo uriInfo) {
        String relativeUrl = relativeUrlWithoutVersion(batchRequest);
        final List<String> pathParameters;
        String command = null;
        if (relativeUrl.indexOf('?') > 0) {
            String subString = StringUtils.substringBefore(relativeUrl, "?");
            pathParameters = Splitter.on('/').splitToList(subString);
            final Map<String, String> queryParameters = CommandStrategyUtils.getQueryParameters(relativeUrl);
            command = queryParameters.get("command");
        } else {
            pathParameters = Splitter.on('/').splitToList(relativeUrl);
        }

        Long savingsAccountId = Long.parseLong(pathParameters.get(1));
        Long transactionId = Long.parseLong(pathParameters.get(3));
        final String responseBody = savingsAccountTransactionsApiResource.adjustTransaction(savingsAccountId, transactionId, command,
                batchRequest.getBody());

        return new BatchResponse().setRequestId(batchRequest.getRequestId()).setStatusCode(HttpStatus.SC_OK).setBody(responseBody)
                .setHeaders(batchRequest.getHeaders());
    }
}
