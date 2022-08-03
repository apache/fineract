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
import org.apache.fineract.cob.domain.BatchBusinessStep;
import org.apache.fineract.cob.domain.BatchBusinessStepRepository;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class COBBusinessStepServiceImpl implements COBBusinessStepService {

    private final BatchBusinessStepRepository batchBusinessStepRepository;
    private final ApplicationContext applicationContext;
    private final ListableBeanFactory beanFactory;

    @Override
    public <T extends COBBusinessStep<S>, S extends AbstractPersistableCustom> S run(TreeMap<Long, String> executionMap, S item) {
        for (String businessStep : executionMap.values()) {
            COBBusinessStep<S> businessStepBean = (COBBusinessStep<S>) applicationContext.getBean(businessStep);
            item = businessStepBean.execute(item);
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
