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
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.BatchBusinessStep;
import org.apache.fineract.cob.domain.BatchBusinessStepRepository;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class COBBusinessStepServiceImpl implements COBBusinessStepService {

    private final BatchBusinessStepRepository batchBusinessStepRepository;
    private final ApplicationContext applicationContext;
    private final ListableBeanFactory beanFactory;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public <T extends COBBusinessStep<S>, S extends AbstractPersistableCustom> S run(TreeMap<Long, String> executionMap, S item) {
        if (executionMap == null || executionMap.isEmpty()) {
            throw new BusinessStepException("Execution map is empty! COB Business step execution skipped!");
        }
        // Extra safety net to avoid event leaking
        try {
            businessEventNotifierService.startExternalEventRecording();

            for (String businessStep : executionMap.values()) {
                try {
                    ThreadLocalContextUtil.setActionContext(ActionContext.COB);
                    COBBusinessStep<S> businessStepBean = (COBBusinessStep<S>) applicationContext.getBean(businessStep);
                    item = businessStepBean.execute(item);
                } catch (Exception e) {
                    throw new BusinessStepException("Error happened during business step execution", e);
                } finally {
                    // Fallback to COB action context after each business step
                    ThreadLocalContextUtil.setActionContext(ActionContext.COB);
                }
            }
            businessEventNotifierService.stopExternalEventRecording();
        } catch (Exception e) {
            businessEventNotifierService.resetEventRecording();
            throw e;
        }
        return item;
    }

    @NotNull
    @Override
    public <T extends COBBusinessStep<S>, S extends AbstractPersistableCustom> TreeMap<Long, String> getCOBBusinessStepMap(
            Class<T> businessStepClass, String cobJobName) {
        List<BatchBusinessStep> cobStepConfigs = batchBusinessStepRepository.findAllByJobName(cobJobName);
        List<String> businessSteps = Arrays.stream(beanFactory.getBeanNamesForType(businessStepClass)).toList();
        TreeMap<Long, String> executionMap = new TreeMap<>();
        for (String businessStep : businessSteps) {
            COBBusinessStep<S> businessStepBean = (COBBusinessStep<S>) applicationContext.getBean(businessStep);
            Optional<BatchBusinessStep> businessStepConfig = cobStepConfigs.stream()
                    .filter(stepConfig -> businessStepBean.getEnumStyledName().equals(stepConfig.getStepName())).findFirst();
            businessStepConfig.ifPresent(batchBusinessStep -> executionMap.put(batchBusinessStep.getStepOrder(), businessStep));
        }
        return executionMap;
    }
}
