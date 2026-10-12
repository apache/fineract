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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;

public final class LoanTestValidators {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(LoanTestData.DATETIME_PATTERN, Locale.ENGLISH);

    private LoanTestValidators() {}

    public static void verifyTransactions(GetLoansLoanIdResponse loanDetails, LoanTestData.Transaction... transactions) {
        if (transactions == null || transactions.length == 0) {
            assertTrue(loanDetails.getTransactions() == null || loanDetails.getTransactions().isEmpty(), "No transaction is expected");
            return;
        }
        assertNotNull(loanDetails.getTransactions());
        assertEquals(transactions.length, loanDetails.getTransactions().size(), "Number of transactions");

        Arrays.stream(transactions).forEach(tr -> {
            boolean found = loanDetails.getTransactions().stream()
                    .anyMatch(item -> Objects.equals(Utils.getDoubleValue(item.getAmount()), tr.amount)
                            && Objects.equals(item.getType().getValue(), tr.type)
                            && Objects.equals(item.getDate(), LocalDate.parse(tr.date, DATE_FORMATTER)));

            assertTrue(found, "Required transaction not found: " + tr);

            if (tr.reversed != null) {
                GetLoansLoanIdTransactions tx = loanDetails.getTransactions().stream()
                        .filter(item -> Objects.equals(Utils.getDoubleValue(item.getAmount()), tr.amount)
                                && Objects.equals(item.getType().getValue(), tr.type)
                                && Objects.equals(item.getDate(), LocalDate.parse(tr.date, DATE_FORMATTER)))
                        .findFirst().orElseThrow();
                assertEquals(tr.reversed, tx.getManuallyReversed(), "Transaction is not reversed: " + tr);
            }
        });
    }

