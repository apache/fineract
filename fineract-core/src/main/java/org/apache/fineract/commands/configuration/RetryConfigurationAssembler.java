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
package org.apache.fineract.commands.configuration;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import org.apache.fineract.batch.service.BatchExecutionException;
import org.apache.fineract.commands.exception.CommandResultPersistenceException;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class RetryConfigurationAssembler {

    public static final String EXECUTE_COMMAND = "executeCommand";
    public static final String BATCH_RETRY = "batchRetry";
    public static final String COMMAND_RESULT_PERSISTENCE = "commandResultPersistence";
    private static final String LAST_EXECUTION_EXCEPTION_KEY = "LAST_EXECUTION_EXCEPTION";
    private final RetryRegistry registry;
    private final FineractProperties fineractProperties;
    private final FineractRequestContextHolder fineractRequestContextHolder;

    private boolean isAssignableFrom(Object e, Class<? extends Throwable>[] exceptionList) {
        return Arrays.stream(exceptionList).anyMatch(re -> re.isAssignableFrom(e.getClass()));
    }

    private void setLastException(Object ex) {
        fineractRequestContextHolder.setAttribute(LAST_EXECUTION_EXCEPTION_KEY, ex.getClass());
    }

    public Class<?> getLastException() {
        return (Class<?>) fineractRequestContextHolder.getAttribute(RetryConfigurationAssembler.LAST_EXECUTION_EXCEPTION_KEY);
    }

    public Retry getRetryConfigurationForExecuteCommand() {
        Class<? extends Throwable>[] exceptionList = fineractProperties.getRetry().getInstances().getExecuteCommand().getRetryExceptions();
        RetryConfig.Builder configBuilder = buildCommonExecuteCommandConfiguration();

        if (exceptionList != null) {
            configBuilder.retryOnException(ex -> {
                setLastException(ex);
                return isAssignableFrom(ex, exceptionList);
            });
        }

        RetryConfig config = configBuilder.build();
        return registry.retry(EXECUTE_COMMAND, config);
    }

    public Retry getRetryConfigurationForBatchApiWithEnclosingTransaction() {
        Class<? extends Throwable>[] exceptionList = fineractProperties.getRetry().getInstances().getExecuteCommand().getRetryExceptions();
        RetryConfig.Builder configBuilder = buildCommonExecuteCommandConfiguration();

        if (exceptionList != null) {
            configBuilder.retryOnException(ex -> {
                if (ex instanceof BatchExecutionException e) {
                    setLastException(e.getCause().getClass());
                    return isAssignableFrom(e.getCause(), exceptionList);
                } else {
                    setLastException(ex);
                    return isAssignableFrom(ex, exceptionList);
                }
            });
        }

        RetryConfig config = configBuilder.build();
        return registry.retry(BATCH_RETRY, config);
    }

    private RetryConfig.Builder buildCommonExecuteCommandConfiguration() {
        var props = fineractProperties.getRetry().getInstances().getExecuteCommand();

        RetryConfig.Builder configBuilder = RetryConfig.custom().maxAttempts(props.getMaxAttempts());

        if (props.getWaitDuration() != null && props.getWaitDuration().toMillis() >= 0) {
            if (Boolean.TRUE.equals(props.getEnableExponentialBackoff())) {
                Double multiplier = props.getExponentialBackoffMultiplier();
                if (multiplier != null) {
                    configBuilder.intervalFunction(IntervalFunction.ofExponentialBackoff(props.getWaitDuration(), multiplier));
                } else {
                    configBuilder.intervalFunction(IntervalFunction.ofExponentialBackoff(props.getWaitDuration()));
                }
            } else {
                configBuilder.waitDuration(props.getWaitDuration());
            }
        }

        return configBuilder;
    }

    public Retry getRetryConfigurationForCommandResultPersistence() {
        RetryConfig.Builder configBuilder = buildCommonExecuteCommandConfiguration();

        configBuilder.retryOnException(e -> e instanceof RuntimeException && !(e instanceof CommandResultPersistenceException));

        RetryConfig config = configBuilder.build();
        return registry.retry(COMMAND_RESULT_PERSISTENCE, config);
    }
}
