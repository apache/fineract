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
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

/**
 * Aspect that handles the @WithFlushMode annotation to manage JPA flush mode around method execution.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class FlushModeAspect {

    private final FlushModeHandler flushModeHandler;

    @Around("@within(withFlushMode) || @annotation(withFlushMode)")
    public Object manageFlushMode(ProceedingJoinPoint joinPoint, WithFlushMode withFlushMode) {
        // Get the effective annotation (method level takes precedence over class level)
        WithFlushMode effectiveAnnotation = getEffectiveAnnotation(joinPoint, withFlushMode);

        FlushModeType flushMode = effectiveAnnotation.value();

        // Use FlushModeHandler to manage the flush mode around method execution
        return flushModeHandler.withFlushMode(flushMode, () -> {
            try {
                return joinPoint.proceed();
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    private WithFlushMode getEffectiveAnnotation(ProceedingJoinPoint joinPoint, WithFlushMode annotation) {
        // If the annotation is already present on the method, use it
        if (annotation != null && joinPoint.getSignature() instanceof MethodSignature) {
            return annotation;
        }

        // Otherwise, try to get the class-level annotation
        Class<?> targetClass = joinPoint.getTarget().getClass();
        return AnnotationUtils.findAnnotation(targetClass, WithFlushMode.class);
    }
}
