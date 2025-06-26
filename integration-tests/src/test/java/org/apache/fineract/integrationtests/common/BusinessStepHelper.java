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
package org.apache.fineract.integrationtests.common;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.BusinessStepRequest;
import org.apache.fineract.client.models.JobBusinessStepConfigData;
import org.apache.fineract.client.util.Calls;

public class BusinessStepHelper {

    public BusinessStepHelper() {}

    public BusinessStepsSnapshot getConfigurationSnapshot(String jobName) {
        JobBusinessStepConfigData businessConfig = Calls
                .ok(FineractClientHelper.getFineractClient().businessStepConfiguration.retrieveAllConfiguredBusinessStep(jobName));
        return new BusinessStepsSnapshot(jobName, businessConfig.getBusinessSteps());
    }

    public void updateSteps(String jobName, String... steps) {
        long order = 0;
        List<BusinessStep> stepList = new ArrayList<>();
        for (String step : steps) {
            order++;
            BusinessStep businessStep = new BusinessStep();
            businessStep.stepName(step);
            businessStep.setOrder(order);
            stepList.add(businessStep);
        }
        Calls.ok(FineractClientHelper.getFineractClient().businessStepConfiguration.updateJobBusinessStepConfig(jobName,
                new BusinessStepRequest().businessSteps(stepList)));
    }

    @Getter
    @AllArgsConstructor
    public static class BusinessStepsSnapshot {

        private String jobName;
        private List<BusinessStep> businessSteps;

        public void restore() {
            Calls.ok(FineractClientHelper.getFineractClient().businessStepConfiguration.updateJobBusinessStepConfig(jobName,
                    new BusinessStepRequest().businessSteps(businessSteps)));
        }
    }
}
