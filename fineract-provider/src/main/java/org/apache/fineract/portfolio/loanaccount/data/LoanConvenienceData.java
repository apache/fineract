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