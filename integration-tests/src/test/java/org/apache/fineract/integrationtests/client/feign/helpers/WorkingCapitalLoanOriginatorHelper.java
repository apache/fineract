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

import org.apache.fineract.client.feign.services.LoanOriginatorsApi;
import org.apache.fineract.client.feign.services.WorkingCapitalLoanOriginatorsApi;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.LoanOriginatorMappingResponse;
import org.apache.fineract.client.models.LoanOriginatorsResponse;
import org.apache.fineract.client.models.PostLoanOriginatorsRequest;
import org.apache.fineract.client.models.PostLoanOriginatorsResponse;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

public class WorkingCapitalLoanOriginatorHelper {

    private static LoanOriginatorsApi api() {
        return FineractFeignClientHelper.getFineractFeignClient().loanOriginators();
    }

    private static WorkingCapitalLoanOriginatorsApi workingCapitalLoanOriginatorsApi() {
        return FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoanOriginators();
    }

    public Long createOriginator(final String externalId, final String name) {
        PostLoanOriginatorsRequest request = new PostLoanOriginatorsRequest();
        request.setExternalId(externalId);
        request.setName(name);
        request.setStatus("ACTIVE");

        PostLoanOriginatorsResponse response = FeignCalls.ok(() -> api().createLoanOriginator(request));
        return response.getResourceId();
    }

    public void deleteOriginator(final Long originatorId) {
        FeignCalls.ok(() -> api().deleteLoanOriginator(originatorId));
    }

    public LoanOriginatorMappingResponse attachOriginatorToWorkingCapitalLoan(final Long loanId, final Long originatorId) {
        return FeignCalls.ok(() -> workingCapitalLoanOriginatorsApi().attachOriginatorToWorkingCapitalLoan(loanId, originatorId));
    }

    public LoanOriginatorMappingResponse detachOriginatorFromWorkingCapitalLoan(final Long loanId, final Long originatorId) {
        return FeignCalls.ok(() -> workingCapitalLoanOriginatorsApi().detachOriginatorFromWorkingCapitalLoan(loanId, originatorId));
    }

    public LoanOriginatorsResponse retrieveOriginatorsByWorkingCapitalLoanId(final Long loanId) {
        return FeignCalls.ok(() -> workingCapitalLoanOriginatorsApi().retrieveOriginatorsByWorkingCapitalLoanId(loanId));
    }
}
