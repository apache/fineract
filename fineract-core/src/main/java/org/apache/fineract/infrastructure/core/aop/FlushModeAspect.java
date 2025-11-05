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
package org.apache.fineract.infrastructure.core.aop;

import jakarta.persistence.FlushModeType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.annotation.WithFlushMode;
import org.apache.fineract.infrastructure.core.persistence.FlushModeHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ClassUtils;

/**
 * Aspect that handles the @WithFlushMode annotation to manage JPA flush mode around method execution.
 * <p>
 * This aspect is ordered to run after the @Transactional aspect (Ordered.LOWEST_PRECEDENCE - 1) to ensure proper
 * transaction management. It will only modify the flush mode if there is an active transaction.
 */
@Aspect
@Component
@Order
@RequiredArgsConstructor
public class FlushModeAspect {

    private static final Logger logger = LoggerFactory.getLogger(FlushModeAspect.class);
    private final FlushModeHandler flushModeHandler;

    @Around("@within(withFlushMode) || @annotation(withFlushMode)")
    public Object manageFlushMode(ProceedingJoinPoint joinPoint, WithFlushMode withFlushMode) {
        // Get the effective annotation (method level takes precedence over class level)
        WithFlushMode effectiveAnnotation = getEffectiveAnnotation(joinPoint, withFlushMode);
        if (effectiveAnnotation == null) {
            return jointPointProceed(joinPoint);
        }

        FlushModeType flushMode = effectiveAnnotation.value();

        // Check if we're in an active transaction
        boolean hasActiveTransaction = TransactionSynchronizationManager.isActualTransactionActive();

        if (!hasActiveTransaction) {
            if (logger.isDebugEnabled()) {
                logger.warn("No active transaction found for @WithFlushMode on {}.{}", joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName());
            }
            return jointPointProceed(joinPoint);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Setting flush mode to {} for {}.{}", flushMode, joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
        }

        // Use FlushModeHandler to manage the flush mode around method execution
        return flushModeHandler.withFlushMode(flushMode, () -> jointPointProceed(joinPoint));
    }

    private static Object jointPointProceed(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("Error in method with @WithFlushMode", e);
        }
    }

    private WithFlushMode getEffectiveAnnotation(ProceedingJoinPoint joinPoint, WithFlushMode annotation) {
        // If the annotation is already present on the method, use it
        if (annotation != null && joinPoint.getSignature() instanceof MethodSignature) {
            return annotation;
        }

        // Otherwise, try to get the class-level annotation
        Class<?> targetClass = ClassUtils.getUserClass(joinPoint.getTarget().getClass());
        return AnnotationUtils.findAnnotation(targetClass, WithFlushMode.class);
    }
}
