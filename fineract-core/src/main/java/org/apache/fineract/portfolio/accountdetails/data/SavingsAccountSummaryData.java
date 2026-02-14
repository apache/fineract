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
import java.time.LocalDate;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountApplicationTimelineData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSubStatusEnumData;

/**
 * Immutable data object for savings accounts.
 */
@Getter
@SuppressWarnings("unused")
public class SavingsAccountSummaryData {

    private final Long id;
    private final String accountNo;
    private final String externalId;
    private final Long productId;
    private final String productName;
    private final String shortProductName;
    private final SavingsAccountStatusEnumData status;
    private final CurrencyData currency;
    /**
     * The total balance of the savings account.
     */
    private final BigDecimal accountBalance;
    /**
     * Funds held as collateral for loan guarantees. When a savings account is used as a guarantor for a loan, the
     * guarantee amount is held here and unavailable for withdrawal until the loan is repaid or the guarantee is
     * released. Sourced from the database column on_hold_funds_derived.
     */
    private final BigDecimal onHoldFunds;
    /**
     * User-initiated holds explicitly placed on the account, including lien holds. These are manual holds placed
     * through hold/release transactions to restrict withdrawals for specific purposes. Sourced from the database column
     * total_savings_amount_on_hold.
     */
    private final BigDecimal savingsAmountOnHold;
    /**
     * The actual available balance that can be withdrawn, accounting for all holds. Calculated as: accountBalance -
     * onHoldFunds (guarantor holds) - savingsAmountOnHold (user/lien holds)
     */
    private final BigDecimal availableBalance;
    // differentiate Individual, JLG or Group account
    private final EnumOptionData accountType;
    private final SavingsAccountApplicationTimelineData timeline;
    private final SavingsAccountSubStatusEnumData subStatus;
    private final LocalDate lastActiveTransactionDate;

    // differentiate deposit accounts Savings, FD and RD accounts
    private final EnumOptionData depositType;

    public SavingsAccountSummaryData(final Long id, final String accountNo, final String externalId, final Long productId,
            final String productName, final String shortProductName, final SavingsAccountStatusEnumData status, final CurrencyData currency,
            final BigDecimal accountBalance, final BigDecimal onHoldFunds, final BigDecimal savingsAmountOnHold,
            final BigDecimal availableBalance, final EnumOptionData accountType, final SavingsAccountApplicationTimelineData timeline,
            final EnumOptionData depositType, final SavingsAccountSubStatusEnumData subStatus, final LocalDate lastActiveTransactionDate) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.productId = productId;
        this.productName = productName;
        this.shortProductName = shortProductName;
        this.status = status;
        this.currency = currency;
        this.accountBalance = accountBalance;
        this.onHoldFunds = onHoldFunds;
        this.savingsAmountOnHold = savingsAmountOnHold;
        this.availableBalance = availableBalance;
        this.accountType = accountType;
        this.timeline = timeline;
        this.depositType = depositType;
        this.subStatus = subStatus;
        this.lastActiveTransactionDate = lastActiveTransactionDate;
    }
}
