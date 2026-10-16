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
package org.apache.fineract.integrationtests.client.feign.modules;

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.assertEqualBigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;

public final class WorkingCapitalAmortizationScheduleValidators {

    private WorkingCapitalAmortizationScheduleValidators() {}

    public static ProjectedAmortizationSchedulePaymentData paymentByNo(final ProjectedAmortizationScheduleData schedule,
            final int paymentNo) {
        assertNotNull(schedule.getPayments(), "the amortization schedule carries no payments");
        return schedule.getPayments().stream().filter(payment -> payment.getPaymentNo() == paymentNo).findFirst()
                .orElseThrow(() -> new AssertionError(
                        "no payment numbered " + paymentNo + " in a schedule of " + schedule.getPayments().size() + " rows"));
    }

    /**
     * One schedule row: due date, expected payment, balance, amortization and deferred discount fee. The disbursement
     * row (0) amortizes nothing, so it is checked with a {@code null} amortization.
     */
    public static void validatePayment(final ProjectedAmortizationScheduleData schedule, final int paymentNo, final String expectedDate,
            final String expectedPaymentAmount, final String expectedBalance, final String expectedAmortizationAmount,
            final String expectedDiscountFeeBalance) {
        final ProjectedAmortizationSchedulePaymentData payment = paymentByNo(schedule, paymentNo);
        final String where = "payment " + paymentNo + ": ";
        assertEquals(LocalDate.parse(expectedDate), payment.getPaymentDate(), where + "paymentDate");
        assertEqualBigDecimal(new BigDecimal(expectedPaymentAmount), payment.getExpectedPaymentAmount(), where + "expectedPaymentAmount");
        assertEqualBigDecimal(new BigDecimal(expectedBalance), payment.getExpectedBalance(), where + "expectedBalance");
        if (expectedAmortizationAmount == null) {
            assertNull(payment.getExpectedAmortizationAmount(), where + "expectedAmortizationAmount");
        } else {
            assertEqualBigDecimal(new BigDecimal(expectedAmortizationAmount), payment.getExpectedAmortizationAmount(),
                    where + "expectedAmortizationAmount");
        }
        assertEqualBigDecimal(new BigDecimal(expectedDiscountFeeBalance), payment.getExpectedDiscountFeeBalance(),
                where + "expectedDiscountFeeBalance");
    }

    public static void validatePaymentDate(final ProjectedAmortizationScheduleData schedule, final int paymentNo,
            final String expectedDate) {
        assertEquals(LocalDate.parse(expectedDate), paymentByNo(schedule, paymentNo).getPaymentDate(),
                "payment " + paymentNo + ": paymentDate");
    }

    /**
     * Summary shape of a projected schedule: the regular payment, the number of payments (term) and the row count,
     * which includes the disbursement row numbered 0.
     */
    public static void validateScheduleShape(final ProjectedAmortizationScheduleData schedule, final String expectedPaymentAmount,
            final int expectedTerm) {
        assertEqualBigDecimal(new BigDecimal(expectedPaymentAmount), schedule.getExpectedPaymentAmount(), "schedule expectedPaymentAmount");
        assertEquals(expectedTerm, schedule.getOriginalPaymentNumber(), "schedule originalPaymentNumber (term)");
        assertNotNull(schedule.getPayments(), "the amortization schedule carries no payments");
        assertEquals(expectedTerm + 1, schedule.getPayments().size(), "schedule rows (the term plus the disbursement row 0)");
    }

    public static void validateActuals(final ProjectedAmortizationScheduleData schedule, final int paymentNo,
            final String expectedActualPaymentAmount, final String expectedActualBalance) {
        final ProjectedAmortizationSchedulePaymentData payment = paymentByNo(schedule, paymentNo);
        final String where = "payment " + paymentNo + ": ";
        assertEqualBigDecimal(new BigDecimal(expectedActualPaymentAmount), payment.getActualPaymentAmount(), where + "actualPaymentAmount");
        if (expectedActualBalance != null) {
            assertEqualBigDecimal(new BigDecimal(expectedActualBalance), payment.getActualBalance(), where + "actualBalance");
        }
    }

    public static void assertActualsNotYetKnown(final ProjectedAmortizationScheduleData schedule, final int paymentNo) {
        final ProjectedAmortizationSchedulePaymentData payment = paymentByNo(schedule, paymentNo);
        final String where = "payment " + paymentNo + " (" + payment.getPaymentDate() + "): ";
        assertNull(payment.getActualPaymentAmount(), where + "actualPaymentAmount must be null for a period not yet due");
        assertNull(payment.getActualBalance(), where + "actualBalance must be null for a period not yet due");
        assertNull(payment.getActualDiscountFeeBalance(), where + "actualDiscountFeeBalance must be null for a period not yet due");
    }

