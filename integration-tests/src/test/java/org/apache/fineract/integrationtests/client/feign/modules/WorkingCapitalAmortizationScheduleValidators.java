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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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

    public static void validatePayment(final ProjectedAmortizationScheduleData schedule, final int paymentNo,
            final String expectedPaymentAmount, final String expectedBalance, final String expectedAmortizationAmount,
            final String expectedDiscountFeeBalance) {
        final ProjectedAmortizationSchedulePaymentData payment = paymentByNo(schedule, paymentNo);
        final String where = "payment " + paymentNo + ": ";
        assertEqualBigDecimal(new BigDecimal(expectedPaymentAmount), payment.getExpectedPaymentAmount(), where + "expectedPaymentAmount");
        assertEqualBigDecimal(new BigDecimal(expectedBalance), payment.getExpectedBalance(), where + "expectedBalance");
        assertEqualBigDecimal(new BigDecimal(expectedAmortizationAmount), payment.getExpectedAmortizationAmount(),
                where + "expectedAmortizationAmount");
        assertEqualBigDecimal(new BigDecimal(expectedDiscountFeeBalance), payment.getExpectedDiscountFeeBalance(),
                where + "expectedDiscountFeeBalance");
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
