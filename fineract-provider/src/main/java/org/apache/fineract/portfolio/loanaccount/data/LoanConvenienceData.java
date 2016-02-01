/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

public class LoanConvenienceData {

    private final int maxSubmittedOnOffsetFromToday;
    private final int maxApprovedOnOffsetFromToday;
    private final int maxDisbursedOnOffsetFromToday;
    private final int expectedLoanTermInDays;
    private final int actualLoanTermInDays;
    private final int expectedLoanTermInMonths;
    private final int actualLoanTermInMonths;

    public LoanConvenienceData(final int maxSubmittedOnOffsetFromToday, final int maxApprovedOnOffsetFromToday,
            final int maxDisbursedOnOffsetFromToday, final int expectedLoanTermInDays, final int actualLoanTermInDays,
            final int expectedLoanTermInMonths, final int actualLoanTermInMonths) {
        this.maxSubmittedOnOffsetFromToday = maxSubmittedOnOffsetFromToday;
        this.maxApprovedOnOffsetFromToday = maxApprovedOnOffsetFromToday;
        this.maxDisbursedOnOffsetFromToday = maxDisbursedOnOffsetFromToday;
        this.expectedLoanTermInDays = expectedLoanTermInDays;
        this.actualLoanTermInDays = actualLoanTermInDays;
        this.expectedLoanTermInMonths = expectedLoanTermInMonths;
        this.actualLoanTermInMonths = actualLoanTermInMonths;
    }

    public int getMaxSubmittedOnOffsetFromToday() {
        return this.maxSubmittedOnOffsetFromToday;
    }

    public int getMaxApprovedOnOffsetFromToday() {
        return this.maxApprovedOnOffsetFromToday;
    }

    public int getMaxDisbursedOnOffsetFromToday() {
        return this.maxDisbursedOnOffsetFromToday;
    }

    public int getExpectedLoanTermInDays() {
        return this.expectedLoanTermInDays;
    }

    public int getActualLoanTermInDays() {
        return this.actualLoanTermInDays;
    }

    public int getExpectedLoanTermInMonths() {
        return this.expectedLoanTermInMonths;
    }

    public int getActualLoanTermInMonths() {
        return this.actualLoanTermInMonths;
    }
}