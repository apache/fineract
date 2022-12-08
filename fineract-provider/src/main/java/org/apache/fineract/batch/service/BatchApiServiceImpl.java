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
package org.apache.fineract.batch.service;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.batch.command.CommandContext;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.command.CommandStrategyProvider;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.batch.domain.Header;
import org.apache.fineract.batch.exception.ClientDetailsNotFoundException;
import org.apache.fineract.batch.exception.ErrorHandler;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.batch.service.ResolutionHelper.BatchRequestNode;
import org.apache.fineract.infrastructure.core.domain.BatchRequestContextHolder;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.AbstractIdempotentCommandException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessFailedException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessSucceedException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessUnderProcessingException;
import org.apache.fineract.infrastructure.core.filters.BatchCallHandler;
import org.apache.fineract.infrastructure.core.filters.BatchFilter;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Implementation for {@link BatchApiService} to iterate through all the incoming requests and obtain the appropriate
 * CommandStrategy from CommandStrategyProvider.
 *
 * @author Rishabh Shukla
 * @see org.apache.fineract.batch.domain.BatchRequest
 * @see org.apache.fineract.batch.domain.BatchResponse
 * @see org.apache.fineract.batch.command.CommandStrategyProvider
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BatchApiServiceImpl implements BatchApiService {

    private final CommandStrategyProvider strategyProvider;
    private final ResolutionHelper resolutionHelper;
    private final TransactionTemplate transactionTemplate;

    private final List<BatchFilter> batchFilters;

    private final FineractRequestContextHolder fineractRequestContextHolder;

    /**
     * Returns the response list by getting a proper {@link org.apache.fineract.batch.command.CommandStrategy}.
     * execute() method of acquired commandStrategy is then provided with the separate Request.
     *
     * @param requestList
     * @param uriInfo
     * @return {@code List<BatchResponse>}
     */
    private List<BatchResponse> handleBatchRequests(final List<BatchRequest> requestList, final UriInfo uriInfo) {

        final List<BatchResponse> responseList = new ArrayList<>(requestList.size());

        final List<BatchRequestNode> batchRequestNodes = this.resolutionHelper.getDependingRequests(requestList);
        if (batchRequestNodes.isEmpty()) {
            final BatchResponse response = new BatchResponse();
            ErrorInfo ex = ErrorHandler.handler(new ClientDetailsNotFoundException());
            response.setStatusCode(500);
            response.setBody(ex.getMessage());
            responseList.add(response);
            return responseList;
        }

        for (BatchRequestNode rootNode : batchRequestNodes) {
            final BatchRequest rootRequest = rootNode.getRequest();
            final CommandStrategy commandStrategy = this.strategyProvider
                    .getCommandStrategy(CommandContext.resource(rootRequest.getRelativeUrl()).method(rootRequest.getMethod()).build());
            final BatchResponse rootResponse = safelyExecuteStrategy(commandStrategy, rootRequest, uriInfo);

            responseList.add(rootResponse);
            responseList.addAll(this.processChildRequests(rootNode, rootResponse, uriInfo));
        }

        Collections.sort(responseList, Comparator.comparing(BatchResponse::getRequestId));

        return responseList;

    }

    private BatchResponse safelyExecuteStrategy(CommandStrategy commandStrategy, BatchRequest request, UriInfo originalUriInfo) {
        try {
            BatchRequestContextHolder.setRequestAttributes(new HashMap<>(Optional.ofNullable(request.getHeaders())
                    .map(list -> list.stream().collect(Collectors.toMap(Header::getName, Header::getValue)))
                    .orElse(Collections.emptyMap())));

            return new BatchCallHandler(this.batchFilters, commandStrategy::execute).serviceCall(request, originalUriInfo);
        } catch (AbstractIdempotentCommandException idempotentException) {
            return handleIdempotentRequests(idempotentException, request);
        } catch (RuntimeException e) {
            log.warn("Exception while executing batch strategy {}", commandStrategy.getClass().getSimpleName(), e);

            ErrorInfo ex = ErrorHandler.handler(e);

            final BatchResponse response = new BatchResponse();
            response.setRequestId(request.getRequestId());
            response.setHeaders(request.getHeaders());
            response.setStatusCode(ex.getStatusCode());
            response.setBody(ex.getMessage());
            return response;
        } finally {
            BatchRequestContextHolder.resetRequestAttributes();
        }
    }

    private BatchResponse handleIdempotentRequests(AbstractIdempotentCommandException idempotentException, BatchRequest request) {
        BatchResponse response = new BatchResponse();
        response.setRequestId(request.getRequestId());
        response.setHeaders(Optional.ofNullable(request.getHeaders()).orElse(new HashSet<>()));
        response.getHeaders().add(new Header(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER, "true"));
        response.setBody(idempotentException.getResponse());
        if (idempotentException instanceof IdempotentCommandProcessSucceedException) {
            response.setStatusCode(200);
        } else if (idempotentException instanceof IdempotentCommandProcessUnderProcessingException) {
            response.setStatusCode(409);
        } else if (idempotentException instanceof IdempotentCommandProcessFailedException) {
            response.setStatusCode(((IdempotentCommandProcessFailedException) idempotentException).getStatusCode());
        } else {
            response.setStatusCode(500);
        }
        return response;
    }

    private List<BatchResponse> processChildRequests(final BatchRequestNode rootRequest, BatchResponse rootResponse, UriInfo uriInfo) {

        final List<BatchResponse> childResponses = new ArrayList<>();
        if (!rootRequest.getChildRequests().isEmpty()) {

            for (BatchRequestNode childNode : rootRequest.getChildRequests()) {

                BatchRequest childRequest = childNode.getRequest();
                BatchResponse childResponse;

                try {

                    if (rootResponse.getStatusCode().equals(200)) {
                        childRequest = this.resolutionHelper.resoluteRequest(childRequest, rootResponse);
                        final CommandStrategy commandStrategy = this.strategyProvider.getCommandStrategy(
                                CommandContext.resource(childRequest.getRelativeUrl()).method(childRequest.getMethod()).build());

                        childResponse = safelyExecuteStrategy(commandStrategy, childRequest, uriInfo);

                    } else {
                        // Something went wrong with the parent request, create
                        // a response with status code 409
                        childResponse = new BatchResponse();
                        childResponse.setRequestId(childRequest.getRequestId());
                        childResponse.setStatusCode(Status.CONFLICT.getStatusCode());

                        // Some detail information about the error
                        final ErrorInfo conflictError = new ErrorInfo(Status.CONFLICT.getStatusCode(), 8001,
                                "Parent request with id " + rootResponse.getRequestId() + " was erroneous!");
                        childResponse.setBody(conflictError.getMessage());
                    }
                    childResponses.addAll(this.processChildRequests(childNode, childResponse, uriInfo));

                } catch (Throwable ex) {

                    childResponse = new BatchResponse();
                    childResponse.setRequestId(childRequest.getRequestId());
                    childResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    childResponse.setBody(ex.getMessage());
                }

                childResponses.add(childResponse);
            }
        }

        return childResponses;
    }

    @Override
    public List<BatchResponse> handleBatchRequestsWithoutEnclosingTransaction(final List<BatchRequest> requestList, UriInfo uriInfo) {

        return handleBatchRequests(requestList, uriInfo);
    }

    @Override
    public List<BatchResponse> handleBatchRequestsWithEnclosingTransaction(final List<BatchRequest> requestList, final UriInfo uriInfo) {
        List<BatchResponse> responseList = new ArrayList<>();
        try {
            return this.transactionTemplate.execute(status -> {
                try {
                    responseList.addAll(handleBatchRequests(requestList, uriInfo));
                    return responseList;
                } catch (RuntimeException ex) {

                    ErrorInfo e = ErrorHandler.handler(ex);
                    BatchResponse errResponse = new BatchResponse();
                    errResponse.setStatusCode(e.getStatusCode());
                    errResponse.setBody(e.getMessage());

                    List<BatchResponse> errResponseList = new ArrayList<>();
                    errResponseList.add(errResponse);

                    status.setRollbackOnly();
                    return errResponseList;
                }
            });
        } catch (TransactionException | NonTransientDataAccessException ex) {
            ErrorInfo e = ErrorHandler.handler(ex);
            BatchResponse errResponse = new BatchResponse();
            errResponse.setStatusCode(e.getStatusCode());

            for (BatchResponse res : responseList) {
                if (!res.getStatusCode().equals(200)) {
                    errResponse.setBody("Transaction is being rolled back. First erroneous request: \n" + new Gson().toJson(res));
                    break;
                }
            }

            List<BatchResponse> errResponseList = new ArrayList<>();
            errResponseList.add(errResponse);

            return errResponseList;
        }
    }
}
