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
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountApplicationTimelineData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSubStatusEnumData;
import org.joda.time.LocalDate;

/**
 * Immutable data object for savings accounts.
 */
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
    private final BigDecimal accountBalance;
    //differentiate Individual, JLG or Group account
    private final EnumOptionData accountType;
    private final SavingsAccountApplicationTimelineData timeline;
    private final SavingsAccountSubStatusEnumData subStatus;
    private final LocalDate lastActiveTransactionDate;

    //differentiate deposit accounts Savings, FD and RD accounts
    private final EnumOptionData depositType;

    public SavingsAccountSummaryData(final Long id, final String accountNo, final String externalId, final Long productId,
            final String productName, final String shortProductName, final SavingsAccountStatusEnumData status, final CurrencyData currency,
            final BigDecimal accountBalance, final EnumOptionData accountType, final SavingsAccountApplicationTimelineData timeline, final EnumOptionData depositType, 
            final SavingsAccountSubStatusEnumData subStatus, final LocalDate lastActiveTransactionDate) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.productId = productId;
        this.productName = productName;
        this.shortProductName = shortProductName;
        this.status = status;
        this.currency = currency;
        this.accountBalance = accountBalance;
        this.accountType = accountType;
        this.timeline = timeline;
        this.depositType = depositType;
        this.subStatus = subStatus;
        this.lastActiveTransactionDate = lastActiveTransactionDate;
    }
}