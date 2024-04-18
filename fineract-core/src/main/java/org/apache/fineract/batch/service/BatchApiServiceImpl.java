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

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPathException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.resilience4j.core.functions.Either;
import io.github.resilience4j.retry.Retry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.batch.command.CommandContext;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.command.CommandStrategyProvider;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.batch.domain.Header;
import org.apache.fineract.batch.exception.BatchReferenceInvalidException;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.batch.service.ResolutionHelper.BatchRequestNode;
import org.apache.fineract.infrastructure.core.domain.BatchRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.filters.BatchCallHandler;
import org.apache.fineract.infrastructure.core.filters.BatchFilter;
import org.apache.fineract.infrastructure.core.filters.BatchRequestPreprocessor;
import org.apache.fineract.infrastructure.retry.RetryConfigurationAssembler;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionExecution;
import org.springframework.transaction.TransactionSystemException;
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
    private final PlatformTransactionManager transactionManager;
    private final ErrorHandler errorHandler;

    private final List<BatchFilter> batchFilters;

    private final List<BatchRequestPreprocessor> batchPreprocessors;
    private final RetryConfigurationAssembler retryConfigurationAssembler;

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Run each request root step in a separated transaction
     *
     * @param requestList
     * @param uriInfo
     * @return
     */
    @Override
    public List<BatchResponse> handleBatchRequestsWithoutEnclosingTransaction(final List<BatchRequest> requestList, UriInfo uriInfo) {
        return handleBatchRequests(requestList, uriInfo, false);
    }

    /**
     * Run the batch request in transaction
     *
     * @param requestList
     * @param uriInfo
     * @return
     */
    @Override
    public List<BatchResponse> handleBatchRequestsWithEnclosingTransaction(final List<BatchRequest> requestList, final UriInfo uriInfo) {
        return handleBatchRequests(requestList, uriInfo, true);
    }

    private List<BatchResponse> handleBatchRequests(final List<BatchRequest> requestList, final UriInfo uriInfo,
            boolean enclosingTransaction) {
        BatchRequestContextHolder.setIsEnclosingTransaction(enclosingTransaction);
        try {
            return enclosingTransaction ? callInTransaction(Function.identity()::apply, () -> handleRequestNodes(requestList, uriInfo))
                    : handleRequestNodes(requestList, uriInfo);
        } finally {
            BatchRequestContextHolder.resetIsEnclosingTransaction();
        }
    }

    /**
     * Helper method to run the command in transaction
     *
     * @param request
     *            the enclosing supplier of the command
     * @param transactionConfigurator
     *            consumer to configure the transaction behavior and isolation
     * @return
     */
    private List<BatchResponse> callInTransaction(Consumer<TransactionTemplate> transactionConfigurator,
            Supplier<List<BatchResponse>> request) {
        // Retry logic for enclosingTransaction=true and when the isolation level is REPEATABLE_READ or stricter we need
        // to restart the transaction as well!

        Retry retry = retryConfigurationAssembler.getRetryConfigurationForBatchApiWithEnclosingTransaction();
        List<BatchResponse> responseList = new ArrayList<>();
        Supplier<List<BatchResponse>> batchSupplier = () -> {
            responseList.clear();
            try {
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
                transactionConfigurator.accept(transactionTemplate);
                return transactionTemplate.execute(status -> {
                    BatchRequestContextHolder.setEnclosingTransaction(status);
                    responseList.addAll(request.get());
                    return responseList;
                });
            } finally {
                BatchRequestContextHolder.resetTransaction();
            }
        };
        Supplier<List<BatchResponse>> retryingBatch = Retry.decorateSupplier(retry, batchSupplier);
        try {
            return retryingBatch.get();
        } catch (TransactionException | NonTransientDataAccessException ex) {
            return buildErrorResponses(ex, responseList);
        } catch (BatchExecutionException ex) {
            log.error("Exception during the batch request processing", ex);
            responseList.add(buildErrorResponse(ex.getCause(), ex.getRequest()));
            return responseList;
        }
    }

    /**
     * Returns the response list by getting a proper {@link org.apache.fineract.batch.command.CommandStrategy}.
     * execute() method of acquired commandStrategy is then provided with the separate Request.
     *
     * @param requestList
     * @param uriInfo
     * @return {@code List<BatchResponse>}
     */
    private List<BatchResponse> handleRequestNodes(final List<BatchRequest> requestList, final UriInfo uriInfo) {
        final List<BatchRequestNode> rootNodes;
        try {
            rootNodes = this.resolutionHelper.buildNodesTree(requestList);
        } catch (BatchReferenceInvalidException e) {
            return List.of(buildOrThrowErrorResponse(e, null));
        }

        final ArrayList<BatchResponse> responseList = new ArrayList<>(requestList.size());
        for (BatchRequestNode rootNode : rootNodes) {
            this.callRequestRecursive(rootNode.getRequest(), rootNode, responseList, uriInfo);
        }
        responseList.sort(Comparator.comparing(BatchResponse::getRequestId));
        return responseList;
    }

    /**
     * Executes the request and call child requests recursively.
     *
     * @param request
     *            the current batch request
     * @param requestNode
     *            the batch request holder node
     * @param responseList
     *            the collected responses
     * @return {@code BatchResponse}
     */
    private void callRequestRecursive(BatchRequest request, BatchRequestNode requestNode, List<BatchResponse> responseList,
            UriInfo uriInfo) {
        // run current node
        BatchResponse response = executeRequest(request, uriInfo);
        responseList.add(response);
        if (response.getStatusCode() != null && response.getStatusCode() == SC_OK) {
            // run child nodes
            requestNode.getChildNodes().forEach(childNode -> {
                BatchRequest childRequest = childNode.getRequest();
                BatchRequest resolvedChildRequest;
                try {
                    resolvedChildRequest = this.resolutionHelper.resolveRequest(childRequest, response);
                    callRequestRecursive(resolvedChildRequest, childNode, responseList, uriInfo);
                } catch (JsonPathException jpex) {
                    responseList.add(buildOrThrowErrorResponse(jpex, childRequest));
                }
            });
        } else {
            responseList.addAll(parentRequestFailedRecursive(request, requestNode, response, null));
        }
        // If the current request fails, then all the child requests are not executed. If we want to write out all the
        // child requests, here is the place.
    }

    /**
     * Execute the request
     *
     * @param request
     * @param uriInfo
     * @return
     */
    private BatchResponse executeRequest(BatchRequest request, UriInfo uriInfo) {
        final CommandStrategy commandStrategy = this.strategyProvider
                .getCommandStrategy(CommandContext.resource(request.getRelativeUrl()).method(request.getMethod()).build());
        log.debug("Batch request: method [{}], relative url [{}]", request.getMethod(), request.getRelativeUrl());
        Either<RuntimeException, BatchRequest> preprocessorResult = runPreprocessors(request);
        if (preprocessorResult.isLeft()) {
            return buildOrThrowErrorResponse(preprocessorResult.getLeft(), request);
        } else {
            request = preprocessorResult.get();
        }
        try {
            BatchRequestContextHolder.setRequestAttributes(new HashMap<>(Optional.ofNullable(request.getHeaders())
                    .map(list -> list.stream().collect(Collectors.toMap(Header::getName, Header::getValue)))
                    .orElse(Collections.emptyMap())));
            if (BatchRequestContextHolder.isEnclosingTransaction()) {
                entityManager.flush();
            }
            BatchCallHandler callHandler = new BatchCallHandler(this.batchFilters, commandStrategy::execute);
            final BatchResponse rootResponse = callHandler.serviceCall(request, uriInfo);
            log.debug("Batch response: status code [{}], method [{}], relative url [{}]", rootResponse.getStatusCode(), request.getMethod(),
                    request.getRelativeUrl());
            return rootResponse;
        } catch (RuntimeException ex) {
            return buildOrThrowErrorResponse(ex, request);
        } finally {
            BatchRequestContextHolder.resetRequestAttributes();
        }
    }

    private Either<RuntimeException, BatchRequest> runPreprocessors(BatchRequest request) {
        return runPreprocessor(batchPreprocessors, request);
    }

    private Either<RuntimeException, BatchRequest> runPreprocessor(List<BatchRequestPreprocessor> remainingPreprocessor,
            BatchRequest request) {
        if (remainingPreprocessor.isEmpty()) {
            return Either.right(request);
        } else {
            BatchRequestPreprocessor preprocessor = remainingPreprocessor.get(0);
            Either<RuntimeException, BatchRequest> processingResult = preprocessor.preprocess(request);
            if (processingResult.isLeft()) {
                return processingResult;
            } else {
                return runPreprocessor(remainingPreprocessor.subList(1, remainingPreprocessor.size()), processingResult.get());
            }
        }
    }

    /**
     * All requests recursively are set to status 409 if the parent request fails.
     *
     * @param request
     *            the current request
     * @param requestNode
     *            the current request node
     * @return {@code BatchResponse} list of the generated batch responses
     */
    private List<BatchResponse> parentRequestFailedRecursive(@NotNull BatchRequest request, @NotNull BatchRequestNode requestNode,
            @NotNull BatchResponse response, Long parentId) {
        List<BatchResponse> responseList = new ArrayList<>();
        if (parentId == null) { // root
            BatchRequestContextHolder.getEnclosingTransaction().ifPresent(TransactionExecution::setRollbackOnly);
        } else {
            responseList.add(buildErrorResponse(request.getRequestId(), response.getStatusCode(),
                    "Parent request with id " + parentId + " was erroneous!", null));
        }
        requestNode.getChildNodes().forEach(childNode -> responseList
                .addAll(parentRequestFailedRecursive(childNode.getRequest(), childNode, response, request.getRequestId())));
        return responseList;
    }

    /**
     * Return the response when any exception raised
     *
     * @param ex
     *            the exception
     * @param request
     *            the called request
     */
    private BatchResponse buildErrorResponse(Throwable ex, BatchRequest request) {
        Long requestId = null;
        Integer statusCode = null;
        String body = null;
        Set<Header> headers = new HashSet<>();
        if (ex != null) {
            ErrorInfo errorInfo = errorHandler.handle(ErrorHandler.getMappable(ex));
            statusCode = errorInfo.getStatusCode();
            body = errorInfo.getMessage();
            headers = Optional.ofNullable(errorInfo.getHeaders()).orElse(new HashSet<>());
        }
        if (request != null) {
            requestId = request.getRequestId();
            if (request.getHeaders() != null) {
                headers.addAll(request.getHeaders());
            }
        }
        return buildErrorResponse(requestId, statusCode, body, headers);
    }

    private BatchResponse buildOrThrowErrorResponse(RuntimeException ex, BatchRequest request) {
        BatchResponse response = buildErrorResponse(ex, request);
        if (response.getStatusCode() != SC_OK && BatchRequestContextHolder.isEnclosingTransaction()) {
            BatchRequestContextHolder.getTransaction().ifPresent(TransactionExecution::setRollbackOnly);
            throw new BatchExecutionException(request, ex);
        }
        return response;
    }

    @NotNull
    private List<BatchResponse> buildErrorResponses(Throwable ex, @NotNull List<BatchResponse> responseList) {
        BatchResponse response = responseList.isEmpty() ? null
                : responseList.stream().filter(e -> e.getStatusCode() == null || e.getStatusCode() != SC_OK).findFirst()
                        .orElse(responseList.get(responseList.size() - 1));

        if (response != null && response.getStatusCode() == SC_OK && ex instanceof TransactionSystemException tse) {
            ex = new ConcurrencyFailureException(tse.getMessage(), tse.getCause());
        }

        Long requestId = null;
        Integer statusCode = null;
        String body = null;
        Set<Header> headers = new HashSet<>();
        if (ex != null) {
            ErrorInfo errorInfo = errorHandler.handle(ErrorHandler.getMappable(ex));
            statusCode = errorInfo.getStatusCode();
            body = errorInfo.getMessage();
            headers = Optional.ofNullable(errorInfo.getHeaders()).orElse(new HashSet<>());
        }
        if (response != null) {
            requestId = response.getRequestId();
            Integer responseCode = response.getStatusCode();
            if (responseCode == null || responseCode != SC_OK) {
                if (responseCode != null) {
                    statusCode = responseCode;
                }
                body = "Transaction is being rolled back. First erroneous request: \n" + new Gson().toJson(response);
            }
            if (response.getHeaders() != null) {
                headers.addAll(response.getHeaders());
            }
        }
        return List.of(buildErrorResponse(requestId, statusCode, body, headers));
    }

    @SuppressFBWarnings(value = "BX_UNBOXING_IMMEDIATELY_REBOXED", justification = "TODO: fix this!")
    private BatchResponse buildErrorResponse(Long requestId, Integer statusCode, String body, Set<Header> headers) {
        return new BatchResponse().setRequestId(requestId).setStatusCode(statusCode == null ? SC_INTERNAL_SERVER_ERROR : statusCode)
                .setBody(body == null ? "Request with id " + requestId + " was erroneous!" : body).setHeaders(headers);
    }
}
