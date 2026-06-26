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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import org.apache.fineract.client.models.GetLoanRescheduleRequestResponse;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostCreateRescheduleLoansResponse;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansResponse;

public final class LoanRescheduleRequestHelper {

    private LoanRescheduleRequestHelper() {}

    public static PostCreateRescheduleLoansResponse createLoanRescheduleRequest(PostCreateRescheduleLoansRequest request) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().rescheduleLoans().createRescheduleLoan(request));
    }

    public static PostUpdateRescheduleLoansResponse approveLoanRescheduleRequest(Long scheduleId,
            PostUpdateRescheduleLoansRequest request) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().rescheduleLoans().updateRescheduleLoan(scheduleId, request,
                "approve"));
    }

    public static PostUpdateRescheduleLoansResponse rejectLoanRescheduleRequest(Long scheduleId, PostUpdateRescheduleLoansRequest request) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().rescheduleLoans().updateRescheduleLoan(scheduleId, request,
                "reject"));
    }

    public static GetLoanRescheduleRequestResponse readLoanRescheduleRequest(final Long requestId, final String command) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().rescheduleLoans().retrieveOneRescheduleLoan(requestId, command));
    }

    public static List<GetLoanRescheduleRequestResponse> retrieveLoanRescheduleRequestsByLoan(final String command, final Long loanId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().rescheduleLoans().retrieveAllRescheduleLoans(command, loanId));
    }
}