    public static void verifyTransactions(GetLoansLoanIdResponse loanDetails, LoanTestData.TransactionExt... transactions) {
        if (transactions == null || transactions.length == 0) {
            assertNull(loanDetails.getTransactions(), "No transaction is expected");
            return;
        }
        assertNotNull(loanDetails.getTransactions());
        assertEquals(transactions.length, loanDetails.getTransactions().size(), "Number of transactions");

        Arrays.stream(transactions).forEach(tr -> {
            List<GetLoansLoanIdTransactions> transactionsByDate = loanDetails.getTransactions().stream()
                    .filter(item -> Objects.equals(item.getDate(), LocalDate.parse(tr.date, DATE_FORMATTER))).toList();

            if (transactionsByDate.isEmpty()) {
                Assertions.fail("No transactions found for date " + tr.date);
                return;
            }

            // The API omits manuallyReversed for non-reversed transactions, so null and false are the same state.
            boolean found = transactionsByDate.stream()
                    .anyMatch(item -> Objects.equals(Utils.getDoubleValue(item.getAmount()), tr.amount)
                            && Objects.equals(item.getType().getValue(), tr.type)
                            && Objects.equals(Utils.getDoubleValue(item.getOutstandingLoanBalance()), tr.outstandingPrincipal)
                            && Objects.equals(Utils.getDoubleValue(item.getPrincipalPortion()), tr.principalPortion)
                            && Objects.equals(Utils.getDoubleValue(item.getInterestPortion()), tr.interestPortion)
                            && Objects.equals(Utils.getDoubleValue(item.getFeeChargesPortion()), tr.feePortion)
                            && Objects.equals(Utils.getDoubleValue(item.getPenaltyChargesPortion()), tr.penaltyPortion)
                            && Objects.equals(Utils.getDoubleValue(item.getOverpaymentPortion()), tr.overpaymentPortion)
                            && Objects.equals(Utils.getDoubleValue(item.getUnrecognizedIncomePortion()), tr.unrecognizedPortion)
                            && Boolean.TRUE.equals(item.getManuallyReversed()) == Boolean.TRUE.equals(tr.reversed));

            if (!found) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Required transaction not found: ").append(tr);
                errorMessage.append("\nTransactions found for date ").append(tr.date).append(":");
                for (int i = 0; i < transactionsByDate.size(); i++) {
                    GetLoansLoanIdTransactions item = transactionsByDate.get(i);
                    errorMessage.append("\n  Transaction ").append(i + 1).append(": ");
                    errorMessage.append("amount=").append(Utils.getDoubleValue(item.getAmount()));
                    errorMessage.append(", type=").append(item.getType().getValue());
                    errorMessage.append(", outstandingPrincipal=").append(Utils.getDoubleValue(item.getOutstandingLoanBalance()));
                    errorMessage.append(", principalPortion=").append(Utils.getDoubleValue(item.getPrincipalPortion()));
                    errorMessage.append(", interestPortion=").append(Utils.getDoubleValue(item.getInterestPortion()));
                    errorMessage.append(", feePortion=").append(Utils.getDoubleValue(item.getFeeChargesPortion()));
                    errorMessage.append(", penaltyPortion=").append(Utils.getDoubleValue(item.getPenaltyChargesPortion()));
                    errorMessage.append(", unrecognizedPortion=").append(Utils.getDoubleValue(item.getUnrecognizedIncomePortion()));
                    errorMessage.append(", overpaymentPortion=").append(Utils.getDoubleValue(item.getOverpaymentPortion()));
                    errorMessage.append(", reversed=").append(item.getManuallyReversed() != null ? item.getManuallyReversed() : false);
                }
                Assertions.fail(errorMessage.toString());
            }
        });
    }

    public static void verifyRepaymentSchedule(GetLoansLoanIdResponse loanResponse, LoanTestData.Installment... installments) {
        assertNotNull(loanResponse.getRepaymentSchedule());
        assertNotNull(loanResponse.getRepaymentSchedule().getPeriods());
        assertEquals(installments.length, loanResponse.getRepaymentSchedule().getPeriods().size(),
                "Expected installments are not matching with the installments configured on the loan");

        int installmentNumber = 0;
        for (int i = 0; i < installments.length; i++) {
            GetLoansLoanIdRepaymentPeriod period = loanResponse.getRepaymentSchedule().getPeriods().get(i);
            Double principalDue = Utils.getDoubleValue(period.getPrincipalDue());
            Double amount = installments[i].principalAmount;

            if (installments[i].completed == null) {
                assertEquals(amount, Utils.getDoubleValue(period.getPrincipalLoanBalanceOutstanding()),
                        "%d. installment's principal due is different, expected: %.2f, actual: %.2f".formatted(i, amount,
                                Utils.getDoubleValue(period.getPrincipalLoanBalanceOutstanding())));
            } else {
                assertEquals(amount, principalDue,
                        "%d. installment's principal due is different, expected: %.2f, actual: %.2f".formatted(i, amount, principalDue));

                Double interestAmount = installments[i].interestAmount;
                Double interestDue = Utils.getDoubleValue(period.getInterestDue());
                if (interestAmount != null) {
                    assertEquals(interestAmount, interestDue, "%d. installment's interest due is different, expected: %.2f, actual: %.2f"
                            .formatted(i, interestAmount, interestDue));
                }

                Double feeAmount = installments[i].feeAmount;
                Double feeDue = Utils.getDoubleValue(period.getFeeChargesDue());
                if (feeAmount != null) {
                    assertEquals(feeAmount, feeDue,
                            "%d. installment's fee charges due is different, expected: %.2f, actual: %.2f".formatted(i, feeAmount, feeDue));
                }

                Double penaltyAmount = installments[i].penaltyAmount;
                Double penaltyDue = Utils.getDoubleValue(period.getPenaltyChargesDue());
                if (penaltyAmount != null) {
                    assertEquals(penaltyAmount, penaltyDue,
                            "%d. installment's penalty charges due is different, expected: %.2f, actual: %.2f".formatted(i, penaltyAmount,
                                    penaltyDue));
                }

                Double outstandingAmount = installments[i].totalOutstandingAmount;
                Double totalOutstanding = Utils.getDoubleValue(period.getTotalOutstandingForPeriod());
                if (outstandingAmount != null) {
                    assertEquals(outstandingAmount, totalOutstanding,
                            "%d. installment's total outstanding is different, expected: %.2f, actual: %.2f".formatted(i, outstandingAmount,
                                    totalOutstanding));
                }

                Double outstandingPrincipalExpected = installments[i].outstandingAmounts != null
                        ? installments[i].outstandingAmounts.principalOutstanding
                        : null;
                Double outstandingPrincipal = Utils.getDoubleValue(period.getPrincipalOutstanding());
                if (outstandingPrincipalExpected != null) {
                    assertEquals(outstandingPrincipalExpected, outstandingPrincipal,
                            "%d. installment's outstanding principal is different, expected: %.2f, actual: %.2f".formatted(i,
                                    outstandingPrincipalExpected, outstandingPrincipal));
                }

                Double outstandingFeeExpected = installments[i].outstandingAmounts != null
                        ? installments[i].outstandingAmounts.feeOutstanding
                        : null;
                Double outstandingFee = Utils.getDoubleValue(period.getFeeChargesOutstanding());
                if (outstandingFeeExpected != null) {
                    assertEquals(outstandingFeeExpected, outstandingFee,
                            "%d. installment's outstanding fee is different, expected: %.2f, actual: %.2f".formatted(i,
                                    outstandingFeeExpected, outstandingFee));
                }

                Double outstandingPenaltyExpected = installments[i].outstandingAmounts != null
                        ? installments[i].outstandingAmounts.penaltyOutstanding
                        : null;
                Double outstandingPenalty = Utils.getDoubleValue(period.getPenaltyChargesOutstanding());
                if (outstandingPenaltyExpected != null) {
                    assertEquals(outstandingPenaltyExpected, outstandingPenalty,
                            "%d. installment's outstanding penalty is different, expected: %.2f, actual: %.2f".formatted(i,
                                    outstandingPenaltyExpected, outstandingPenalty));
                }

                Double outstandingTotalExpected = installments[i].outstandingAmounts != null
                        ? installments[i].outstandingAmounts.totalOutstanding
                        : null;
                Double outstandingTotal = Utils.getDoubleValue(period.getTotalOutstandingForPeriod());
                if (outstandingTotalExpected != null) {
                    assertEquals(outstandingTotalExpected, outstandingTotal,
                            "%d. installment's total outstanding is different, expected: %.2f, actual: %.2f".formatted(i,
                                    outstandingTotalExpected, outstandingTotal));
                }

                Double loanBalanceExpected = installments[i].loanBalance;
                Double loanBalance = Utils.getDoubleValue(period.getPrincipalLoanBalanceOutstanding());
                if (loanBalanceExpected != null) {
                    assertEquals(loanBalanceExpected, loanBalance,
                            "%d. installment's loan balance is different, expected: %.2f, actual: %.2f".formatted(i, loanBalanceExpected,
                                    loanBalance));
                }
                installmentNumber++;
                assertEquals(installmentNumber, period.getPeriod());
            }
            assertEquals(installments[i].completed, period.getComplete());
            assertEquals(LocalDate.parse(installments[i].dueDate, DATE_FORMATTER), period.getDueDate());
        }
    }

    public static void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, LocalDate dueDate, double principalDue,
            double principalPaid, double principalOutstanding, double paidInAdvance, double paidLate) {
        GetLoansLoanIdRepaymentPeriod period = loanDetails.getRepaymentSchedule().getPeriods().stream()
                .filter(p -> Objects.equals(p.getPeriod(), index)).findFirst().orElseThrow();
        assertEquals(dueDate, period.getDueDate());
        assertEquals(principalDue, Utils.getDoubleValue(period.getPrincipalDue()));
        assertEquals(principalPaid, Utils.getDoubleValue(period.getPrincipalPaid()));
        assertEquals(principalOutstanding, Utils.getDoubleValue(period.getPrincipalOutstanding()));
        assertEquals(paidInAdvance, Utils.getDoubleValue(period.getTotalPaidInAdvanceForPeriod()));
        assertEquals(paidLate, Utils.getDoubleValue(period.getTotalPaidLateForPeriod()));
    }

    public static void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, double principalDue, double principalPaid,
            double principalOutstanding, double paidInAdvance, double paidLate) {
        GetLoansLoanIdRepaymentPeriod period = loanDetails.getRepaymentSchedule().getPeriods().stream()
                .filter(p -> Objects.equals(p.getPeriod(), index)).findFirst().orElseThrow();
        assertEquals(principalDue, Utils.getDoubleValue(period.getPrincipalDue()));
        assertEquals(principalPaid, Utils.getDoubleValue(period.getPrincipalPaid()));
        assertEquals(principalOutstanding, Utils.getDoubleValue(period.getPrincipalOutstanding()));
        assertEquals(paidInAdvance, Utils.getDoubleValue(period.getTotalPaidInAdvanceForPeriod()));
        assertEquals(paidLate, Utils.getDoubleValue(period.getTotalPaidLateForPeriod()));
    }

    public static void validateFullyUnpaidRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, String dueDate,
            double principalDue, double feeDue, double penaltyDue, double interestDue) {
        validateRepaymentPeriod(loanDetails, index,
                LocalDate.parse(dueDate, DateTimeFormatter.ofPattern(LoanTestData.DATETIME_PATTERN, Locale.ENGLISH)), principalDue, 0,
                principalDue, feeDue, 0, feeDue, penaltyDue, 0, penaltyDue, interestDue, 0, interestDue, 0, 0);
    }

    public static void validateFullyPaidRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, String dueDate,
            double principalDue, double feeDue, double penaltyDue, double interestDue) {
        validateRepaymentPeriod(loanDetails, index,
                LocalDate.parse(dueDate, DateTimeFormatter.ofPattern(LoanTestData.DATETIME_PATTERN, Locale.ENGLISH)), principalDue,
                principalDue, 0, feeDue, feeDue, 0, penaltyDue, penaltyDue, 0, interestDue, interestDue, 0, 0, 0);
    }

    public static void validateFullyPaidRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, String dueDate,
            double principalDue, double feeDue, double penaltyDue, double interestDue, double paidLate) {
        validateRepaymentPeriod(loanDetails, index,
                LocalDate.parse(dueDate, DateTimeFormatter.ofPattern(LoanTestData.DATETIME_PATTERN, Locale.ENGLISH)), principalDue,
                principalDue, 0, feeDue, feeDue, 0, penaltyDue, penaltyDue, 0, interestDue, interestDue, 0, 0, paidLate);
    }

    public static void validateFullyPaidRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, String dueDate,
            double principalDue, double feeDue, double penaltyDue, double interestDue, double paidLate, double paidInAdvance) {
        validateRepaymentPeriod(loanDetails, index,
                LocalDate.parse(dueDate, DateTimeFormatter.ofPattern(LoanTestData.DATETIME_PATTERN, Locale.ENGLISH)), principalDue,
                principalDue, 0, feeDue, feeDue, 0, penaltyDue, penaltyDue, 0, interestDue, interestDue, 0, paidInAdvance, paidLate);
    }

    public static void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, LocalDate dueDate, double principalDue,
            double feeDue, double penaltyDue, double interestDue) {
        validateRepaymentPeriod(loanDetails, index, dueDate, principalDue, 0, principalDue, feeDue, 0, feeDue, penaltyDue, 0, penaltyDue,
                interestDue, 0, interestDue, 0, 0);
    }

    public static void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, LocalDate dueDate, double principalDue,
            double principalPaid, double principalOutstanding, double feeDue, double feePaid, double feeOutstanding, double penaltyDue,
            double penaltyPaid, double penaltyOutstanding, double interestDue, double interestPaid, double interestOutstanding,
            double paidInAdvance, double paidLate) {
        GetLoansLoanIdRepaymentPeriod period = loanDetails.getRepaymentSchedule().getPeriods().stream()
                .filter(p -> Objects.equals(p.getPeriod(), index)).findFirst().orElseThrow();
        assertEquals(dueDate, period.getDueDate());
        assertEquals(principalDue, Utils.getDoubleValue(period.getPrincipalDue()));
        assertEquals(principalPaid, Utils.getDoubleValue(period.getPrincipalPaid()));
        assertEquals(principalOutstanding, Utils.getDoubleValue(period.getPrincipalOutstanding()));
        assertEquals(feeDue, Utils.getDoubleValue(period.getFeeChargesDue()));
        assertEquals(feePaid, Utils.getDoubleValue(period.getFeeChargesPaid()));
        assertEquals(feeOutstanding, Utils.getDoubleValue(period.getFeeChargesOutstanding()));
        assertEquals(penaltyDue, Utils.getDoubleValue(period.getPenaltyChargesDue()));
        assertEquals(penaltyPaid, Utils.getDoubleValue(period.getPenaltyChargesPaid()));
        assertEquals(penaltyOutstanding, Utils.getDoubleValue(period.getPenaltyChargesOutstanding()));
        assertEquals(interestDue, Utils.getDoubleValue(period.getInterestDue()));
        assertEquals(interestPaid, Utils.getDoubleValue(period.getInterestPaid()));
        assertEquals(interestOutstanding, Utils.getDoubleValue(period.getInterestOutstanding()));
        assertEquals(paidInAdvance, Utils.getDoubleValue(period.getTotalPaidInAdvanceForPeriod()));
        assertEquals(paidLate, Utils.getDoubleValue(period.getTotalPaidLateForPeriod()));
    }

    public static void verifyLoanStatus(GetLoansLoanIdResponse loanDetails, Function<GetLoansLoanIdStatus, Boolean> extractor) {
        assertNotNull(loanDetails);
        assertNotNull(loanDetails.getStatus());
        Boolean actualValue = extractor.apply(loanDetails.getStatus());
        assertNotNull(actualValue);
        assertTrue(actualValue);
    }
}
