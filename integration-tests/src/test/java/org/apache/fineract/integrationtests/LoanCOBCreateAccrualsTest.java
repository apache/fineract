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
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.Test;

@Slf4j
public class LoanCOBCreateAccrualsTest extends BaseLoanIntegrationTest {

    private PostClientsResponse client;
    private PostLoanProductsResponse loanProduct;

    private void setup() {
        if (loanProduct == null) {
            loanProduct = loanProductHelper.createLoanProduct(create4IProgressive() //
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
                    .isInterestRecalculationEnabled(false).minPrincipal(1.0d).maxPrincipal(100000.0d).minInterestRatePerPeriod(0.0d)
                    .maxInterestRatePerPeriod(108.0d)
                    .paymentAllocation(List.of(createDefaultPaymentAllocation(FuturePaymentAllocationRule.LAST_INSTALLMENT)))
                    .repaymentFrequencyType(RepaymentFrequencyType.DAYS_L).repaymentEvery(30).minNumberOfRepayments(1)
                    .maxNumberOfRepayments(12).numberOfRepayments(1).currencyCode("USD"));
        }

        if (client == null) {
            ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
            client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        }
    }

    // Update business date to 10/20/2024
    // Create and disburse loan - €100 - 10/20/2024
    // Update business date to 10/21/2024 and run COB
    // Update business date to 10/22/2024
    // Create a repayment - €102 - 10/22/2024
    // Update business date to 10/23/2024 and run COB
    // Create a charge (Penalty - NSF) with amount less than the overpaid part - 10/23/2024
    // Update business date to 10/24/2024 and run COB
    @Test
    public void chargeAmountLessThanOverpaidAmount() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        setup();
        runAt("20 October 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "20 October 2024", 100.0, 0.0,
                    1, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "20 October 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));
        });
        runAt("21 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanIdRef.get());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));
        });
        runAt("22 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));

            loanTransactionHelper.makeLoanRepayment("22 October 2024", 102.0F, loanId.intValue());

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyPaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0, 0, 100);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 0, 0, 2.0, false));

        });
        runAt("23 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyPaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0, 0, 100);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 0, 0, 2.0, false));

            addCharge(loanId, true, 1.5d, "23 October 2024");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 11, 20), 100.0, 100.0, 0, 0, 0, 0, 1.5, 1.5, 0, 0, 0, 0, 101.5, 0);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(1.5d, "Accrual", "23 October 2024", 0, 0.0, 0.0, 0.0, 1.5, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 1.5, 0, 0.5, false));

        });
        runAt("24 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 11, 20), 100.0, 100.0, 0, 0, 0, 0, 1.5, 1.5, 0, 0, 0, 0, 101.5, 0);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(1.5d, "Accrual", "23 October 2024", 0, 0.0, 0.0, 0.0, 1.5, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 1.5, 0, 0.5, false));
        });
    }

    // Update business date to 10/20/2024
    // Create and disburse loan - €100 - 10/20/2024
    // Update business date to 10/21/2024 and run COB
    // Update business date to 10/22/2024
    // Create a repayment - €102 - 10/22/2024
    // Update business date to 10/23/2024 and run COB
    // Create a charge (Penalty - NSF) with amount equals to the overpaid part - 10/23/2024
    // Update business date to 10/24/2024 and run COB
    @Test
    public void chargeAmountEqualsToOverpaidAmount() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        setup();
        runAt("20 October 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "20 October 2024", 100.0, 0.0,
                    1, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "20 October 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));
        });
        runAt("21 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanIdRef.get());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));
        });
        runAt("22 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));

            loanTransactionHelper.makeLoanRepayment("22 October 2024", 102.0F, loanId.intValue());

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyPaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0, 0, 100);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 0, 0, 2.0, false));

        });
        runAt("23 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyPaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0, 0, 100);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 0, 0, 2.0, false));

            addCharge(loanId, true, 2.0d, "23 October 2024");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 11, 20), 100.0, 100.0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 102, 0);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(2.0d, "Accrual", "23 October 2024", 0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 2, 0, 0, false));

        });
        runAt("24 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 11, 20), 100.0, 100.0, 0, 0, 0, 0, 2, 2, 0, 0, 0, 0, 102, 0);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(2.0d, "Accrual", "23 October 2024", 0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 2, 0, 0, false));
        });
    }

    // Update business date to 10/20/2024
    // Create and disburse loan - €100 - 10/20/2024
    // Update business date to 10/21/2024 and run COB
    // Update business date to 10/22/2024
    // Create a repayment - €102 - 10/22/2024
    // Update business date to 10/23/2024 and run COB
    // Create a charge (Penalty - NSF) with amount greater to the overpaid part - 10/23/2024
    // Update business date to 10/24/2024 and run COB
    @Test
    public void chargeAmountIsGreaterThanOverpaidAmount() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        setup();
        runAt("20 October 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "20 October 2024", 100.0, 0.0,
                    1, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "20 October 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));
        });
        runAt("21 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanIdRef.get());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));
        });
        runAt("22 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));

            loanTransactionHelper.makeLoanRepayment("22 October 2024", 102.0F, loanId.intValue());

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyPaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0, 0, 100);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 0, 0, 2.0, false));

        });
        runAt("23 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyPaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0, 0, 100);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 0, 0, 2.0, false));

            addCharge(loanId, true, 5.0d, "23 October 2024");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 11, 20), 100.0, 100.0, 0, 0, 0, 0, 5, 2, 3, 0, 0, 0, 102, 0);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 2, 0, 0, false));

        });
        runAt("24 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 11, 20), 100.0, 100.0, 0, 0, 0, 0, 5, 2, 3, 0, 0, 0, 102, 0);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(5.0d, "Accrual", "23 October 2024", 0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0, false), //
                    transaction(102.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 2, 0, 0, false));

        });
    }

    // Update business date to 10/20/2024
    // Create and disburse loan - €100 - 10/20/2024
    // Update business date to 10/21/2024 and run COB
    // Update business date to 10/22/2024
    // Create a repayment - €100 - 10/22/2024
    // Update business date to 10/23/2024 and run COB
    // Create a charge (Penalty - NSF) - 10/23/2024
    // Update business date to 10/24/2024 and run COB
    @Test
    public void chargeForObligationMetLoan() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        setup();
        runAt("20 October 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "20 October 2024", 100.0, 0.0,
                    1, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "20 October 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));
        });
        runAt("21 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanIdRef.get());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));
        });
        runAt("22 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0);

            verifyTransactions(loanId, transaction(100.0d, "Disbursement", "20 October 2024"));

            loanTransactionHelper.makeLoanRepayment("22 October 2024", 100.0F, loanId.intValue());

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyPaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0, 0, 100);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(100.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 0, 0, 0.0, false));

        });
        runAt("23 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyPaidRepaymentPeriod(loanDetails, 1, "20 November 2024", 100, 0, 0, 0, 0, 100);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(100.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 0, 0, 0.0, false));

            addCharge(loanId, true, 5.0d, "23 October 2024");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 11, 20), 100.0, 100.0, 0, 0, 0, 0, 5, 0, 5, 0, 0, 0, 100, 0);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(100.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 0, 0, 0, false));

        });
        runAt("24 October 2024", () -> {
            Long loanId = loanIdRef.get();

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 11, 20), 100.0, 100.0, 0, 0, 0, 0, 5, 0, 5, 0, 0, 0, 100, 0);

            verifyTransactions(loanId, //
                    transaction(100.0d, "Disbursement", "20 October 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(5.0d, "Accrual", "23 October 2024", 0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0, false), //
                    transaction(100.0d, "Repayment", "22 October 2024", 0, 100.0, 0, 0, 0, 0, 0, false));

        });
    }
}