    public static void assertNoRowDated(final ProjectedAmortizationScheduleData schedule, final String date) {
        final LocalDate unexpected = LocalDate.parse(date);
        assertFalse(instalments(schedule).stream().anyMatch(payment -> unexpected.equals(payment.getPaymentDate())),
                "no schedule row may be dated " + date + " — a payment between due dates belongs to the period that contains it");
    }

    /**
     * Two schedules that must be the same row for row: date, expected payment, balance and deferred fee.
     */
    public static void assertSameExpectedRows(final ProjectedAmortizationScheduleData expected,
            final ProjectedAmortizationScheduleData actual) {
        assertNotNull(expected.getPayments(), "the reference schedule carries no payments");
        assertNotNull(actual.getPayments(), "the compared schedule carries no payments");
        assertEquals(expected.getPayments().size(), actual.getPayments().size(), "row count");
        for (final ProjectedAmortizationSchedulePaymentData reference : expected.getPayments()) {
            final ProjectedAmortizationSchedulePaymentData row = paymentByNo(actual, reference.getPaymentNo());
            final String where = "payment " + reference.getPaymentNo() + ": ";
            assertEquals(reference.getPaymentDate(), row.getPaymentDate(), where + "paymentDate");
            assertEqualBigDecimal(reference.getExpectedPaymentAmount(), row.getExpectedPaymentAmount(), where + "expectedPaymentAmount");
            assertEqualBigDecimal(reference.getExpectedBalance(), row.getExpectedBalance(), where + "expectedBalance");
            assertEqualBigDecimal(reference.getExpectedDiscountFeeBalance(), row.getExpectedDiscountFeeBalance(),
                    where + "expectedDiscountFeeBalance");
        }
    }

    /**
     * {@link #assertSameExpectedRows} plus the actuals each row has recorded so far, unknown actuals included.
     */
    public static void assertSameRows(final ProjectedAmortizationScheduleData expected, final ProjectedAmortizationScheduleData actual) {
        assertSameExpectedRows(expected, actual);
        for (final ProjectedAmortizationSchedulePaymentData reference : expected.getPayments()) {
            final ProjectedAmortizationSchedulePaymentData row = paymentByNo(actual, reference.getPaymentNo());
            final String where = "payment " + reference.getPaymentNo() + " (" + reference.getPaymentDate() + "): ";
            assertSameAmount(reference.getActualPaymentAmount(), row.getActualPaymentAmount(), where + "actualPaymentAmount");
            assertSameAmount(reference.getActualBalance(), row.getActualBalance(), where + "actualBalance");
            assertSameAmount(reference.getActualDiscountFeeBalance(), row.getActualDiscountFeeBalance(),
                    where + "actualDiscountFeeBalance");
            assertSameAmount(reference.getActualAmortizationAmount(), row.getActualAmortizationAmount(),
                    where + "actualAmortizationAmount");
        }
    }

    private static void assertSameAmount(final BigDecimal expected, final BigDecimal actual, final String message) {
        if (expected == null) {
            assertNull(actual, message + " — expected no value");
        } else {
            assertEqualBigDecimal(expected, actual, message);
        }
    }

    public static List<ProjectedAmortizationSchedulePaymentData> instalments(final ProjectedAmortizationScheduleData schedule) {
        assertNotNull(schedule.getPayments(), "the amortization schedule carries no payments");
        return schedule.getPayments().stream().filter(payment -> payment.getPaymentNo() > 0)
                .sorted((left, right) -> Integer.compare(left.getPaymentNo(), right.getPaymentNo())).toList();
    }

    public static BigDecimal totalExpectedAmortization(final ProjectedAmortizationScheduleData schedule) {
        return instalments(schedule).stream().map(ProjectedAmortizationSchedulePaymentData::getExpectedAmortizationAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static void assertNoNegativeAmounts(final ProjectedAmortizationScheduleData schedule) {
        for (final ProjectedAmortizationSchedulePaymentData payment : instalments(schedule)) {
            final String where = "payment " + payment.getPaymentNo() + ": ";
            assertNonNegative(where + "expectedPaymentAmount", payment.getExpectedPaymentAmount());
            assertNonNegative(where + "expectedBalance", payment.getExpectedBalance());
            assertNonNegative(where + "expectedAmortizationAmount", payment.getExpectedAmortizationAmount());
            assertNonNegative(where + "expectedDiscountFeeBalance", payment.getExpectedDiscountFeeBalance());
        }
    }

    private static void assertNonNegative(final String label, final BigDecimal value) {
        if (value != null) {
            assertTrue(value.signum() >= 0, label + " went negative: " + value);
        }
    }
}
