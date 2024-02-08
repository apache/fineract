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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object represent the important time-line events of a loan application and loan.
 */
@Getter
@RequiredArgsConstructor
public class LoanApplicationTimelineData {

    private final LocalDate submittedOnDate;
    private final String submittedByUsername;
    private final String submittedByFirstname;
    private final String submittedByLastname;
    private final LocalDate rejectedOnDate;
    private final String rejectedByUsername;
    private final String rejectedByFirstname;
    private final String rejectedByLastname;
    private final LocalDate withdrawnOnDate;
    private final String withdrawnByUsername;
    private final String withdrawnByFirstname;
    private final String withdrawnByLastname;
    private final LocalDate approvedOnDate;
    private final String approvedByUsername;
    private final String approvedByFirstname;
    private final String approvedByLastname;
    private final LocalDate expectedDisbursementDate;
    private final LocalDate actualDisbursementDate;
    private final String disbursedByUsername;
    private final String disbursedByFirstname;
    private final String disbursedByLastname;
    private final LocalDate closedOnDate;
    private final String closedByUsername;
    private final String closedByFirstname;
    private final String closedByLastname;

    private final LocalDate actualMaturityDate;
    private final LocalDate expectedMaturityDate;
    private final LocalDate writeOffOnDate;
    private final String writeOffByUsername;
    private final String writeOffByFirstname;
    private final String writeOffByLastname;

    private final LocalDate chargedOffOnDate;
    private final String chargedOffByUsername;
    private final String chargedOffByFirstname;
    private final String chargedOffByLastname;

    public static LoanApplicationTimelineData templateDefault(final LocalDate expectedDisbursementDate) {

        final LocalDate submittedOnDate = null;
        final String submittedByUsername = null;
        final String submittedByFirstname = null;
        final String submittedByLastname = null;
        final LocalDate rejectedOnDate = null;
        final String rejectedByUsername = null;
        final String rejectedByFirstname = null;
        final String rejectedByLastname = null;
        final LocalDate withdrawnOnDate = null;
        final String withdrawnByUsername = null;
        final String withdrawnByFirstname = null;
        final String withdrawnByLastname = null;
        final LocalDate approvedOnDate = null;
        final String approvedByUsername = null;
        final String approvedByFirstname = null;
        final String approvedByLastname = null;
        final LocalDate actualDisbursementDate = null;
        final String disbursedByUsername = null;
        final String disbursedByFirstname = null;
        final String disbursedByLastname = null;
        final LocalDate closedOnDate = null;
        final String closedByUsername = null;
        final String closedByFirstname = null;
        final String closedByLastname = null;
        final LocalDate expectedMaturityDate = null;
        final LocalDate writeOffOnDate = null;
        final String writeOffByUsername = null;
        final String writeOffByFirstname = null;
        final String writeOffByLastname = null;
        final LocalDate chargedOffOnDate = null;
        final String chargedOffByUsername = null;
        final String chargedOffByFirstname = null;
        final String chargedOffByLastname = null;
        final LocalDate actualMaturityDate = null;

        return new LoanApplicationTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname, submittedByLastname,
                rejectedOnDate, rejectedByUsername, rejectedByFirstname, rejectedByLastname, withdrawnOnDate, withdrawnByUsername,
                withdrawnByFirstname, withdrawnByLastname, approvedOnDate, approvedByUsername, approvedByFirstname, approvedByLastname,
                expectedDisbursementDate, actualDisbursementDate, disbursedByUsername, disbursedByFirstname, disbursedByLastname,
                closedOnDate, closedByUsername, closedByFirstname, closedByLastname, actualMaturityDate, expectedMaturityDate,
                writeOffOnDate, writeOffByUsername, writeOffByFirstname, writeOffByLastname, chargedOffOnDate, chargedOffByUsername,
                chargedOffByFirstname, chargedOffByLastname);
    }

    public RepaymentScheduleRelatedLoanData repaymentScheduleRelatedData(final CurrencyData currency, final BigDecimal principal,
            final BigDecimal approvedPrincipal, final BigDecimal inArrearsTolerance, final BigDecimal totalFeeChargesAtDisbursement) {
        return new RepaymentScheduleRelatedLoanData(this.expectedDisbursementDate, this.actualDisbursementDate, currency, principal,
                inArrearsTolerance, totalFeeChargesAtDisbursement);
    }

    public LocalDate getDisbursementDate() {
        LocalDate disbursementDate = this.expectedDisbursementDate;
        if (this.actualDisbursementDate != null) {
            disbursementDate = this.actualDisbursementDate;
        }
        return disbursementDate;
    }
}
