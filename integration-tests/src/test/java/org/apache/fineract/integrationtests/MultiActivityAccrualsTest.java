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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MultiActivityAccrualsTest extends FeignLoanTestBase {

    private static final String disbursementDate = "9 August 2024";
    private static final String repaymentDate = "9 December 2024";
    private static final Double fullRepaymentAmount = 700.0;
    private static final Integer expectedNumberOfAccruals = 1;
    private static final Integer expectedNumberOfActivityAccruals = 4;

    private static Long loanId;

    @BeforeEach
    public void setup() {
        Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        Long loanProductId = createLoanProduct(create4IProgressive().currencyCode("USD").enableAccrualActivityPosting(true));

        loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, disbursementDate, 600.0, 9.99, 4, null);
        Assertions.assertNotNull(loanId);
        disburseLoan(loanId, BigDecimal.valueOf(600), disbursementDate);
    }

    @Test
    public void testMultiAccrualActivityCreated() {
        runAt(repaymentDate, () -> {

            makeLoanRepayment(loanId, "Repayment", repaymentDate, fullRepaymentAmount);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            List<GetLoansLoanIdTransactions> accrualTransactional = loanDetails.getTransactions().stream()
                    .filter(transaction -> transaction.getType().getCode().equals("loanTransactionType.accrual")).toList();

            List<GetLoansLoanIdTransactions> accrualActivityTransactional = loanDetails.getTransactions().stream()
                    .filter(transaction -> transaction.getType().getCode().equals("loanTransactionType.accrualActivity")).toList();

            assertFalse(accrualTransactional.isEmpty());
            assertEquals(expectedNumberOfAccruals, accrualTransactional.size());
            assertFalse(accrualActivityTransactional.isEmpty());
            assertEquals(expectedNumberOfActivityAccruals, accrualActivityTransactional.size());

            verifyTransactions(loanId, //
                    transaction(600.0, "Disbursement", "09 August 2024", 600.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(19.98, "Accrual", "09 December 2024", 0, 0, 19.98, 0, 0, 0.0, 0.0),
                    transaction(4.99, "Accrual Activity", "09 September 2024", 0, 0, 4.99, 0, 0, 0.0, 0.0),
                    transaction(5.0, "Accrual Activity", "09 October 2024", 0, 0, 5.0, 0, 0, 0.0, 0.0),
                    transaction(4.99, "Accrual Activity", "09 November 2024", 0, 0, 4.99, 0, 0, 0.0, 0.0),
                    transaction(5.0, "Accrual Activity", "09 December 2024", 0, 0, 5.0, 0, 0, 0.0, 0.0),
                    transaction(700.00, "Repayment", "09 December 2024", 0, 600.00, 19.98, 0, 0, 0.0, 80.02));
        });
    }
}
