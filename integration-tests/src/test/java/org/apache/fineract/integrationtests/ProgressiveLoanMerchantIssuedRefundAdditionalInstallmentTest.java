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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RepaymentFrequencyType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.SupportedInterestRefundTypesItem;
import org.junit.jupiter.api.Test;

/**
 * Covers Merchant Issued Refund on a progressive, interest bearing, down payment enabled loan whose persisted schedule
 * carries an "additional" installment which is no longer beyond the maturity date.
 * <p>
 * The interest schedule model is generated without down payment and additional installments (see
 * {@code ProgressiveEMICalculator#generateInstallmentInterestScheduleModel}), so an additional installment has no
 * repayment period to be resolved against. The payment allocation used to hand such an installment to the EMI
 * calculator whenever the MERCHANT_ISSUED_REFUND rule uses LAST_INSTALLMENT, because the only guard in place was the
 * maturity date check - and that check stops holding as soon as a reschedule or a re-age extends the schedule past the
 * additional installment. The refund then failed with an unwrapped {@code NoSuchElementException}.
 */
public class ProgressiveLoanMerchantIssuedRefundAdditionalInstallmentTest extends FeignLoanTestBase {

    /** Must match the currency of {@code create4IProgressive()}, otherwise charges cannot be attached to the loan. */
    private static final String CURRENCY = "EUR";
    private static final Double PRINCIPAL = 1000.0;
    private static final Double INTEREST_RATE = 9.99;
    private static final int NUMBER_OF_REPAYMENTS = 4;

    /**
     * The additional installment is created by a specified due date charge falling after maturity, and is then brought
     * below the maturity date by a reschedule which pushes the remaining installments six months out. The refund is
     * dated before the additional installment's due date, so the LAST_INSTALLMENT rule selects it.
     */
    @Test
    public void merchantIssuedRefundWithAdditionalInstallmentBelowMaturityDateIsProcessed() {
        AtomicLong createdLoanId = new AtomicLong();

        runAt("01 January 2021", () -> {
            Long loanId = createDownPaymentProgressiveLoanWithMerchantIssuedRefundRule("01 January 2021");
            createdLoanId.set(loanId);

            // Down payment plus four monthly installments, so the loan matures on 01 May 2021
            checkMaturityDates(loanId, LocalDate.of(2021, 5, 1), LocalDate.of(2021, 5, 1));
        });

        runAt("15 January 2021", () -> {
            long loanId = createdLoanId.get();

            // A specified due date charge after maturity appends an additional installment carrying only the charge
            PostChargesResponse charge = createCharge(25.0, CURRENCY);
            addLoanCharge(loanId, charge.getResourceId(), "01 July 2021", 25.0);

            assertEquals(LocalDate.of(2021, 7, 1), lastPeriodDueDate(loanId),
                    "The specified due date charge should have appended an additional installment due on 01 July 2021");
            checkMaturityDates(loanId, LocalDate.of(2021, 5, 1), LocalDate.of(2021, 5, 1));
        });

        runAt("20 January 2021", () -> {
            long loanId = createdLoanId.get();

            // Breathing space: the 01 March installment moves to 01 September, dragging the maturity date to 01
            // November 2021, which is past the additional installment's due date of 01 July 2021
            createAndApproveReschedule(loanId, "20 January 2021", "01 March 2021", "01 September 2021");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getTimeline().getActualMaturityDate().isAfter(LocalDate.of(2021, 7, 1)),
                    "The reschedule should have pushed the maturity date past the additional installment, but it is "
                            + loanDetails.getTimeline().getActualMaturityDate());
        });

        runAt("01 June 2021", () -> {
            long loanId = createdLoanId.get();

            // Before the fix this failed with NoSuchElementException raised by ProgressiveEMICalculator#getDueAmounts,
            // surfacing as an internal server error instead of a processed refund
            PostLoansLoanIdTransactionsResponse response = makeLoanRepayment(loanId, "MerchantIssuedRefund", "01 June 2021", 100.0);

            assertNotNull(response);
            assertNotNull(response.getResourceId());
            assertNotNull(getLoanDetails(loanId).getRepaymentSchedule());
        });
    }

    /**
     * The sequence reported from production: a down payment enabled progressive loan gets a breathing space, is then
     * re-aged, and finally receives a Merchant Issued Refund.
     */
    @Test
    public void merchantIssuedRefundAfterRescheduleAndReAgeIsProcessed() {
        AtomicLong createdLoanId = new AtomicLong();

        runAt("21 April 2021", () -> {
            Long loanId = createDownPaymentProgressiveLoanWithMerchantIssuedRefundRule("21 April 2021");
            createdLoanId.set(loanId);
        });

        runAt("21 May 2021", () -> {
            long loanId = createdLoanId.get();

            // Breathing space of two periods: the 21 June installment is pushed to 21 August
            createAndApproveReschedule(loanId, "21 May 2021", "21 June 2021", "21 August 2021");
        });

        runAt("10 June 2021", () -> {
            long loanId = createdLoanId.get();

            reAgeLoan(loanId, RepaymentFrequencyType.MONTHS_STRING, 1, "10 July 2021", 10, null);

            PostLoansLoanIdTransactionsResponse response = makeLoanRepayment(loanId, "MerchantIssuedRefund", "10 June 2021", 150.0);

            assertNotNull(response);
            assertNotNull(response.getResourceId());
            assertNotNull(getLoanDetails(loanId).getRepaymentSchedule());
        });
    }

    private Long createDownPaymentProgressiveLoanWithMerchantIssuedRefundRule(final String date) {
        Long clientId = createClient();

        PostLoanProductsRequest product = create4IProgressive() //
                .enableDownPayment(true) //
                .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25)) //
                .enableAutoRepaymentForDownPayment(true) //
                .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND) //
                .paymentAllocation(List.of(LoanRequestBuilders.defaultPaymentAllocation(),
                        LoanRequestBuilders.paymentAllocation("MERCHANT_ISSUED_REFUND", "LAST_INSTALLMENT")));

        Long loanProductId = createLoanProduct(product);
        Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, date, PRINCIPAL, INTEREST_RATE, NUMBER_OF_REPAYMENTS, null);
        assertNotNull(loanId);

        disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), date);
        return loanId;
    }

    private LocalDate lastPeriodDueDate(final long loanId) {
        List<GetLoansLoanIdRepaymentPeriod> periods = getLoanDetails(loanId).getRepaymentSchedule().getPeriods();
        return periods.stream().map(GetLoansLoanIdRepaymentPeriod::getDueDate).max(Comparator.naturalOrder()).orElseThrow();
    }
}
