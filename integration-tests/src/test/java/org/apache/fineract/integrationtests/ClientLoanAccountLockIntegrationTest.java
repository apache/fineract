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
package org.apache.fineract.integrationtests;

import org.apache.fineract.client.models.LoanAccountLockResponseDTO;
import org.apache.fineract.client.models.LockRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClientLoanAccountLockIntegrationTest extends FeignLoanTestBase {

    @Test
    public void checkRetrieveLockedLoanAccountsList() {
        Long clientId = createClient();
        Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());

        Long loanId = applyAndApproveLoan(clientId, loanProductId, "20 September 2011", 12000.0, 4);
        verifyLoanStatus(loanId, LoanStatus.APPROVED);

        disburseLoan(loanId, "20 September 2011", 12000.0);
        verifyLoanStatus(loanId, LoanStatus.ACTIVE);

        executeVoid(() -> FineractFeignClientHelper.getFineractFeignClient().loanAccountLock().placeLockOnLoanAccount(loanId,
                "LOAN_INLINE_COB_PROCESSING", new LockRequest().error("Sample error")));

        LoanAccountLockResponseDTO lockResponse = ok(
                () -> FineractFeignClientHelper.getFineractFeignClient().loanAccountLock().retrieveLockedAccounts(0, 1000));
        Assertions.assertTrue(lockResponse.getContent().size() > 0);
        Assertions.assertTrue(lockResponse.getContent().stream().anyMatch(lock -> lock.getLoanId().equals(loanId)));
    }
}
