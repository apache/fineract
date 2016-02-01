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
package org.apache.fineract.portfolio.loanaccount.guarantor.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;

public class GuarantorFundingData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final EnumOptionData status;
    @SuppressWarnings("unused")
    private final PortfolioAccountData savingsAccount;
    @SuppressWarnings("unused")
    private final BigDecimal amount;
    @SuppressWarnings("unused")
    private final BigDecimal amountReleased;
    @SuppressWarnings("unused")
    private final BigDecimal amountRemaining;
    @SuppressWarnings("unused")
    private final BigDecimal amountTransfered;
    @SuppressWarnings("unused")
    private final Collection<GuarantorTransactionData> guarantorTransactions;

    private GuarantorFundingData(final Long id, final EnumOptionData status, final PortfolioAccountData savingsAccount, final BigDecimal amount,
            final BigDecimal amountReleased, final BigDecimal amountRemaining, final BigDecimal amountTransfered,
            final Collection<GuarantorTransactionData> guarantorTransactions) {
        this.id = id;
        this.status = status;
        this.savingsAccount = savingsAccount;
        this.amount = amount;
        this.amountReleased = amountReleased;
        this.amountRemaining = amountRemaining;
        this.amountTransfered = amountTransfered;
        this.guarantorTransactions = guarantorTransactions;
    }

    public static GuarantorFundingData instance(final Long id, final EnumOptionData status, final PortfolioAccountData savingsAccount, final BigDecimal amount,
            final BigDecimal amountReleased, final BigDecimal amountRemaining, final BigDecimal amountTransfered,
            final Collection<GuarantorTransactionData> guarantorTransactions) {
        return new GuarantorFundingData(id, status, savingsAccount, amount, amountReleased, amountRemaining, amountTransfered,
                guarantorTransactions);
    }
}
