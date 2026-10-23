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
import org.apache.fineract.batch.command.CommandStrategyUtils;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.workingcapitalloan.api.WorkingCapitalLoanTransactionsApiResource;
import org.apache.http.HttpStatus;

/**
 * Base for batching a {@code ?command=} on a working capital loan transaction.
 *
 * <p>
 * The four URL shapes differ only in whether the loan and the transaction are named by id or by external id, so the
 * subclass reads the two identifiers from the path segments and picks the matching resource method; the command, the
 * body and the response handling are shared.
 */
@RequiredArgsConstructor
abstract class WorkingCapitalLoanTransactionCommandStrategy implements CommandStrategy {

    protected final WorkingCapitalLoanTransactionsApiResource workingCapitalLoanTransactionsApiResource;
    private final DefaultToApiJsonSerializer<CommandProcessingResult> toApiJsonSerializer;

    @Override
    public BatchResponse execute(final BatchRequest request, @SuppressWarnings("unused") final UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final String relativeUrl = relativeUrlWithoutVersion(request);
        final String command = CommandStrategyUtils.getQueryParameters(relativeUrl).get("command");
        final String pathPart = Splitter.on('?').splitToList(relativeUrl).getFirst();
        final List<String> pathParameters = Splitter.on('/').splitToList(pathPart);

        final CommandProcessingResult commandProcessingResult = executeCommand(pathParameters, command, request.getBody());

        response.setStatusCode(HttpStatus.SC_OK);
        response.setBody(toApiJsonSerializer.serialize(commandProcessingResult));

        return response;
    }

    /**
     * Executes the command on the transaction the URL names.
     *
     * @param pathParameters
     *            the path segments of the URL, starting at {@code working-capital-loans}
     * @param command
     *            the {@code command} query parameter
     * @param apiRequestBodyAsJson
     *            the sub-request body
     */
    protected abstract CommandProcessingResult executeCommand(List<String> pathParameters, String command, String apiRequestBodyAsJson);
}
