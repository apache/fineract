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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.assertj.core.data.Offset.offset;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.junit.jupiter.api.Test;

/**
 * Covers re-aging an interest bearing progressive loan whose last installment was paid off early (repayments are
 * allocated to the LAST installment) and that carries a specified due date charge which is still outstanding when the
 * loan is re-aged.
 *
 * <p>
 * The re-aged schedule is expected to consist of exactly the requested number of installments, each carrying an equal
 * share of the outstanding principal.
 * </p>
 */
@Slf4j
public class FeignLoanReAgeAfterEarlyLastInstallmentPaymentWithChargeTest extends FeignLoanTestBase {

    private static final String DISBURSEMENT_DATE = "14 April 2026";
    private static final String REPAYMENT_DATE = "20 April 2026";
    private static final String RE_AGE_DATE = "22 October 2026";
    private static final String RE_AGE_START_DATE = "22 November 2026";
    private static final LocalDate RE_AGE_LOCAL_DATE = LocalDate.of(2026, 10, 22);
    private static final double PRINCIPAL = 600.0;
    private static final double INTEREST_RATE = 9.99;
    private static final double CHARGE_AMOUNT = 10.0;
    private static final int NUMBER_OF_REPAYMENTS = 6;
    private static final int RE_AGE_INSTALLMENTS = 3;

    /**
     * The charge is due after maturity, so it lives on an additional (N+1) installment when the loan is re-aged.
     */
    @Test
    void reAgeWithChargeDueAfterMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 200.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            addCharge(loanId, false, CHARGE_AMOUNT, "20 October 2026");

            reAgeLoan(loanId);

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * The charge is due on the last unpaid installment, before maturity.
     */
    @Test
    void reAgeWithChargeDueBeforeMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 100.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            addCharge(loanId, false, CHARGE_AMOUNT, "01 September 2026");

            reAgeLoan(loanId);

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Same as {@link #reAgeWithChargeDueBeforeMaturity()}, except that the charge is waived and a new charge is added
     * for the same due date before re-aging.
     */
    @Test
    void reAgeWithWaivedAndRecreatedChargeDueBeforeMaturity() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 100.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            Long loanChargeId = addCharge(loanId, false, CHARGE_AMOUNT, "01 September 2026");
            waiveLoanCharge(loanId, loanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));
            addCharge(loanId, false, CHARGE_AMOUNT, "01 September 2026");

            reAgeLoan(loanId);

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Control case: the same flow without any charge.
     */
    @Test
    void reAgeWithoutCharge() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 200.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            reAgeLoan(loanId);

            assertReAgedSchedule(loanId, true);
        });
    }

    /**
     * Default interest handling, with the last two installments paid off early.
     */
    @Test
    void reAgeWithDefaultInterestHandlingAfterTwoInstallmentsPaidEarly() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 200.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            reAgeLoanWithDefaultInterestHandling(loanId);

            assertReAgedSchedule(loanId, false);
        });
    }

    /**
     * Default interest handling, with the last installment paid off early.
     */
    @Test
    void reAgeWithDefaultInterestHandlingAfterLastInstallmentPaidEarly() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long loanId = createDisbursedLoan();

            updateBusinessDate(REPAYMENT_DATE);
            addRepaymentForLoan(loanId, 100.0, REPAYMENT_DATE);

            updateBusinessDate(RE_AGE_DATE);
            reAgeLoanWithDefaultInterestHandling(loanId);

            assertReAgedSchedule(loanId, false);
        });
    }

    private Long createDisbursedLoan() {
        Long clientId = createClient(DISBURSEMENT_DATE);
        Long productId = createLoanProduct(payMonthlyInterestBearingProduct());

        Long loanId = applyAndApproveProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL, INTEREST_RATE, NUMBER_OF_REPAYMENTS,
                null);
        disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), DISBURSEMENT_DATE);

        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);
        return loanId;
    }

    /**
     * Progressive, interest bearing product with daily interest recalculation and no down payment. Repayments are
     * allocated to the LAST installment, so an early repayment pays off the final installment first.
     */
    private PostLoanProductsRequest payMonthlyInterestBearingProduct() {
        return customizeProduct(create4IProgressive(), p -> p//
                .numberOfRepayments(NUMBER_OF_REPAYMENTS)//
                .interestRatePerPeriod(INTEREST_RATE)//
                .currencyCode("USD")//
                .enableDownPayment(false)//
                .paymentAllocation(List.of(LoanRequestBuilders.paymentAllocation("DEFAULT", "LAST_INSTALLMENT"))));
    }

    private void reAgeLoan(Long loanId) {
        reAgeLoan(loanId, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, RE_AGE_START_DATE, RE_AGE_INSTALLMENTS,
                "EQUAL_AMORTIZATION_PAYABLE_INTEREST");
    }

    private void reAgeLoanWithDefaultInterestHandling(Long loanId) {
        reAgeLoan(loanId, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, RE_AGE_START_DATE, RE_AGE_INSTALLMENTS, null);
    }

    private void assertReAgedSchedule(Long loanId, boolean equalPrincipal) {
        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        verifyLoanStatus(loan, GetLoansLoanIdStatus::getActive);
        assertNotNull(loan.getRepaymentSchedule());
        List<GetLoansLoanIdRepaymentPeriod> periods = Objects.requireNonNull(loan.getRepaymentSchedule().getPeriods());

        List<GetLoansLoanIdRepaymentPeriod> pastPeriods = periods.stream()//
                .filter(p -> p.getPeriod() != null && !p.getDueDate().isAfter(RE_AGE_LOCAL_DATE))//
                .toList();
        List<GetLoansLoanIdRepaymentPeriod> reAgedPeriods = periods.stream()//
                .filter(p -> p.getPeriod() != null && p.getDueDate().isAfter(RE_AGE_LOCAL_DATE))//
                .toList();

        for (GetLoansLoanIdRepaymentPeriod period : pastPeriods) {
            assertThat(period.getTotalOutstandingForPeriod())
                    .as("Installment %s due before the re-age must have nothing left outstanding", period.getPeriod())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        assertThat(reAgedPeriods).as("Re-aged schedule must contain exactly the requested number of installments")
                .hasSize(RE_AGE_INSTALLMENTS);

        BigDecimal firstPrincipal = reAgedPeriods.getFirst().getPrincipalDue();
        for (GetLoansLoanIdRepaymentPeriod period : reAgedPeriods) {
            assertThat(period.getPrincipalDue()).as("Principal due of re-aged installment %s", period.getPeriod())
                    .isGreaterThan(BigDecimal.ZERO);
            if (equalPrincipal) {
                assertThat(period.getPrincipalDue()).as("Principal is equally amortized over re-aged installment %s", period.getPeriod())
                        .isCloseTo(firstPrincipal, offset(new BigDecimal("0.02")));
            }
        }

        BigDecimal totalPrincipalDue = periods.stream()//
                .filter(p -> p.getPeriod() != null)//
                .map(GetLoansLoanIdRepaymentPeriod::getPrincipalDue)//
                .filter(Objects::nonNull)//
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalPrincipalDue).as("Total principal due must still equal the disbursed amount")
                .isEqualByComparingTo(BigDecimal.valueOf(PRINCIPAL));
    }
}
