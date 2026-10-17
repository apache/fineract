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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargePaidByData;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.integrationtests.common.Utils;

public final class LoanChargeAccrualTestValidators {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(LoanTestData.DATETIME_PATTERN, Locale.ENGLISH);
    private static final String ACCRUAL_CODE = "loanTransactionType.accrual";
    private static final String ACCRUAL_ADJUSTMENT_CODE = "loanTransactionType.accrualAdjustment";
    private static final String WAIVE_CHARGES_CODE = "loanTransactionType.waiveCharges";

    private LoanChargeAccrualTestValidators() {}

    public static void verifyConfiguration(GlobalConfigurationPropertyData config, String name, String stringValue, boolean enabled,
            boolean trapDoor) {
        assertNotNull(config, "Global configuration '" + name + "' is not seeded");
        assertEquals(name, config.getName(), "configuration name");
        assertEquals(stringValue, config.getStringValue(), name + " stringValue");
        assertEquals(enabled, config.getEnabled(), name + " enabled");
        assertEquals(trapDoor, config.getTrapDoor(), name + " trapDoor");
    }

    public static List<GetLoansLoanIdTransactions> activeAccruals(GetLoansLoanIdResponse loan) {
        return activeTransactionsOfType(loan, ACCRUAL_CODE);
    }

    public static void verifyAccrualCount(GetLoansLoanIdResponse loan, int expected, String context) {
        List<GetLoansLoanIdTransactions> accruals = activeAccruals(loan);
        assertEquals(expected, accruals.size(), () -> context + ": non-reversed Accrual count. Accruals: " + describe(accruals));
    }

    public static void verifyAccrualAdjustmentCount(GetLoansLoanIdResponse loan, int expected, String context) {
        List<GetLoansLoanIdTransactions> adjustments = activeTransactionsOfType(loan, ACCRUAL_ADJUSTMENT_CODE);
        assertEquals(expected, adjustments.size(),
                () -> context + ": non-reversed Accrual Adjustment count. Adjustments: " + describe(adjustments));
    }

    public static void verifyAccrualFeeTotal(GetLoansLoanIdResponse loan, double expected) {
        List<GetLoansLoanIdTransactions> accruals = activeAccruals(loan);
        double total = accruals.stream().mapToDouble(t -> zeroIfNull(Utils.getDoubleValue(t.getFeeChargesPortion()))).sum();
        assertEquals(expected, Math.round(total * 100.0) / 100.0, () -> "Sum of Accrual fee portions. Accruals: " + describe(accruals));
    }

    public static GetLoansLoanIdTransactions verifyAccrual(GetLoansLoanIdResponse loan, double amount, String date, double feePortion,
            double penaltyPortion) {
        LocalDate expectedDate = LocalDate.parse(date, DATE_FORMATTER);
        List<GetLoansLoanIdTransactions> accruals = activeAccruals(loan);
        List<GetLoansLoanIdTransactions> matches = accruals.stream()
                .filter(t -> Objects.equals(expectedDate, t.getDate()) && Objects.equals(amount, Utils.getDoubleValue(t.getAmount()))
                        && Objects.equals(0.0, zeroIfNull(Utils.getDoubleValue(t.getInterestPortion())))
                        && Objects.equals(feePortion, zeroIfNull(Utils.getDoubleValue(t.getFeeChargesPortion())))
                        && Objects.equals(penaltyPortion, zeroIfNull(Utils.getDoubleValue(t.getPenaltyChargesPortion()))))
                .toList();
        assertEquals(1, matches.size(), () -> "Expected exactly one Accrual [amount=" + amount + ", date=" + date + ", fee=" + feePortion
                + ", penalty=" + penaltyPortion + ", interest=0.0]. Accruals: " + describe(accruals));
        return matches.get(0);
    }

    public static GetLoansLoanIdTransactions verifyChargeAccrual(GetLoansLoanIdResponse loan, Long loanChargeId, double amount, String date,
            double feePortion, double penaltyPortion, Integer installmentNumber) {
        List<GetLoansLoanIdTransactions> accruals = activeAccruals(loan);
        List<GetLoansLoanIdTransactions> mapped = accruals.stream().filter(t -> t.getLoanChargePaidByList() != null
                && t.getLoanChargePaidByList().stream().anyMatch(p -> Objects.equals(loanChargeId, p.getChargeId()))).toList();
        assertEquals(1, mapped.size(),
                () -> "Expected exactly one Accrual mapped to loan charge " + loanChargeId + ". Accruals: " + describe(accruals));
        GetLoansLoanIdTransactions accrual = mapped.get(0);
        String context = "Accrual of loan charge " + loanChargeId;
        assertEquals(LocalDate.parse(date, DATE_FORMATTER), accrual.getDate(), context + " date");
        assertEquals(amount, Utils.getDoubleValue(accrual.getAmount()), context + " amount");
        assertEquals(0.0, zeroIfNull(Utils.getDoubleValue(accrual.getInterestPortion())), context + " interest portion");
        assertEquals(feePortion, zeroIfNull(Utils.getDoubleValue(accrual.getFeeChargesPortion())), context + " fee portion");
        assertEquals(penaltyPortion, zeroIfNull(Utils.getDoubleValue(accrual.getPenaltyChargesPortion())), context + " penalty portion");
        assertEquals(1, accrual.getLoanChargePaidByList().size(), () -> context + " paid-by rows: " + accrual.getLoanChargePaidByList());
        GetLoansLoanIdLoanChargePaidByData paidBy = accrual.getLoanChargePaidByList().get(0);
        assertEquals(amount, Utils.getDoubleValue(paidBy.getAmount()), context + " paid-by amount");
        if (installmentNumber != null) {
            assertEquals(installmentNumber, paidBy.getInstallmentNumber(), context + " paid-by installmentNumber");
        }
        return accrual;
    }

