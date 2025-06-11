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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.UriInfo;
import java.time.Duration;
import java.util.List;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.command.CommandStrategyProvider;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.commands.configuration.RetryConfigurationAssembler;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.filters.BatchRequestPreprocessor;
import org.apache.fineract.infrastructure.core.persistence.ExtendedJpaTransactionManager;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.DefaultTransactionStatus;

@ExtendWith(MockitoExtension.class)
class BatchApiServiceImplTest {

    @Mock
    private CommandStrategyProvider strategyProvider;
    @Mock
    private ExtendedJpaTransactionManager transactionManager;
    @Mock
    private EntityManager entityManager;
    @Mock
    private CommandStrategy commandStrategy;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private RetryRegistry registry;

    @Mock
    private FineractProperties fineractProperties;

    @Spy
    private FineractRequestContextHolder fineractRequestContextHolder;

    @InjectMocks
    private RetryConfigurationAssembler retryConfigurationAssembler;

    private final ResolutionHelper resolutionHelper = Mockito.spy(new ResolutionHelper(new FromJsonHelper()));
    private final List<BatchRequestPreprocessor> batchPreprocessors = Mockito.spy(List.of());

    @InjectMocks
    private BatchApiServiceImpl batchApiService;
    private BatchRequest request;
    private BatchResponse response;

    @BeforeEach
    void setUp() {
        batchApiService = new BatchApiServiceImpl(strategyProvider, resolutionHelper, transactionManager, errorHandler, List.of(),
                batchPreprocessors, retryConfigurationAssembler);
        batchApiService.setEntityManager(entityManager);
        request = new BatchRequest();
        request.setRequestId(1L);
        request.setMethod("POST");
        request.setRelativeUrl("/random_api");
        response = new BatchResponse();
        response.setRequestId(1L);
        response.setStatusCode(200);
        response.setBody("Success");
        FineractProperties.RetryProperties settings = new FineractProperties.RetryProperties();
        settings.setInstances(new FineractProperties.RetryProperties.InstancesProperties());
        settings.getInstances().setExecuteCommand(new FineractProperties.RetryProperties.InstancesProperties.ExecuteCommandProperties());
        settings.getInstances().getExecuteCommand().setMaxAttempts(3);
        settings.getInstances().getExecuteCommand().setWaitDuration(Duration.ofMillis(2));
        settings.getInstances().getExecuteCommand().setEnableExponentialBackoff(false);
        settings.getInstances().getExecuteCommand().setRetryExceptions(new Class[] { RetryException.class });
        when(fineractProperties.getRetry()).thenReturn(settings);
        when(registry.retry(anyString(), any(RetryConfig.class)))
                .thenAnswer(i -> Retry.of((String) i.getArgument(0), (RetryConfig) i.getArgument(1)));
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(resolutionHelper);
        Mockito.reset(batchPreprocessors);
        Mockito.reset(entityManager);
        Mockito.reset(commandStrategy);
        Mockito.reset(strategyProvider);
        Mockito.reset(transactionManager);
    }

    @Test
    void testHandleBatchRequestsWithEnclosingTransactionResult200WithRetryOnTransactionFailure() {

        List<BatchRequest> requestList = List.of(request);
        when(strategyProvider.getCommandStrategy(any())).thenReturn(commandStrategy);
        when(commandStrategy.execute(any(), any())).thenReturn(response).thenReturn(response);
        // throw exception at transaction commit to verify If OptimisticLockException or similar exceptions are on the
        // retry list, they can perform a retry.
        // do nothing on 2nd hit to simulate success commit
        doThrow(new RetryException()).doNothing().when(transactionManager).commit(any());

        // Regular transaction
        when(transactionManager.getTransaction(any()))
                .thenReturn(new DefaultTransactionStatus("txn_name", null, true, true, false, false, false, null));

        List<BatchResponse> result = batchApiService.handleBatchRequestsWithEnclosingTransaction(requestList, uriInfo);
        assertEquals(1, result.size());
        assertEquals(200, result.getFirst().getStatusCode());
        assertTrue(result.getFirst().getBody().contains("Success"));

        verify(transactionManager, times(2)).commit(any());
        verify(entityManager, times(2)).flush();
    }

