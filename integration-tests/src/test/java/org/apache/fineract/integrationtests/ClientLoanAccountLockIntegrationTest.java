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
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClientLoanAccountLockIntegrationTest extends BaseLoanIntegrationTest {

    @Test
    public void checkRetrieveLockedLoanAccountsList() {
        final Long clientId = ClientHelper.addClientAsPerson(null, ClientHelper.LEGALFORM_ID_PERSON, null).getResourceId();

        final Long loanProductId = loanTransactionHelper.createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()) //
                .getResourceId();

        final PostLoansResponse loanResponse = loanTransactionHelper
                .applyLoan(applyLoanRequest(clientId, loanProductId, "20 September 2011", 12000.0, 4));
        final Long loanId = loanResponse.getLoanId();

        loanTransactionHelper.approveLoan(loanId, new PostLoansLoanIdRequest() //
                .approvedOnDate("20 September 2011") //
                .dateFormat(Utils.DATE_FORMAT) //
                .locale("en"));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);

        loanTransactionHelper.disburseLoan(loanId, "20 September 2011", 12000.0);
        verifyLoanStatus(loanId, LoanStatus.ACTIVE);

        Calls.ok(FineractClientHelper.getFineractClient().legacy //
                .placeLockOnLoanAccount(loanId, "LOAN_INLINE_COB_PROCESSING", new LockRequest().error("Sample error")));

        LoanAccountLockResponseDTO lockResponse = Calls
                .ok(FineractClientHelper.getFineractClient().loanAccountLockApi.retrieveLockedAccounts(0, 1000));
        Assertions.assertTrue(lockResponse.getContent().size() > 0);
        Assertions.assertTrue(lockResponse.getContent().stream().anyMatch(lock -> lock.getLoanId().equals(loanId)));
    }
}
