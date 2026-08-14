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
package org.apache.fineract.integrationtests.common.workingcapitalloanbreach;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.feign.services.WorkingCapitalBreachApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachData;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachTemplateResponse;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

public class WorkingCapitalLoanBreachHelper {

    private static WorkingCapitalBreachApi api() {
        return FineractFeignClientHelper.getFineractFeignClient().workingCapitalBreaches();
    }

    public Long create(final WorkingCapitalLoanBreachRequest request) {
        final CommandProcessingResult response = FeignCalls.ok(() -> api().createWorkingCapitalLoanBreach(request));
        return response.getResourceId();
    }

    public Long update(final Long breachId, final WorkingCapitalLoanBreachRequest request) {
        final CommandProcessingResult response = FeignCalls.ok(() -> api().updateWorkingCapitalLoanBreach(breachId, request));
        return response.getResourceId();
    }

    public Long delete(final Long breachId) {
        final CommandProcessingResult response = FeignCalls.ok(() -> api().deleteWorkingCapitalLoanBreach(breachId));
        return response.getResourceId();
    }

    public WorkingCapitalLoanBreachTemplateResponse retrieveTemplate() {
        return FeignCalls.ok(() -> api().retrieveWorkingCapitalLoanBreachTemplate());
    }

    public List<WorkingCapitalLoanBreachData> retrieveAll() {
        return FeignCalls.ok(() -> api().retrieveAllWorkingCapitalLoanBreaches());
    }

    public WorkingCapitalLoanBreachData retrieveOne(final Long breachId) {
        return FeignCalls.ok(() -> api().retrieveWorkingCapitalLoanBreach(breachId));
    }

    public CallFailedRuntimeException runCreateExpectingFailure(final WorkingCapitalLoanBreachRequest request) {
        return FeignCalls.fail(() -> api().createWorkingCapitalLoanBreach(request));
    }

    public CallFailedRuntimeException runUpdateExpectingFailure(final Long breachId, final WorkingCapitalLoanBreachRequest request) {
        return FeignCalls.fail(() -> api().updateWorkingCapitalLoanBreach(breachId, request));
    }

    public CallFailedRuntimeException runRetrieveOneExpectingFailure(final Long breachId) {
        return FeignCalls.fail(() -> api().retrieveWorkingCapitalLoanBreach(breachId));
    }

    public CallFailedRuntimeException runDeleteExpectingFailure(final Long breachId) {
        return FeignCalls.fail(() -> api().deleteWorkingCapitalLoanBreach(breachId));
    }

    public WorkingCapitalLoanBreachData retrieveWorkingCapitalLoanBreach(final Long breachId) {
        return FeignCalls.ok(() -> api().retrieveWorkingCapitalLoanBreach(breachId));
    }

    public WorkingCapitalLoanBreachRequest createBreachRequest(final String name, final Integer breachFrequency,
            final String breachFrequencyType, final String breachAmountCalculationType, final BigDecimal breachAmount) {
        return new WorkingCapitalLoanBreachRequest().name(name).breachFrequency(breachFrequency).breachFrequencyType(breachFrequencyType)
                .breachAmountCalculationType(breachAmountCalculationType).breachAmount(breachAmount);
    }
}