    @Test
    void testHandleBatchRequestsWithEnclosingTransactionResult200WithRetry() {

        ErrorInfo errorInfo = mock(ErrorInfo.class);
        when(errorInfo.getMessage()).thenReturn("Failed");
        when(errorInfo.getStatusCode()).thenReturn(500);
        when(errorHandler.handle(any())).thenReturn(errorInfo);

        List<BatchRequest> requestList = List.of(request);
        when(strategyProvider.getCommandStrategy(any())).thenReturn(commandStrategy);
        when(commandStrategy.execute(any(), any())).thenThrow(new RetryException()).thenReturn(response);
        // Regular transaction
        when(transactionManager.getTransaction(any()))
                .thenReturn(new DefaultTransactionStatus("txn_name", null, true, true, false, false, false, null));
        List<BatchResponse> result = batchApiService.handleBatchRequestsWithEnclosingTransaction(requestList, uriInfo);
        assertEquals(1, result.size());
        assertEquals(200, result.getFirst().getStatusCode());
        assertTrue(result.getFirst().getBody().contains("Success"));
        Mockito.verify(entityManager, times(2)).flush();
    }

    @Test
    void testHandleBatchRequestsWithEnclosingTransactionFailsWithRetry() {

        List<BatchRequest> requestList = List.of(request);
        when(strategyProvider.getCommandStrategy(any())).thenReturn(commandStrategy);
        when(commandStrategy.execute(any(), any())).thenThrow(new RetryException()).thenThrow(new RetryException())
                .thenThrow(new RetryException());

        ErrorInfo errorInfo = mock(ErrorInfo.class);
        when(errorInfo.getMessage()).thenReturn("Failed");
        when(errorInfo.getStatusCode()).thenReturn(500);
        when(errorHandler.handle(any())).thenReturn(errorInfo);

        // Regular transaction
        when(transactionManager.getTransaction(any()))
                .thenReturn(new DefaultTransactionStatus("txn_name", null, true, true, false, false, false, null));
        List<BatchResponse> result = batchApiService.handleBatchRequestsWithEnclosingTransaction(requestList, uriInfo);
        assertEquals(1, result.size());
        assertEquals(500, result.getFirst().getStatusCode());
        assertTrue(result.getFirst().getBody().contains("Failed"));
        Mockito.verify(entityManager, times(3)).flush();
    }

    @Test
    void testHandleBatchRequestsWithEnclosingTransaction() {
        List<BatchRequest> requestList = List.of(request);
        when(strategyProvider.getCommandStrategy(any())).thenReturn(commandStrategy);
        when(commandStrategy.execute(any(), any())).thenReturn(response);
        // Regular transaction
        when(transactionManager.getTransaction(any()))
                .thenReturn(new DefaultTransactionStatus("txn_name", null, true, true, false, false, false, null));
        List<BatchResponse> result = batchApiService.handleBatchRequestsWithEnclosingTransaction(requestList, uriInfo);
        assertEquals(1, result.size());
        assertEquals(200, result.get(0).getStatusCode());
        assertTrue(result.get(0).getBody().contains("Success"));
        Mockito.verify(entityManager, times(1)).flush();
    }

    @Test
    void testHandleBatchRequestsWithEnclosingTransactionReadOnly() {
        List<BatchRequest> requestList = List.of(request);
        when(strategyProvider.getCommandStrategy(any())).thenReturn(commandStrategy);
        when(commandStrategy.execute(any(), any())).thenReturn(response);
        // Read-only transaction
        when(transactionManager.getTransaction(any()))
                .thenReturn(new DefaultTransactionStatus("txn_name", null, true, true, false, true, false, null));
        List<BatchResponse> result = batchApiService.handleBatchRequestsWithEnclosingTransaction(requestList, uriInfo);
        assertEquals(1, result.size());
        assertEquals(200, result.get(0).getStatusCode());
        assertTrue(result.get(0).getBody().contains("Success"));
        Mockito.verifyNoInteractions(entityManager);
    }

    private static final class RetryException extends RuntimeException {}

}
