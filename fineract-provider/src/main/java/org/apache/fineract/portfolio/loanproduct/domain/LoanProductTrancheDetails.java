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
package org.apache.fineract.portfolio.loanproduct.domain;

import java.math.BigDecimal;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;

@Embeddable
public class LoanProductTrancheDetails {

    @Column(name = "allow_multiple_disbursals")
    private boolean multiDisburseLoan;

    @Column(name = "max_disbursals", nullable = true)
    private Integer maxTrancheCount;

    @Column(name = "max_outstanding_loan_balance", scale = 6, precision = 19, nullable = true)
    private BigDecimal outstandingLoanBalance;

    protected LoanProductTrancheDetails() {
        // TODO Auto-generated constructor stub
    }

    public LoanProductTrancheDetails(final boolean multiDisburseLoan, final Integer maxTrancheCount, final BigDecimal outstandingLoanBalance) {
        this.multiDisburseLoan = multiDisburseLoan;
        this.maxTrancheCount = maxTrancheCount;
        this.outstandingLoanBalance = outstandingLoanBalance;
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges, final String localeAsInput) {
        if (command.isChangeInBooleanParameterNamed(LoanProductConstants.multiDisburseLoanParameterName, this.multiDisburseLoan)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(LoanProductConstants.multiDisburseLoanParameterName);
            actualChanges.put(LoanProductConstants.multiDisburseLoanParameterName, newValue);
            this.multiDisburseLoan = newValue;
        }

        if (this.multiDisburseLoan) {
            if (command.isChangeInIntegerParameterNamed(LoanProductConstants.maxTrancheCountParameterName, this.maxTrancheCount)) {
                final Integer newValue = command.integerValueOfParameterNamed(LoanProductConstants.maxTrancheCountParameterName);
                actualChanges.put(LoanProductConstants.maxTrancheCountParameterName, newValue);
                this.maxTrancheCount = newValue;
            }

            if (command.isChangeInBigDecimalParameterNamed(LoanProductConstants.outstandingLoanBalanceParameterName,
                    this.outstandingLoanBalance)) {
                final BigDecimal newValue = command
                        .bigDecimalValueOfParameterNamed(LoanProductConstants.outstandingLoanBalanceParameterName);
                actualChanges.put(LoanProductConstants.outstandingLoanBalanceParameterName, newValue);
                actualChanges.put(LoanProductConstants.outstandingLoanBalanceParameterName, localeAsInput);
                this.outstandingLoanBalance = newValue;
            }
        } else {
            this.maxTrancheCount = null;
            this.outstandingLoanBalance = null;
        }
    }

    public boolean isMultiDisburseLoan() {
        return this.multiDisburseLoan;
    }

    public BigDecimal outstandingLoanBalance() {
        return this.outstandingLoanBalance;
    }

    public Integer maxTrancheCount() {
        return this.maxTrancheCount;
    }

}
