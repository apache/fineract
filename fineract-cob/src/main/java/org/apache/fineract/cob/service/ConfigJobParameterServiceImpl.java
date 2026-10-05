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
package org.apache.fineract.cob.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.COBBusinessStep;
import org.apache.fineract.cob.data.BusinessStep;
import org.apache.fineract.cob.data.BusinessStepDetail;
import org.apache.fineract.cob.data.JobBusinessStepConfigData;
import org.apache.fineract.cob.data.JobBusinessStepDetail;
import org.apache.fineract.cob.domain.BatchBusinessStep;
import org.apache.fineract.cob.domain.BatchBusinessStepRepository;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.cob.exceptions.BusinessStepNotBelongsToJobException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigJobParameterServiceImpl implements ConfigJobParameterService {

    private final BatchBusinessStepRepository batchBusinessStepRepository;
    private final BusinessStepConfigDataParser dataParser;
    private final List<BusinessStepCategoryService> businessStepCategoryServices;
    private final ApplicationContext applicationContext;
    private final BusinessStepMapper mapper;

    @Override
    public JobBusinessStepConfigData getBusinessStepConfigByJobName(String jobName) {
        List<BatchBusinessStep> batchBusinessSteps = batchBusinessStepRepository.findAllByJobName(jobName);
        JobBusinessStepConfigData jobBusinessStepConfigData = new JobBusinessStepConfigData();
        jobBusinessStepConfigData.setJobName(jobName);
        jobBusinessStepConfigData.setBusinessSteps(mapper.map(batchBusinessSteps));
        return jobBusinessStepConfigData;
    }

    @Override
    public CommandProcessingResult updateStepConfigByJobName(final JsonCommand command, final String jobName)
            throws BusinessStepNotBelongsToJobException {
        final List<BusinessStep> businessSteps = dataParser.parseUpdate(command);
        if (businessSteps.isEmpty()) {
            throw new BusinessStepException("A job needs to have 1 business step at least.");
        }
        final BusinessStepCategoryService categoryService = findByCobJobName(jobName)
                .orElseThrow(() -> new BusinessStepException(jobName + " is not a configurable business step job."));
        final List<String> availableBusinessStepNames = availableBusinessSteps(categoryService).stream()
                .map(BusinessStepDetail::getStepName).toList();
        final List<String> notValidBusinessStepNames = businessSteps.stream().map(BusinessStep::getStepName)
                .filter(businessStepName -> !availableBusinessStepNames.contains(businessStepName)).toList();
        if (!notValidBusinessStepNames.isEmpty()) {
            throw new BusinessStepException(notValidBusinessStepNames + " Business steps are not configurable for this job.");
        }
        batchBusinessStepRepository.deleteAllByJobName(jobName);
        businessSteps.forEach(newBusinessStepConfig -> {
            final BatchBusinessStep batchBusinessStep = new BatchBusinessStep();
            batchBusinessStep.setJobName(jobName);
            batchBusinessStep.setStepName(newBusinessStepConfig.getStepName());
            batchBusinessStep.setStepOrder(newBusinessStepConfig.getOrder());
            batchBusinessStepRepository.save(batchBusinessStep);
        });
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .build();
    }

    @Override
    public JobBusinessStepDetail getAvailableBusinessStepsByJobName(final String jobName) {
        return findByCategoryOrCobJobName(jobName).map(categoryService -> {
            final JobBusinessStepDetail jobBusinessStepDetail = new JobBusinessStepDetail();
            jobBusinessStepDetail.setJobName(jobName);
            jobBusinessStepDetail.setAvailableBusinessSteps(availableBusinessSteps(categoryService));
            return jobBusinessStepDetail;
        }).orElse(null);
    }

    @Override
    public List<String> getAllConfiguredJobNames() {
        return batchBusinessStepRepository.findConfiguredJobNames();
    }

    private Optional<BusinessStepCategoryService> findByCobJobName(final String jobName) {
        return businessStepCategoryServices.stream() //
                .filter(categoryService -> categoryService.getCobJobName().equals(jobName)) //
                .findFirst();
    }

    private Optional<BusinessStepCategoryService> findByCategoryOrCobJobName(final String name) {
        return businessStepCategoryServices.stream() //
                .filter(categoryService -> categoryService.getCategory().name().equalsIgnoreCase(name)
                        || categoryService.getCobJobName().equals(name)) //
                .findFirst();
    }

    private List<BusinessStepDetail> availableBusinessSteps(final BusinessStepCategoryService categoryService) {
        return Arrays.stream(applicationContext.getBeanNamesForType(categoryService.getBusinessStepClass())) //
                .map(beanName -> (COBBusinessStep<?>) applicationContext.getBean(beanName)) //
                .map(businessStep -> {
                    final BusinessStepDetail businessStepDetail = new BusinessStepDetail();
                    businessStepDetail.setStepName(businessStep.getEnumStyledName());
                    businessStepDetail.setStepDescription(businessStep.getHumanReadableName());
                    return businessStepDetail;
                }) //
                .toList();
    }
}
