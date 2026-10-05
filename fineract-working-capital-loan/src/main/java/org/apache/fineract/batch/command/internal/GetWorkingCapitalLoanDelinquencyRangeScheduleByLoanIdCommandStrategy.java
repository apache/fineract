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
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.workingcapitalloan.api.WorkingCapitalLoanDelinquencyRangeScheduleApiResource;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link CommandStrategy} and retrieves the delinquency range schedule for a Working Capital loan by loan
 * id. It passes the contents of the body from the BatchRequest to
 * {@link WorkingCapitalLoanDelinquencyRangeScheduleApiResource} and gets back the response. This allows a caller to
 * fetch the breach schedule and the delinquency range schedule for the same loan in a single {@code POST /batches} call
 * instead of two separate GET calls.
 */
@Component
@RequiredArgsConstructor
public class GetWorkingCapitalLoanDelinquencyRangeScheduleByLoanIdCommandStrategy implements CommandStrategy {

    private final WorkingCapitalLoanDelinquencyRangeScheduleApiResource delinquencyRangeScheduleApiResource;

    private final DefaultToApiJsonSerializer<List<WorkingCapitalLoanDelinquencyRangeScheduleData>> toApiJsonSerializer;

    @Override
    public BatchResponse execute(final BatchRequest request, @SuppressWarnings("unused") final UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        // Expected pattern - working-capital-loans\/\d+\/delinquency-range-schedule
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrlWithoutVersion(request));
        final Long loanId = Long.parseLong(pathParameters.get(1));

        final List<WorkingCapitalLoanDelinquencyRangeScheduleData> delinquencyRangeScheduleData = delinquencyRangeScheduleApiResource
                .retrieveDelinquencyRangeSchedule(loanId);

        response.setStatusCode(HttpStatus.SC_OK);
        response.setBody(toApiJsonSerializer.serialize(delinquencyRangeScheduleData));

        return response;
    }
}
