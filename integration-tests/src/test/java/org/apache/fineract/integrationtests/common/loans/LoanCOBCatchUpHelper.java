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
package org.apache.fineract.integrationtests.common.loans;

import java.time.LocalDate;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetOldestCOBProcessedLoanResponse;
import org.apache.fineract.client.models.IsCatchUpRunningResponse;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.jetbrains.annotations.NotNull;
import retrofit2.Response;

@Slf4j
public class LoanCOBCatchUpHelper extends IntegrationTest {

    public LoanCOBCatchUpHelper() {}

    public boolean isLoanCOBCatchUpRunning() {
        Response<IsCatchUpRunningResponse> response = executeGetLoanCatchUpStatus();
        return Boolean.TRUE.equals(Objects.requireNonNull(response.body()).getIsCatchUpRunning());
    }

    public boolean isLoanCOBCatchUpFinishedFor(@NotNull LocalDate cobBusinessDate) {
        Response<IsCatchUpRunningResponse> response = executeGetLoanCatchUpStatus();
        IsCatchUpRunningResponse isCatchUpRunningResponse = Objects.requireNonNull(response.body());
        GetOldestCOBProcessedLoanResponse getOldestCOBProcessedLoanResponse = executeRetrieveOldestCOBProcessedLoan();

        return !Boolean.TRUE.equals(isCatchUpRunningResponse.getIsCatchUpRunning())
                && cobBusinessDate.equals(getOldestCOBProcessedLoanResponse.getCobProcessedDate());
    }

    public Response<Void> executeLoanCOBCatchUp() {
        return okR(fineract().loanCobCatchUpApi.executeLoanCOBCatchUp());
    }

    public GetOldestCOBProcessedLoanResponse executeRetrieveOldestCOBProcessedLoan() {
        return ok(fineract().loanCobCatchUpApi.getOldestCOBProcessedLoan());
    }

    public Response<IsCatchUpRunningResponse> executeGetLoanCatchUpStatus() {
        return okR(fineract().loanCobCatchUpApi.isCatchUpRunning());
    }

}
