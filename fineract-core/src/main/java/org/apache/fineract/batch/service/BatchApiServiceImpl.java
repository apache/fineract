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
import io.github.resilience4j.core.functions.Either;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import org.apache.fineract.batch.exception.ClientDetailsNotFoundException;
import org.apache.fineract.batch.exception.ErrorHandler;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.batch.service.ResolutionHelper.BatchRequestNode;
import org.apache.fineract.infrastructure.core.domain.BatchRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.AbstractIdempotentCommandException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessFailedException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessSucceedException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessUnderProcessingException;
import org.apache.fineract.infrastructure.core.filters.BatchCallHandler;
import org.apache.fineract.infrastructure.core.filters.BatchFilter;
import org.apache.fineract.infrastructure.core.filters.BatchRequestPreprocessor;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionExecution;
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

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Returns the response list by getting a proper {@link org.apache.fineract.batch.command.CommandStrategy}.
     * execute() method of acquired commandStrategy is then provided with the separate Request.
     *
     * @param requestList
     * @param uriInfo
     * @return {@code List<BatchResponse>}
     */
    private List<BatchResponse> handleBatchRequests(boolean enclosingTransaction, final List<BatchRequest> requestList,
            final UriInfo uriInfo) {

        final List<BatchResponse> responseList = new ArrayList<>(requestList.size());

        final List<BatchRequestNode> batchRequestNodes = this.resolutionHelper.getDependingRequests(requestList);
        if (batchRequestNodes.isEmpty()) {
            final BatchResponse response = new BatchResponse();
            ErrorInfo ex = errorHandler.handle(new ClientDetailsNotFoundException());
            response.setStatusCode(500);
            response.setBody(ex.getMessage());
            responseList.add(response);
            return responseList;
        }

        for (BatchRequestNode rootNode : batchRequestNodes) {
            if (enclosingTransaction) {
                this.callRequestRecursive(rootNode.getRequest(), rootNode, responseList, uriInfo, enclosingTransaction);
            } else {
                List<BatchResponse> localResponseList = new ArrayList<>();
                this.callRequestRecursive(rootNode.getRequest(), rootNode, localResponseList, uriInfo, enclosingTransaction);
                responseList.addAll(localResponseList);
            }
        }
        Collections.sort(responseList, Comparator.comparing(BatchResponse::getRequestId));
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
    private void callRequestRecursive(BatchRequest request, BatchRequestNode requestNode, List<BatchResponse> responseList, UriInfo uriInfo,
            boolean enclosingTransaction) {
        // 1. run current node
        BatchResponse response;
        if (enclosingTransaction) {
            response = executeRequest(request, uriInfo);
        } else {
            List<BatchResponse> transactionResponse = callInTransaction(
                    transactionTemplate -> transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW),
                    () -> List.of(executeRequest(request, uriInfo)));
            response = transactionResponse.get(0);
        }
        responseList.add(response);
        if (response.getStatusCode() != null && response.getStatusCode() == 200) {
            requestNode.getChildRequests().forEach(childNode -> {
                BatchRequest resolvedChildRequest;
                try {
                    resolvedChildRequest = this.resolutionHelper.resoluteRequest(childNode.getRequest(), response);
                } catch (RuntimeException ex) {
                    throw new BatchExecutionException(childNode.getRequest(), ex, errorHandler.handle(ex));
                }
                callRequestRecursive(resolvedChildRequest, childNode, responseList, uriInfo, enclosingTransaction);

            });
        } else {
            responseList.addAll(parentRequestFailedRecursive(request, requestNode));
        }
        // If the current request fails, then all the child requests are not executed. If we want to write out all the
        // child requests, here is the place.
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
    private List<BatchResponse> parentRequestFailedRecursive(BatchRequest request, BatchRequestNode requestNode) {
        List<BatchResponse> responseList = new ArrayList<>();
        BatchRequestContextHolder.getEnclosingTransaction().ifPresent(TransactionExecution::setRollbackOnly);
        BatchResponse errorResponse = new BatchResponse();
        errorResponse.setRequestId(request.getRequestId());
        errorResponse.setStatusCode(Status.CONFLICT.getStatusCode());

        // Some detail information about the error
        final ErrorInfo conflictError = new ErrorInfo(Status.CONFLICT.getStatusCode(), 8001,
                "Parent request with id " + request.getRequestId() + " was erroneous!");
        errorResponse.setBody(conflictError.getMessage());
        requestNode.getChildRequests().stream()
                .flatMap(childNode -> parentRequestFailedRecursive(childNode.getRequest(), childNode).stream()).forEach(responseList::add);
        return responseList;
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
            throw new BatchExecutionException(request, preprocessorResult.getLeft(), errorHandler.handle(preprocessorResult.getLeft()));
        } else {
            request = preprocessorResult.get();
        }
        try {
            BatchRequestContextHolder.setRequestAttributes(new HashMap<>(Optional.ofNullable(request.getHeaders())
                    .map(list -> list.stream().collect(Collectors.toMap(Header::getName, Header::getValue)))
                    .orElse(Collections.emptyMap())));
            BatchCallHandler callHandler = new BatchCallHandler(this.batchFilters, commandStrategy::execute);
            if (BatchRequestContextHolder.getEnclosingTransaction().isPresent()) {
                if (BatchRequestContextHolder.getEnclosingTransaction().get().isRollbackOnly()) {
                    BatchResponse br = new BatchResponse();
                    br.setBody("Parent request was erroneous!");
                    br.setRequestId(request.getRequestId());
                    return br;
                }
                entityManager.flush();
            }
            final BatchResponse rootResponse = callHandler.serviceCall(request, uriInfo);
            log.debug("Batch response: status code [{}], method [{}], relative url [{}]", rootResponse.getStatusCode(), request.getMethod(),
                    request.getRelativeUrl());
            return rootResponse;
        } catch (AbstractIdempotentCommandException idempotentException) {
            return handleIdempotentRequests(idempotentException, request);
        } catch (RuntimeException ex) {
            throw new BatchExecutionException(request, ex, errorHandler.handle(ex));
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
     * Return the idempotent response when idempotent exception raised
     *
     * @param idempotentException
     *            the idempotent exception
     * @param request
     *            the called request
     * @return
     */
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

    /**
     * Run each request root step in a separated transaction
     *
     * @param requestList
     * @param uriInfo
     * @return
     */
    @Override
    public List<BatchResponse> handleBatchRequestsWithoutEnclosingTransaction(final List<BatchRequest> requestList, UriInfo uriInfo) {
        BatchRequestContextHolder.setEnclosingTransaction(Optional.empty());
        return handleBatchRequests(false, requestList, uriInfo);
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
        return callInTransaction(() -> handleBatchRequests(true, requestList, uriInfo));
    }

    @NotNull
    private List<BatchResponse> createErrorResponse(List<BatchResponse> responseList, int statusCode) {
        BatchResponse errResponse = new BatchResponse();

        for (BatchResponse res : responseList) {
            if (res.getStatusCode() == null || !res.getStatusCode().equals(200)) {
                errResponse.setBody("Transaction is being rolled back. First erroneous request: \n" + new Gson().toJson(res));
                errResponse.setRequestId(res.getRequestId());
                if (statusCode == -1) {
                    if (res.getStatusCode() != null) {
                        statusCode = res.getStatusCode();
                    } else {
                        statusCode = Status.INTERNAL_SERVER_ERROR.getStatusCode();
                    }
                }
                break;
            }
        }
        errResponse.setStatusCode(statusCode);
        return Arrays.asList(errResponse);
    }

    private List<BatchResponse> callInTransaction(Supplier<List<BatchResponse>> request) {
        return callInTransaction(Function.identity()::apply, request);
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
        List<BatchResponse> responseList = new ArrayList<>();
        try {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionConfigurator.accept(transactionTemplate);
            return transactionTemplate.execute(status -> {
                BatchRequestContextHolder.setEnclosingTransaction(Optional.of(status));
                try {
                    responseList.addAll(request.get());
                    if (status.isRollbackOnly()) {
                        return createErrorResponse(responseList, -1);
                    }
                    return responseList;
                } catch (BatchExecutionException ex) {
                    status.setRollbackOnly();
                    BatchResponse errResponse = new BatchResponse();
                    errResponse.setStatusCode(ex.getErrorInfo().getStatusCode());
                    errResponse.setRequestId(ex.getRequest().getRequestId());
                    errResponse.setBody(ex.getErrorInfo().getMessage());
                    return Arrays.asList(errResponse);
                } catch (RuntimeException ex) {
                    status.setRollbackOnly();
                    ErrorInfo e = errorHandler.handle(ex);
                    BatchResponse errResponse = new BatchResponse();
                    errResponse.setStatusCode(e.getStatusCode());
                    errResponse.setBody(e.getMessage());
                    return Arrays.asList(errResponse);
                }
            });
        } catch (TransactionException | NonTransientDataAccessException ex) {
            ErrorInfo e = errorHandler.handle(ex);
            return createErrorResponse(responseList, e.getStatusCode());
        }
    }

}
