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
package org.apache.fineract.portfolio.savings.data;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.joda.time.LocalDate;

public class DepositAccountOnHoldTransactionData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final BigDecimal amount;
    @SuppressWarnings("unused")
    private EnumOptionData transactionType;
    @SuppressWarnings("unused")
    private final LocalDate transactionDate;
    @SuppressWarnings("unused")
    private final boolean reversed;
    @SuppressWarnings("unused")
    private final Long savingsId;
    @SuppressWarnings("unused")
    private final String savingsAccountNo;
    @SuppressWarnings("unused")
    private final String savingsClientName;
    @SuppressWarnings("unused")
    private final Long loanId;
    @SuppressWarnings("unused")
    private final String loanAccountNo;
    @SuppressWarnings("unused")
    private final String loanClientName;

    private DepositAccountOnHoldTransactionData(final Long id, final BigDecimal amount, final EnumOptionData transactionType,
            final LocalDate transactionDate, final boolean reversed, final Long savingsId, final String savingsAccNo,
            final String savingsClientName, final Long loanId, final String loanAccNo, final String loanClientName) {
        this.id = id;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.reversed = reversed;
        this.savingsId = savingsId;
        this.savingsAccountNo = savingsAccNo;
        this.savingsClientName = savingsClientName;
        this.loanId = loanId;
        this.loanAccountNo = loanAccNo;
        this.loanClientName = loanClientName;
    }

    private DepositAccountOnHoldTransactionData(final Long id, final BigDecimal amount, final EnumOptionData transactionType,
            final LocalDate transactionDate, final boolean reversed) {
        this.id = id;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.reversed = reversed;
        this.savingsAccountNo = null;
        this.savingsId = 0L;
        this.savingsClientName = null;
        this.loanId = 0L;
        this.loanAccountNo = null;
        this.loanClientName = null;
    }

    public static DepositAccountOnHoldTransactionData instance(final Long id, final BigDecimal amount,
            final EnumOptionData transactionType, final LocalDate transactionDate, final boolean reversed, final Long savingsId,
            final String savingsAccountNo, final String savingsClientName, final Long loanId, final String loanAccountNo,
            final String loanClientName) {
        return new DepositAccountOnHoldTransactionData(id, amount, transactionType, transactionDate, reversed, savingsId, savingsAccountNo,
                savingsClientName, loanId, loanAccountNo, loanClientName);
    }

    public static DepositAccountOnHoldTransactionData instance(Long transactionId, BigDecimal amount, EnumOptionData transactionType,
            LocalDate date, boolean transactionReversed) {
        return new DepositAccountOnHoldTransactionData(transactionId, amount, transactionType, date, transactionReversed);
    }
}
