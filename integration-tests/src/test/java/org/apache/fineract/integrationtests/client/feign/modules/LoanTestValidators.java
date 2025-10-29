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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.integrationtests.common.Utils;

public final class LoanTestValidators {

    private LoanTestValidators() {}

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
