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
import org.apache.fineract.portfolio.interestpauses.api.LoanInterestPauseApiResource;
import org.apache.fineract.portfolio.interestpauses.data.InterestPauseRequestDto;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link CommandStrategy} and updates a loan interest pause by external id. It passes the contents of the
 * body from the BatchRequest to {@link LoanInterestPauseApiResource} and gets back the response. This class will also
 * catch any errors raised by {@link LoanInterestPauseApiResource} and map those errors to appropriate status codes in
 * BatchResponse.
 */
@Component
@RequiredArgsConstructor
public class UpdateLoanInterestPauseByExternalIdCommandStrategy implements CommandStrategy {

    private final LoanInterestPauseApiResource loanInterestPauseApiResource;

    private final DefaultToApiJsonSerializer<CommandProcessingResult> toApiJsonSerializer;

    @Override
    public BatchResponse execute(final BatchRequest request, @SuppressWarnings("unused") final UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        // Expected pattern - loans\/external-id\/[\w\d_-]+\/interest-pauses\/\d+
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrlWithoutVersion(request));
        final String loanExternalId = pathParameters.get(2);
        final Long variationId = Long.parseLong(pathParameters.get(4));

        final InterestPauseRequestDto interestPauseRequestDto = InterestPauseRequestDto.fromJson(request.getBody());
        final CommandProcessingResult commandProcessingResult = loanInterestPauseApiResource.updateInterestPauseByExternalId(loanExternalId,
                variationId, interestPauseRequestDto);

        response.setStatusCode(HttpStatus.SC_OK);
        response.setBody(toApiJsonSerializer.serialize(commandProcessingResult));

        return response;
    }
}