    public static GetLoansLoanIdTransactions verifyChargeAccrual(GetLoansLoanIdResponse loan, Long loanChargeId, double amount,
            double feePortion, double penaltyPortion, Integer installmentNumber) {
        List<GetLoansLoanIdTransactions> accruals = activeAccruals(loan);
        GetLoansLoanIdTransactions accrual = accruals.stream()
                .filter(t -> t.getLoanChargePaidByList() != null
                        && t.getLoanChargePaidByList().stream().anyMatch(p -> Objects.equals(loanChargeId, p.getChargeId())))
                .findFirst().orElse(null);
        assertNotNull(accrual, () -> "Expected an Accrual mapped to loan charge " + loanChargeId + ". Accruals: " + describe(accruals));
        return verifyChargeAccrual(loan, loanChargeId, amount, DATE_FORMATTER.format(accrual.getDate()), feePortion, penaltyPortion,
                installmentNumber);
    }

    public static void verifyNetChargeAccruals(GetLoansLoanIdResponse loan, double expectedFee, double expectedPenalty, String context) {
        List<GetLoansLoanIdTransactions> accruals = activeAccruals(loan);
        List<GetLoansLoanIdTransactions> adjustments = activeTransactionsOfType(loan, ACCRUAL_ADJUSTMENT_CODE);
        double fee = sum(accruals, GetLoansLoanIdTransactions::getFeeChargesPortion)
                - sum(adjustments, GetLoansLoanIdTransactions::getFeeChargesPortion);
        double penalty = sum(accruals, GetLoansLoanIdTransactions::getPenaltyChargesPortion)
                - sum(adjustments, GetLoansLoanIdTransactions::getPenaltyChargesPortion);
        assertEquals(expectedFee, round(fee), () -> context + ": net accrued fee (Accruals - Accrual Adjustments). Accruals: "
                + describe(accruals) + " Adjustments: " + describe(adjustments));
        assertEquals(expectedPenalty, round(penalty), () -> context + ": net accrued penalty (Accruals - Accrual Adjustments). Accruals: "
                + describe(accruals) + " Adjustments: " + describe(adjustments));
    }

    public static double verifyInterestOnlyAccrual(GetLoansLoanIdResponse loan, String date) {
        LocalDate expectedDate = LocalDate.parse(date, DATE_FORMATTER);
        List<GetLoansLoanIdTransactions> accruals = activeAccruals(loan);
        List<GetLoansLoanIdTransactions> matches = accruals.stream()
                .filter(t -> Objects.equals(expectedDate, t.getDate())
                        && Objects.equals(0.0, zeroIfNull(Utils.getDoubleValue(t.getFeeChargesPortion())))
                        && Objects.equals(0.0, zeroIfNull(Utils.getDoubleValue(t.getPenaltyChargesPortion()))))
                .toList();
        assertEquals(1, matches.size(),
                () -> "Expected exactly one interest-only Accrual on " + date + " (fee=0.0, penalty=0.0). Accruals: " + describe(accruals));
        GetLoansLoanIdTransactions accrual = matches.get(0);
        assertEquals(Utils.getDoubleValue(accrual.getInterestPortion()), Utils.getDoubleValue(accrual.getAmount()),
                "Interest-only Accrual amount equals its interest portion");
        return zeroIfNull(Utils.getDoubleValue(accrual.getInterestPortion()));
    }

    public static Integer installmentWithFeeChargesDue(GetLoansLoanIdResponse loan, double feeChargesDue) {
        List<Integer> installments = loan.getRepaymentSchedule().getPeriods().stream()
                .filter(p -> p.getPeriod() != null && Objects.equals(feeChargesDue, zeroIfNull(Utils.getDoubleValue(p.getFeeChargesDue()))))
                .map(GetLoansLoanIdRepaymentPeriod::getPeriod).toList();
        assertEquals(1, installments.size(),
                () -> "Expected exactly one installment with fee charges due " + feeChargesDue + ": " + installments);
        return installments.get(0);
    }

