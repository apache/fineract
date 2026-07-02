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

import java.util.List;
import org.apache.fineract.client.feign.services.WorkingCapitalLoanBreachActionsApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.PostWorkingCapitalLoansBreachActionRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachActionData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

public class WorkingCapitalLoanBreachActionHelper {

    private static WorkingCapitalLoanBreachActionsApi api() {
        return FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanBreachActions();
    }

    public void pause(final Long loanId, final String startDate, final String endDate) {
        FeignCalls.ok(() -> api().createBreachAction(loanId, pauseRequest(startDate, endDate)));
    }

    public void resume(final Long loanId, final String startDate) {
        FeignCalls.ok(() -> api().createBreachAction(loanId, resumeRequest(startDate)));
    }

    public CallFailedRuntimeException resumeExpectingFailure(final Long loanId, final String startDate) {
        return FeignCalls.fail(() -> api().createBreachAction(loanId, resumeRequest(startDate)));
    }

    public List<WorkingCapitalLoanBreachActionData> retrieveBreachActions(final Long loanId) {
        return FeignCalls.ok(() -> api().retrieveBreachActions(loanId));
    }

    private PostWorkingCapitalLoansBreachActionRequest pauseRequest(final String startDate, final String endDate) {
        final PostWorkingCapitalLoansBreachActionRequest request = new PostWorkingCapitalLoansBreachActionRequest();
        request.setAction("pause");
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setDateFormat("yyyy-MM-dd");
        request.setLocale("en");
        return request;
    }

    private PostWorkingCapitalLoansBreachActionRequest resumeRequest(final String startDate) {
        final PostWorkingCapitalLoansBreachActionRequest request = new PostWorkingCapitalLoansBreachActionRequest();
        request.setAction("resume");
        request.setStartDate(startDate);
        request.setDateFormat("yyyy-MM-dd");
        request.setLocale("en");
        return request;
    }
}
