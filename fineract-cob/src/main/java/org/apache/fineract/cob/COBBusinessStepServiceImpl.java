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
package org.apache.fineract.cob;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.domain.BatchBusinessStep;
import org.apache.fineract.cob.domain.BatchBusinessStepRepository;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.cob.service.ReloaderService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class COBBusinessStepServiceImpl implements COBBusinessStepService {

    private final BatchBusinessStepRepository batchBusinessStepRepository;
    private final ApplicationContext applicationContext;
    private final ListableBeanFactory beanFactory;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final ConfigurationDomainService configurationDomainService;

    private final ReloaderService reloaderService;

    @SuppressWarnings({ "unchecked" })
    @Override
    // Spring Batch 6's ChunkOrientedStep submits item processing to the step's task executor. The
    // chunk transaction is bound to the step thread and wraps only the write phase, so with a task
    // executor in play processItem() would run on a pool thread with NO transaction. The business
    // steps below write to the DB, so processing needs a transaction of its own.
    //
    // The COB worker steps therefore register no task executor at all, and there is no configuration to change
    // that: processing stays on the step thread and REQUIRED joins the chunk transaction exactly as it did under
    // Batch 5.
    //
    // Were an executor ever reintroduced, this would open a NEW transaction per item on the worker thread, which
    // commits independently of the chunk write - so a chunk-write rollback, a skip in scan mode, or a step restart
    // would leave those process-time writes committed while the loan is not marked COB'd, and the next pass would
    // re-run the business steps over it. Do not reintroduce one until FINERACT-2684 establishes that the business
    // steps are idempotent under a second pass.
    //
    // This annotation is load-bearing even without an executor: Batch 6 swallows a skipped process failure instead
    // of rethrowing (Batch 5's FaultTolerantChunkProcessor rolled back by default), so the participating-transaction
    // rollback marking it produces is what stops a mid-chain failure from half-committing. Do not remove it.
    @Transactional
    public <T extends COBBusinessStep<S>, S extends AbstractPersistableCustom<Long>> S run(TreeMap<Long, String> executionMap, S item) {
        if (executionMap == null || executionMap.isEmpty()) {
            throw new BusinessStepException("Execution map is empty! COB Business step execution skipped!");
        }
        boolean bulkEventEnabled = configurationDomainService.isCOBBulkEventEnabled();
        // Extra safety net to avoid event leaking
        try {
            if (bulkEventEnabled) {
                businessEventNotifierService.startExternalEventRecording();
            }

            for (String businessStep : executionMap.values()) {
                try {
                    ThreadLocalContextUtil.setActionContext(ActionContext.COB);
                    COBBusinessStep<S> businessStepBean = (COBBusinessStep<S>) applicationContext.getBean(businessStep);
                    item = reloaderService.reload(item);
                    item = businessStepBean.execute(item);
                } catch (Exception e) {
                    throw new BusinessStepException("Error happened during business step execution", e);
                } finally {
                    // Fallback to COB action context after each business step
                    ThreadLocalContextUtil.setActionContext(ActionContext.COB);
                }
            }
            if (bulkEventEnabled) {
                businessEventNotifierService.stopExternalEventRecording();
            }
        } catch (Exception e) {
            if (bulkEventEnabled) {
                businessEventNotifierService.resetEventRecording();
            }
            throw e;
        }
        return item;
    }

    @NonNull
    @Override
    public <T extends COBBusinessStep<S>, S extends AbstractPersistableCustom<Long>> Set<BusinessStepNameAndOrder> getCOBBusinessSteps(
            Class<T> businessStepClass, String cobJobName) {
        List<BatchBusinessStep> cobStepConfigs = batchBusinessStepRepository.findAllByJobName(cobJobName);
        List<String> businessSteps = Arrays.stream(beanFactory.getBeanNamesForType(businessStepClass)).toList();
        Set<BusinessStepNameAndOrder> executionMap = new HashSet<>();
        for (String businessStep : businessSteps) {
            T businessStepBean = applicationContext.getBean(businessStep, businessStepClass);
            Optional<BatchBusinessStep> businessStepConfig = cobStepConfigs.stream()
                    .filter(stepConfig -> businessStepBean.getEnumStyledName().equals(stepConfig.getStepName())).findFirst();
            businessStepConfig.ifPresent(
                    batchBusinessStep -> executionMap.add(new BusinessStepNameAndOrder(businessStep, batchBusinessStep.getStepOrder())));
        }
        return executionMap;
    }
}