    public static Integer installmentNumberOf(GetLoansLoanIdResponse loan, LocalDate date) {
        List<GetLoansLoanIdRepaymentPeriod> periods = loan.getRepaymentSchedule().getPeriods().stream()
                .filter(p -> p.getPeriod() != null && !Boolean.TRUE.equals(p.getDownPaymentPeriod())).toList();
        Integer firstPeriod = periods.stream().map(GetLoansLoanIdRepaymentPeriod::getPeriod).min(Integer::compareTo).orElseThrow();
        return periods.stream()
                .filter(p -> !date.isAfter(p.getDueDate())
                        && (date.isAfter(p.getFromDate()) || (firstPeriod.equals(p.getPeriod()) && date.isEqual(p.getFromDate()))))
                .map(GetLoansLoanIdRepaymentPeriod::getPeriod).findFirst()
                .orElseThrow(() -> new AssertionError("No repayment period contains " + date));
    }

    public static GetLoansLoanIdTransactions verifyWaiveChargesTransaction(GetLoansLoanIdResponse loan, double amount, String date,
            double feePortion, double penaltyPortion, double unrecognizedIncomePortion) {
        LocalDate expectedDate = LocalDate.parse(date, DATE_FORMATTER);
        List<GetLoansLoanIdTransactions> waivers = activeTransactionsOfType(loan, WAIVE_CHARGES_CODE).stream()
                .filter(t -> Objects.equals(expectedDate, t.getDate())).toList();
        assertEquals(1, waivers.size(), () -> "Expected exactly one Waive loan charges transaction on " + date + ": " + describe(waivers));
        GetLoansLoanIdTransactions waiver = waivers.get(0);
        assertEquals(amount, Utils.getDoubleValue(waiver.getAmount()), "Waive amount");
        assertEquals(feePortion, zeroIfNull(Utils.getDoubleValue(waiver.getFeeChargesPortion())), "Waive fee portion");
        assertEquals(penaltyPortion, zeroIfNull(Utils.getDoubleValue(waiver.getPenaltyChargesPortion())), "Waive penalty portion");
        assertEquals(unrecognizedIncomePortion, zeroIfNull(Utils.getDoubleValue(waiver.getUnrecognizedIncomePortion())),
                "Waive unrecognized income portion");
        return waiver;
    }

    public static GetLoansLoanIdChargesChargeIdResponse verifySinglePenaltyCharge(List<GetLoansLoanIdChargesChargeIdResponse> charges,
            double amount) {
        List<GetLoansLoanIdChargesChargeIdResponse> penalties = charges.stream().filter(c -> Boolean.TRUE.equals(c.getPenalty())).toList();
        assertEquals(1, penalties.size(), () -> "Expected exactly one penalty charge on the loan: " + penalties);
        GetLoansLoanIdChargesChargeIdResponse penalty = penalties.get(0);
        assertEquals(amount, penalty.getAmount(), "Penalty charge amount");
        assertNotNull(penalty.getDueDate(), "Penalty charge due date");
        return penalty;
    }

    private static List<GetLoansLoanIdTransactions> activeTransactionsOfType(GetLoansLoanIdResponse loan, String typeCode) {
        if (loan.getTransactions() == null) {
            return List.of();
        }
        return loan.getTransactions().stream().filter(t -> t.getType() != null && typeCode.equals(t.getType().getCode()))
                .filter(t -> !Boolean.TRUE.equals(t.getManuallyReversed())).toList();
    }

    private static double sum(List<GetLoansLoanIdTransactions> transactions, Function<GetLoansLoanIdTransactions, BigDecimal> portion) {
        return transactions.stream().mapToDouble(t -> zeroIfNull(Utils.getDoubleValue(portion.apply(t)))).sum();
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static Double zeroIfNull(Double value) {
        return value == null ? 0.0 : value;
    }

    private static String describe(List<GetLoansLoanIdTransactions> transactions) {
        return transactions.stream()
                .map(t -> "[id=" + t.getId() + ", date=" + t.getDate() + ", amount=" + Utils.getDoubleValue(t.getAmount()) + ", fee="
                        + Utils.getDoubleValue(t.getFeeChargesPortion()) + ", penalty=" + Utils.getDoubleValue(t.getPenaltyChargesPortion())
                        + ", interest=" + Utils.getDoubleValue(t.getInterestPortion()) + ", paidBy="
                        + (t.getLoanChargePaidByList() == null ? "[]"
                                : t.getLoanChargePaidByList().stream()
                                        .map(p -> "(charge=" + p.getChargeId() + ", amount=" + Utils.getDoubleValue(p.getAmount())
                                                + ", installment=" + p.getInstallmentNumber() + ")")
                                        .collect(Collectors.joining(",")))
                        + "]")
                .collect(Collectors.joining("; ", "{", "}"));
    }
}
