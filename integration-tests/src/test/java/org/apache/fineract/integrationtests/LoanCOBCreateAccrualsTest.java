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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.CreditAllocationOrder;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

    @Test
    public void testEarlyRepaymentAccruals() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        setup();
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());

        runAt("20 December 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "20 December 2024",
                    430.0, 26.0, 6, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 January 2025", 67.88, 0, 0, 9.32);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "20 February 2025", 69.35, 0, 0, 7.85);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "20 March 2025", 70.86, 0, 0, 6.34);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "20 April 2025", 72.39, 0, 0, 4.81);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "20 May 2025", 73.96, 0, 0, 3.24);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "20 June 2025", 75.56, 0, 0, 1.64);

            verifyTransactions(loanId, transaction(430.0d, "Disbursement", "20 December 2024"));
            executeInlineCOB(loanId);
        });
        runAt("30 December 2024", () -> {
            Long loanId = loanIdRef.get();
            loanTransactionHelper.makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("30 December 2024").locale("en").transactionAmount(200.0));
        });
        runAt("22 March 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            // No unexpected big accruals or any accrual adjustments
            Assertions.assertTrue(
                    loanDetails.getTransactions().stream().noneMatch(t -> (t.getType().getAccrual() && t.getAmount().doubleValue() > 0.31)
                            || "loanTransactionType.accrualAdjustment".equals(t.getType().getCode())));

            // Accruals around installment due dates are as expected
            validateTransactionsExist(loanDetails, //
                    transaction(0.17, "Accrual", "20 January 2025", 0.0, 0.0, 0.17, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.16, "Accrual", "21 January 2025", 0.0, 0.0, 0.16, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.16, "Accrual", "20 February 2025", 0.0, 0.0, 0.16, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.18, "Accrual", "21 February 2025", 0.0, 0.0, 0.18, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.18, "Accrual", "20 March 2025", 0.0, 0.0, 0.18, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.16, "Accrual", "21 March 2025", 0.0, 0.0, 0.16, 0.0, 0.0, 0.0, 0.0) //
            );
        });

    }

    @Test
    public void testInterestRecognitionOnDisbursementDateTrue() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        setup();
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().interestRecognitionOnDisbursementDate(true));

        runAt("20 December 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "20 December 2024",
                    430.0, 26.0, 6, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 January 2025", 67.88, 0, 0, 9.32);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "20 February 2025", 69.35, 0, 0, 7.85);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "20 March 2025", 70.86, 0, 0, 6.34);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "20 April 2025", 72.39, 0, 0, 4.81);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "20 May 2025", 73.96, 0, 0, 3.24);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "20 June 2025", 75.56, 0, 0, 1.64);

            verifyTransactions(loanId, transaction(430.0d, "Disbursement", "20 December 2024"));
            executeInlineCOB(loanId);
        });
        // disbursement date is included
        runAt("21 December 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(430.0d, "Disbursement", "20 December 2024", 430.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(0.30d, "Accrual", "20 December 2024", 0.0, 0.0, 0.3, 0.0, 0.0, 0.0, 0.0, false));
        });
        // last installment due date is excluded
        runAt("21 June 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            Assertions.assertTrue(loanDetails.getTransactions().stream()
                    .noneMatch(t -> t.getDate().equals(LocalDate.of(2025, 6, 20)) && t.getType().getAccrual()));
        });
    }

    @Test
    public void testInterestRecognitionOnDisbursementDateFalse() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        setup();
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().interestRecognitionOnDisbursementDate(false));

        runAt("20 December 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "20 December 2024",
                    430.0, 26.0, 6, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "20 January 2025", 67.88, 0, 0, 9.32);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "20 February 2025", 69.35, 0, 0, 7.85);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "20 March 2025", 70.86, 0, 0, 6.34);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "20 April 2025", 72.39, 0, 0, 4.81);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "20 May 2025", 73.96, 0, 0, 3.24);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "20 June 2025", 75.56, 0, 0, 1.64);

            verifyTransactions(loanId, transaction(430.0d, "Disbursement", "20 December 2024"));
            executeInlineCOB(loanId);
        });
        runAt("21 December 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(430.0d, "Disbursement", "20 December 2024", 430.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false));
        });
    }

    @Test
    public void testProgressiveChargeBackNoInterestRecalculation() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<Long> repaymentIdRef = new AtomicReference<>();

        setup();
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(false)
                        .creditAllocation(chargebackCreditAllocationOrders(List.of("PRINCIPAL", "PENALTY", "FEE", "INTEREST")))
                        .currencyCode("USD"));

        runAt("20 December 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "20 December 2024",
                    430.0, 26.0, 6, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024");
            executeInlineCOB(loanId);
        });
        runAt("20 January 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addCharge(loanId, true, 5.0d, "20 January 2025");
            Long repaymentId = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "20 January 2025", 82.20).getResourceId();
            repaymentIdRef.set(repaymentId);
        });
        runAt("2 February 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addChargebackForLoan(loanId, repaymentIdRef.get(), 82.20);
        });
        runAt("20 February 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateTransactionsExist(loanDetails, //
                    transaction(0.26, "Accrual", "01 February 2025", 0.0, 0.0, 0.26, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.25, "Accrual", "02 February 2025", 0.0, 0.0, 0.25, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.25, "Accrual", "03 February 2025", 0.0, 0.0, 0.25, 0.0, 0.0, 0.0, 0.0)); //
        });
        runAt("23 February 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateTransactionsExist(loanDetails, //
                    transaction(0.25, "Accrual", "19 February 2025", 0.0, 0.0, 0.25, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.26, "Accrual", "20 February 2025", 0.0, 0.0, 0.26, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.23, "Accrual", "21 February 2025", 0.0, 0.0, 0.23, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.22, "Accrual", "22 February 2025", 0.0, 0.0, 0.22, 0.0, 0.0, 0.0, 0.0)); //
        });
    }

    @Test
    public void testProgressiveChargeBackInterestRecalculation() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<Long> repaymentIdRef = new AtomicReference<>();

        setup();
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(true)
                        .creditAllocation(chargebackCreditAllocationOrders(List.of("PRINCIPAL", "PENALTY", "FEE", "INTEREST")))
                        .currencyCode("USD"));

        runAt("20 December 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "20 December 2024",
                    430.0, 26.0, 6, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024");
            executeInlineCOB(loanId);
        });
        runAt("20 January 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addCharge(loanId, true, 5.0d, "20 January 2025");
            Long repaymentId = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "20 January 2025", 82.20).getResourceId();
            repaymentIdRef.set(repaymentId);
        });
        runAt("2 February 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addChargebackForLoan(loanId, repaymentIdRef.get(), 82.20);
        });
        runAt("20 February 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateTransactionsExist(loanDetails, //
                    transaction(0.26, "Accrual", "01 February 2025", 0.0, 0.0, 0.26, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.25, "Accrual", "02 February 2025", 0.0, 0.0, 0.25, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.30, "Accrual", "03 February 2025", 0.0, 0.0, 0.30, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.30, "Accrual", "04 February 2025", 0.0, 0.0, 0.30, 0.0, 0.0, 0.0, 0.0)); //
        });
        runAt("23 February 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateTransactionsExist(loanDetails, //
                    transaction(0.30, "Accrual", "19 February 2025", 0.0, 0.0, 0.30, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.30, "Accrual", "20 February 2025", 0.0, 0.0, 0.30, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.33, "Accrual", "21 February 2025", 0.0, 0.0, 0.33, 0.0, 0.0, 0.0, 0.0), //
                    transaction(0.34, "Accrual", "22 February 2025", 0.0, 0.0, 0.34, 0.0, 0.0, 0.0, 0.0)); //
        });
    }

    @Test
    public void testRunCOBJobAfterUndoDisbursement() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        setup();
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableAccrualActivityPosting(true));

        runAt("1 April 2025", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 March 2025", 430.0,
                    26.0, 6, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(430), "1 March 2025");

            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateTransactionsExist(loanDetails, //
                    transaction(9.02, "Accrual", "31 March 2025", 0.0, 0.0, 9.02, 0.0, 0.0, 0.0, 0.0));
            assertEquals(LocalDate.of(2025, 3, 31), loanDetails.getLastClosedBusinessDate());

            undoDisbursement(loanId.intValue());
            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            assertNull(loanDetails.getLastClosedBusinessDate());

            disburseLoan(loanIdRef.get(), BigDecimal.valueOf(430), "2 March 2025");
            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            assertNull(loanDetails.getLastClosedBusinessDate());
        });

        runAt("2 April 2025", () -> {
            executeInlineCOB(loanIdRef.get());
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanIdRef.get());
            validateTransactionsExist(loanDetails, //
                    transaction(9.02, "Accrual", "01 April 2025", 0.0, 0.0, 9.02, 0.0, 0.0, 0.0, 0.0));
            assertEquals(LocalDate.of(2025, 4, 1), loanDetails.getLastClosedBusinessDate());
        });
    }

    private List<CreditAllocationData> chargebackCreditAllocationOrders(List<String> allocationIds) {
        List<CreditAllocationOrder> creditAllocationOrders = new ArrayList<>(allocationIds.size());
        for (int i = 0; i < allocationIds.size(); i++) {
            String allocationId = allocationIds.get(i);
            creditAllocationOrders.add(new CreditAllocationOrder().order(i + 1).creditAllocationRule(allocationId));
        }
        return List.of(new CreditAllocationData().transactionType("CHARGEBACK").creditAllocationOrder(creditAllocationOrders));
    }

    private void validateTransactionsExist(GetLoansLoanIdResponse loanDetails, TransactionExt... transactions) {
        Arrays.stream(transactions).forEach(tr -> {
            boolean found = loanDetails.getTransactions().stream()
                    .anyMatch(item -> Objects.equals(Utils.getDoubleValue(item.getAmount()), tr.amount) //
                            && Objects.equals(item.getType().getValue(), tr.type) //
                            && Objects.equals(item.getDate(), LocalDate.parse(tr.date, dateTimeFormatter)) //
                            && Objects.equals(Utils.getDoubleValue(item.getOutstandingLoanBalance()), tr.outstandingPrincipal) //
                            && Objects.equals(Utils.getDoubleValue(item.getPrincipalPortion()), tr.principalPortion) //
                            && Objects.equals(Utils.getDoubleValue(item.getInterestPortion()), tr.interestPortion) //
                            && Objects.equals(Utils.getDoubleValue(item.getFeeChargesPortion()), tr.feePortion) //
                            && Objects.equals(Utils.getDoubleValue(item.getPenaltyChargesPortion()), tr.penaltyPortion) //
                            && Objects.equals(Utils.getDoubleValue(item.getOverpaymentPortion()), tr.overpaymentPortion) //
                            && Objects.equals(Utils.getDoubleValue(item.getUnrecognizedIncomePortion()), tr.unrecognizedPortion) //
            );
            Assertions.assertTrue(found, "Required transaction not found: " + tr + " on loan " + loanDetails.getId());
        });
    }

    @Test
    public void shouldSkipInterestRecalculationWhenNoOverdueInstallments() {
        setup();
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("01 April 2025", () -> {
            // Create and disburse a loan with a single installment due in the future
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "01 April 2025", 100.0, 0.0, 1,
                    null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(100), "01 April 2025");
        });
        runAt("02 April 2025", () -> {
            Long loanId = loanIdRef.get();
            // No overdue installments: installment due in the future
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            // There should be only the disbursement transaction, no accrual/interest recalculation
            Assertions.assertEquals(1, loanDetails.getTransactions().size(),
                    "No interest recalculation/accrual should occur if there are no overdue installments");
            Assertions.assertEquals("Disbursement", loanDetails.getTransactions().get(0).getType().getValue());
        });
    }
}
