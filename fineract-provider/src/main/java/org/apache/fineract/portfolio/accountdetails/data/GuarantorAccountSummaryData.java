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
package org.apache.fineract.portfolio.accountdetails.data;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;

/**
 * Immutable data object for loan accounts.
 */
@SuppressWarnings("unused")
public class GuarantorAccountSummaryData {

    private final Long id;
    private final String accountNo;
    private final String externalId;
    private final Long productId;
    private final String productName;
    private final String shortProductName;
    private final LoanStatusEnumData status;
    private final EnumOptionData loanType;
    private final Integer loanCycle;
    private final Boolean inArrears;
    private final BigDecimal originalLoan;
    private final BigDecimal loanBalance;
    private final BigDecimal amountPaid;
    private final Boolean isActive;
    private final String relationship;
    private final BigDecimal onHoldAmount;
    public GuarantorAccountSummaryData(final Long id, final String accountNo, final String externalId, final Long productId,
            final String loanProductName, final String shortLoanProductName, final LoanStatusEnumData loanStatus, final EnumOptionData loanType, final Integer loanCycle,
            final Boolean inArrears,final BigDecimal originalLoan,final BigDecimal loanBalance,final BigDecimal amountPaid,
            final Boolean isActive, final String relationship, final BigDecimal onHoldAmount) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.productId = productId;
        this.productName = loanProductName;
        this.shortProductName = shortLoanProductName;
        this.status = loanStatus;
        this.loanType = loanType;
        this.loanCycle = loanCycle;
        this.inArrears = inArrears;
        this.loanBalance = loanBalance;
        this.originalLoan = originalLoan;
        this.amountPaid = amountPaid;
        this.isActive = isActive;
        this.relationship = relationship;
        this.onHoldAmount = onHoldAmount;
    }
}