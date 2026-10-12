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
package org.apache.fineract.test.stepdef.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.BusinessStepDetail;
import org.apache.fineract.client.models.ConfiguredJobNamesDTO;
import org.apache.fineract.client.models.JobBusinessStepConfigData;
import org.apache.fineract.client.models.JobBusinessStepDetail;
import org.apache.fineract.test.helper.WorkFlowJobHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class BusinessStepConfigurationStepDef extends AbstractStepDef {

    @Autowired
    private WorkFlowJobHelper workFlowJobHelper;

    @Then("Admin checks that configured business jobs contain {string}")
    public void checkConfiguredBusinessJobsContain(String jobName) {
        ConfiguredJobNamesDTO response = workFlowJobHelper.getConfiguredBusinessJobs();
        List<String> businessJobs = response.getBusinessJobs();
        log.debug("Configured business jobs: {}", businessJobs);
        assertThat(businessJobs)//
                .as("Configured business jobs should contain '%s' but got: %s", jobName, businessJobs)//
                .contains(jobName);
    }

    @Then("Admin verifies configured business steps for {string} match:")
    public void verifyConfiguredBusinessStepsMatch(String jobName, DataTable dataTable) {
        JobBusinessStepConfigData response = workFlowJobHelper.getConfiguredWorkflowSteps(jobName);
        List<BusinessStep> actualSteps = response.getBusinessSteps();
        log.debug("Configured business steps for '{}': {}", jobName, actualSteps);

        List<Map<String, String>> expectedRows = dataTable.asMaps(String.class, String.class);
        assertThat(actualSteps)//
                .as("Configured steps count for '%s' — expected %d but got %d: %s", jobName, expectedRows.size(), actualSteps.size(),
                        actualSteps)//
                .hasSize(expectedRows.size());

        List<BusinessStep> sortedActual = actualSteps.stream()//
                .sorted(Comparator.comparingLong(BusinessStep::getOrder))//
                .toList();

        for (int i = 0; i < expectedRows.size(); i++) {
            Map<String, String> expected = expectedRows.get(i);
            BusinessStep actual = sortedActual.get(i);
            String expectedStepName = expected.get("stepName");
            long expectedOrder = Long.parseLong(expected.get("order"));
            assertThat(actual.getStepName())//
                    .as("Step at position %d for job '%s' — expected name '%s' but got '%s'", i, jobName, expectedStepName,
                            actual.getStepName())//
                    .isEqualTo(expectedStepName);
            assertThat(actual.getOrder())//
                    .as("Step '%s' for job '%s' — expected order %d but got %d", expectedStepName, jobName, expectedOrder,
                            actual.getOrder())//
                    .isEqualTo(expectedOrder);
        }
    }

    @Then("Admin verifies available business steps for {string} contain:")
    public void verifyAvailableBusinessStepsContain(String jobName, DataTable dataTable) {
        JobBusinessStepDetail response = workFlowJobHelper.getAvailableWorkflowSteps(jobName);
        List<String> actualStepNames = response.getAvailableBusinessSteps().stream()//
                .map(BusinessStepDetail::getStepName)//
                .toList();
        log.debug("Available business steps for '{}': {}", jobName, actualStepNames);

        List<Map<String, String>> expectedRows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : expectedRows) {
            String expectedStepName = row.get("stepName");
            assertThat(actualStepNames)//
                    .as("Available steps for '%s' should contain '%s' but got: %s", jobName, expectedStepName, actualStepNames)//
                    .contains(expectedStepName);
        }
    }

    @When("Admin updates business steps for {string} with:")
    public void updateBusinessSteps(String jobName, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        List<BusinessStep> steps = rows.stream()//
                .map(row -> new BusinessStep()//
                        .stepName(row.get("stepName"))//
                        .order(Long.parseLong(row.get("order"))))//
                .toList();
        log.debug("Updating business steps for '{}': {}", jobName, steps);
        workFlowJobHelper.updateWorkflowSteps(jobName, steps);
    }

    @Then("Admin fails to update business steps for {string} with invalid step {string}")
    public void updateBusinessStepsFailsWithInvalidStep(String jobName, String invalidStepName) {
        List<BusinessStep> steps = List.of(new BusinessStep().stepName(invalidStepName).order(1L));
        try {
            workFlowJobHelper.updateWorkflowSteps(jobName, steps);
            throw new AssertionError(
                    "Expected update to fail for invalid step '%s' on job '%s' but it succeeded".formatted(invalidStepName, jobName));
        } catch (CallFailedRuntimeException e) {
            log.debug("Business step update correctly rejected for '{}' on job '{}': status={}", invalidStepName, jobName, e.getStatus());
            assertThat(e.getStatus())//
                    .as("Expected HTTP 400 for invalid step '%s' on job '%s' but got %d", invalidStepName, jobName, e.getStatus())//
                    .isEqualTo(400);
        }
    }
}
