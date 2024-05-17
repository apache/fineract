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

import static org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder.DEFAULT_STRATEGY;

import java.math.BigDecimal;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanBalloonRepaymentTest extends BaseLoanIntegrationTest {

    @Test
    public void loanAccountWithBalloonRepaymentAmount() {
        final String operationDate = "01 March 2024";
        runAt(operationDate, () -> {
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            final PostLoanProductsResponse loanProductResponse = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), operationDate, 1000.0, 1,
                    (req) -> req.transactionProcessingStrategyCode(DEFAULT_STRATEGY).numberOfRepayments(6).repaymentEvery(1)
                            .interestRatePerPeriod(BigDecimal.valueOf(12.0)).interestCalculationPeriodType(1).interestType(0)
                            .repaymentFrequencyType(2).loanTermFrequency(6).loanTermFrequencyType(2).balloonRepaymentAmount(800.0));

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), operationDate);

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2024"), //
                    installment(25.0, false, "01 April 2024"), //
                    installment(28.0, false, "01 May 2024"), //
                    installment(31.36, false, "01 June 2024"), //
                    installment(35.12, false, "01 July 2024"), //
                    installment(39.34, false, "01 August 2024"), //
                    installment(841.18, false, "01 September 2024") //
            );
        });
    }
}
