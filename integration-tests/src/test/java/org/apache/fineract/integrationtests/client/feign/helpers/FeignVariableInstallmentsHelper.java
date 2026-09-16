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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostLoansLoanIdSchedulePeriod;
import org.apache.fineract.client.models.PostLoansLoanIdScheduleRequest;
import org.apache.fineract.client.models.PostLoansLoanIdScheduleResponse;

/**
 * Feign counterpart of the legacy {@code VariableIntallmentsTransactionHelper}: the loan term variations of a variable
 * installment loan.
 */
public class FeignVariableInstallmentsHelper {

    private static final String CALCULATE_COMMAND = "calculateLoanSchedule";
    private static final String ADD_VARIATIONS_COMMAND = "addVariations";
    private static final String DELETE_VARIATIONS_COMMAND = "deleteVariations";

    private final FineractFeignClient fineractClient;

    public FeignVariableInstallmentsHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    /**
     * Calculates - without persisting - the schedule the given variations would produce.
     */
    public List<PostLoansLoanIdSchedulePeriod> validateVariations(Long loanId, PostLoansLoanIdScheduleRequest request) {
        return ok(() -> fineractClient.loanRescheduling().handleCommandsLoanSchedule(loanId, request, CALCULATE_COMMAND)).getPeriods();
    }

    /** Applies the variations to the loan's repayment schedule. */
    public PostLoansLoanIdScheduleResponse submitVariations(Long loanId, PostLoansLoanIdScheduleRequest request) {
        return ok(() -> fineractClient.loanRescheduling().handleCommandsLoanSchedule(loanId, request, ADD_VARIATIONS_COMMAND));
    }

    /** Removes every term variation from the loan's repayment schedule. */
    public PostLoansLoanIdScheduleResponse deleteVariations(Long loanId) {
        return ok(() -> fineractClient.loanRescheduling().handleCommandsLoanSchedule(loanId, new PostLoansLoanIdScheduleRequest(),
                DELETE_VARIATIONS_COMMAND));
    }
}
