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
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.workingcapitalloan.api.WorkingCapitalLoanTransactionsApiResource;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetWorkingCapitalLoanTransactionByLoanIdAndExternalTransactionIdCommandStrategy implements CommandStrategy {

    private final WorkingCapitalLoanTransactionsApiResource workingCapitalLoanTransactionsApiResource;
    private final DefaultToApiJsonSerializer<CommandProcessingResult> toApiJsonSerializer;

    @Override
    public BatchResponse execute(BatchRequest request, @SuppressWarnings("unused") UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final String relativeUrl = relativeUrlWithoutVersion(request);
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrl);
        final Long loanId = Long.parseLong(pathParameters.get(1));
        String transactionExternalId;
        if (relativeUrl.indexOf('?') > 0) {
            transactionExternalId = StringUtils.substringBeforeLast(pathParameters.get(4), "?");
        } else {
            transactionExternalId = pathParameters.get(4);
        }

        final WorkingCapitalLoanTransactionData workingCapitalLoanTransactionData = workingCapitalLoanTransactionsApiResource
                .retrieveTransactionByLoanIdAndTransactionExternalId(loanId, transactionExternalId);

        response.setStatusCode(HttpStatus.SC_OK);
        response.setBody(toApiJsonSerializer.serialize(workingCapitalLoanTransactionData));

        return response;
    }
}
