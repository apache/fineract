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
package org.apache.fineract.portfolio.collectionsheet.data;

import java.math.BigDecimal;

import org.apache.fineract.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object for extracting flat data for joint liability group's
 * collection sheet.
 */
public class IndividualCollectionSheetLoanFlatData {

    private final String clientName;
    private final Long clientId;
    private final Long loanId;
    private final String accountId;
    private final Integer accountStatusId;
    private final String productShortName;
    private final Long productId;
    private final CurrencyData currency;
    private BigDecimal disbursementAmount = BigDecimal.ZERO;
    private BigDecimal principalDue = BigDecimal.ZERO;
    private BigDecimal principalPaid = BigDecimal.ZERO;
    private BigDecimal interestDue = BigDecimal.ZERO;
    private BigDecimal interestPaid = BigDecimal.ZERO;
    private BigDecimal chargesDue = BigDecimal.ZERO;
    private BigDecimal feeDue = BigDecimal.ZERO;
    private BigDecimal feePaid = BigDecimal.ZERO;

    public IndividualCollectionSheetLoanFlatData(final String clientName, final Long clientId, final Long loanId, final String accountId,
            final Integer accountStatusId, final String productShortName, final Long productId, final CurrencyData currency,
            final BigDecimal disbursementAmount, final BigDecimal principalDue, final BigDecimal principalPaid,
            final BigDecimal interestDue, final BigDecimal interestPaid, final BigDecimal chargesDue, final BigDecimal feeDue,
            final BigDecimal feePaid) {
        this.clientName = clientName;
        this.clientId = clientId;
        this.loanId = loanId;
        this.accountId = accountId;
        this.accountStatusId = accountStatusId;
        this.productShortName = productShortName;
        this.productId = productId;
        this.currency = currency;
        this.disbursementAmount = disbursementAmount;
        this.principalDue = principalDue;
        this.principalPaid = principalPaid;
        this.interestDue = interestDue;
        this.interestPaid = interestPaid;
        this.chargesDue = chargesDue;
        this.feeDue = feeDue;
        this.feePaid = feePaid;
    }

    public String getClientName() {
        return this.clientName;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public String getAccountId() {
        return this.accountId;
    }

    public Integer getAccountStatusId() {
        return this.accountStatusId;
    }

    public String getProductShortName() {
        return this.productShortName;
    }

    public Long getProductId() {
        return this.productId;
    }

    public CurrencyData getCurrency() {
        return this.currency;
    }

    public BigDecimal getDisbursementAmount() {
        return this.disbursementAmount;
    }

    public BigDecimal getPrincipalDue() {
        return this.principalDue;
    }

    public BigDecimal getPrincipalPaid() {
        return this.principalPaid;
    }

    public BigDecimal getInterestDue() {
        return this.interestDue;
    }

    public BigDecimal getInterestPaid() {
        return this.interestPaid;
    }

    public BigDecimal getChargesDue() {
        return this.chargesDue;
    }

    public LoanDueData getLoanDueData() {
        return new LoanDueData(this.loanId, this.accountId, this.accountStatusId, this.productShortName, this.productId, this.currency,
                this.disbursementAmount, this.principalDue, this.principalPaid, this.interestDue, this.interestPaid, this.chargesDue,
                this.feeDue, this.feePaid);
    }

    public IndividualClientData getClientData() {
        return IndividualClientData.instance(this.clientId, this.clientName);
    }
    
    public BigDecimal getFeeDue() {
        return this.feeDue;
    }

    public BigDecimal getFeePaid() {
        return this.feePaid;
    }

}