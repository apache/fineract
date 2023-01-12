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
package org.apache.fineract.infrastructure.event.business.aspect;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.business.annotation.BulkEventSupport;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Aspect
public class BulkEventAspect {

    private final BusinessEventNotifierService businessEventNotifierService;

    @Around("execution(* org.apache.fineract.commands.handler.NewCommandSourceHandler.processCommand(..)) && @within(bulkEventSupport)")
    public Object processCommandJoinPoint(ProceedingJoinPoint proceedingJoinPoint, BulkEventSupport bulkEventSupport) throws Throwable {
        return bulkAroundWrapper(proceedingJoinPoint, bulkEventSupport);
    }

    @Around("execution(* org.springframework.batch.core.step.tasklet.Tasklet.execute(..)) && @within(bulkEventSupport)")
    public Object taskletExecuteJoinPoint(ProceedingJoinPoint proceedingJoinPoint, BulkEventSupport bulkEventSupport) throws Throwable {
        return bulkAroundWrapper(proceedingJoinPoint, bulkEventSupport);
    }

    private Object bulkAroundWrapper(ProceedingJoinPoint proceedingJoinPoint, BulkEventSupport bulkEventSupport) throws Throwable {
        if (!bulkEventSupport.value()) {
            return proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        }
        try {
            businessEventNotifierService.startExternalEventRecording();
            Object result = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
            businessEventNotifierService.stopExternalEventRecording();
            return result;
        } finally {
            businessEventNotifierService.resetEventRecording();
        }
    }
}
