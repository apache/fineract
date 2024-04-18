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
package org.apache.fineract.infrastructure.retry;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.service.BatchExecutionException;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.BatchRequestContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RetryConfigurationAssembler {

    private final RetryRegistry registry;
    private final FineractProperties fineractProperties;

    public <T> Retry getRetryConfigurationForExecuteCommand() {
        Class<? extends Throwable>[] exceptionList = fineractProperties.getResilience4j().getExecuteCommand().getRetryExceptions();
        RetryConfig.Builder<T> configBuilder = buildCommonExecuteCommandConfiguration();
        if (exceptionList != null) {
            // We dont wanna retry command if its batch API call with enclosingTransaction=true
            configBuilder.retryOnException(e -> !BatchRequestContextHolder.isEnclosingTransaction()
                    && Arrays.stream(exceptionList).anyMatch(re -> re.isAssignableFrom(e.getClass())));
        }
        RetryConfig config = configBuilder.build();
        return registry.retry("executeCommand", config);
    }

    public <T> Retry getRetryConfigurationForBatchApiWithEnclosingTransaction() {
        Class<? extends Throwable>[] exceptionList = fineractProperties.getResilience4j().getExecuteCommand().getRetryExceptions();
        RetryConfig.Builder<T> configBuilder = buildCommonExecuteCommandConfiguration();
        if (exceptionList != null) {
            configBuilder.retryExceptions(exceptionList);
            configBuilder.retryOnException(e -> e instanceof BatchExecutionException && e.getCause() != null
                    && Arrays.stream(exceptionList).anyMatch(re -> re.isAssignableFrom(e.getCause().getClass())));
        }
        RetryConfig config = configBuilder.build();
        return registry.retry("batchRetry", config);
    }

    private <T> RetryConfig.Builder<T> buildCommonExecuteCommandConfiguration() {
        Integer maxAttempts = fineractProperties.getResilience4j().getExecuteCommand().getMaxAttempts();
        Duration waitDuration = fineractProperties.getResilience4j().getExecuteCommand().getWaitDuration();
        Boolean enableExponentialBackoff = fineractProperties.getResilience4j().getExecuteCommand().getEnableExponentialBackoff();

        RetryConfig.Builder<T> configBuilder = RetryConfig.custom();
        configBuilder.maxAttempts(maxAttempts);

        if (waitDuration != null && waitDuration.toMillis() >= 0) {
            if (Boolean.TRUE.equals(enableExponentialBackoff)) {
                Double backoffMultiplier = fineractProperties.getResilience4j().getExecuteCommand().getExponentialBackoffMultiplier();
                if (backoffMultiplier != null) {
                    configBuilder.intervalFunction(IntervalFunction.ofExponentialBackoff(waitDuration, backoffMultiplier));
                } else {
                    configBuilder.intervalFunction(IntervalFunction.ofExponentialBackoff(waitDuration));
                }
            } else {
                configBuilder.waitDuration(waitDuration);
            }
        }
        return configBuilder;
    }

}
