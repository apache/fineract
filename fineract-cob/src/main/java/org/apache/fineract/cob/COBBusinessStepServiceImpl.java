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

    @Override
    // Called from the COB item writers (AbstractCOBItemWriter), on the step thread and inside the chunk
    // transaction: REQUIRED joins it, so the business steps commit together with the loan's last closed business date
    // and the release of its lock, or not at all.
    //
    // The annotation is load-bearing. A failing business step marks the chunk transaction rollback-only through it, so
    // a mid-chain failure can never half-commit. Spring Batch then rolls the chunk back and, in scan mode, writes each
    // loan again in a transaction of its own; only the failing loan is skipped.
    //
    // Do not move this call back into an ItemProcessor. Batch 6 skips a failed process inside the chunk transaction
    // without rolling it back or replaying the chunk, so the rollback marking above would silently discard every
    // healthy loan of the chunk. And with a task executor registered on the step, processing would run on a pool
    // thread with no transaction at all. Do not open a new transaction here either: it would commit the business
    // steps independently of the loan being marked COB'd, and the next pass would re-run them over it (FINERACT-2684).
    @Transactional
    public <T extends COBBusinessStep<S>, S extends AbstractPersistableCustom<Long>> S run(TreeMap<Long, String> executionMap, S item) {
        if (executionMap == null || executionMap.isEmpty()) {
            throw new BusinessStepException("Execution map is empty! COB Business step execution skipped!");
        }
        if (!configurationDomainService.isCOBBulkEventEnabled()) {
            return executeBusinessSteps(executionMap, item);
        }
        // Everything the chain raises - including any recording window a business step opens of its own - reaches the
        // consumer as a single bulk event.
        return businessEventNotifierService.withExternalEventRecording(() -> executeBusinessSteps(executionMap, item));
    }

    @SuppressWarnings({ "unchecked" })
    private <S extends AbstractPersistableCustom<Long>> S executeBusinessSteps(TreeMap<Long, String> executionMap, S item) {
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
