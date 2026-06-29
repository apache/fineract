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

import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class ProgressiveLoanDisbursementChargeTest extends FeignLoanTestBase {

    @Test
    public void testProgressiveLoanDisbursementChargeIsIncludedInTotalRepaymentExpected() {
        runAt("01 June 2024", () -> {
            // 1. Create a client
            Long clientId = createClient();

            // 2. Create a percentage-based disbursement charge (10% of amount) in EUR
            Long chargeId = createDisbursementPercentageCharge(10.0, "EUR");

            // 3. Create a progressive loan product
            final Long loanProductId = createLoanProduct(create4IProgressive());

            // 4. Apply for a loan with 1000 EUR principal
            Long loanId = applyForLoan(applyLP2ProgressiveLoanRequest(clientId, loanProductId, "01 June 2024", 1000.0, 10.0, 4, null));

            // 5. Add the disbursement charge to the loan account before approval
            addDisbursementCharge(loanId, chargeId, 10.0);

            // 6. Approve the loan
            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 June 2024"));

            // 7. Disburse the loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 June 2024");

            // 8. Retrieve the loan details
            final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // 9. Verify the disbursement charge is calculated correctly (10% of 1000 = 100 EUR)
            // It should be completely due at disbursement (period 0)
            Double expectedFeeAmount = 100.0;
            Double totalFeeChargesCharged = Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalFeeChargesCharged());
            Assertions.assertEquals(expectedFeeAmount, totalFeeChargesCharged,
                    "Total fee charges charged should match the disbursement fee.");

            // 10. Verify that total repayment expected includes the disbursement fee without double counting
            Double totalPrincipalExpected = Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected());
            Double totalInterestCharged = Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalInterestCharged());
            Double expectedTotalRepayment = totalPrincipalExpected + totalInterestCharged + expectedFeeAmount;

            Double actualTotalRepaymentExpected = Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalRepaymentExpected());
            Assertions.assertEquals(expectedTotalRepayment, actualTotalRepaymentExpected,
                    "Total repayment expected is missing or double-counting the disbursement fee.");

            // 11. Check the disbursement period (period 0)
            Double feeChargesDueAtDisbursement = Utils
                    .getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(0).getFeeChargesDue());
            Assertions.assertEquals(expectedFeeAmount, feeChargesDueAtDisbursement, "Disbursement period (0) should have the fee due.");
        });
    }
}
