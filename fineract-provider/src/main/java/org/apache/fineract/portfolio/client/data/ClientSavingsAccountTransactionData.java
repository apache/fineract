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
package org.apache.fineract.portfolio.client.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;

/**
 * Immutable data object representing a client savings account transaction.
 */
public final class ClientSavingsAccountTransactionData {

    private final Long id;
    private final SavingsAccountTransactionEnumData transactionType;
    private final Long accountId;
    private final String accountNo;
    private final LocalDate date;
    private final CurrencyData currency;
    private final BigDecimal amount;
    private final BigDecimal outstandingChargeAmount;
    private final BigDecimal runningBalance;
    private final boolean reversed;
    private final LocalDate submittedOnDate;
    private final boolean interestedPostedAsOn;

    // import fields
    private transient Integer rowIndex;
    private transient Long savingsAccountId;
    private String dateFormat;
    private String locale;
    private LocalDate transactionDate;
    private BigDecimal transactionAmount;
    private Long paymentTypeId;
    private String accountNumber;
    private String checkNumber;
    private String routingCode;
    private String receiptNumber;
    private String bankNumber;
    private final Map<String, Map<String, Object>> datatables;

    public Integer getRowIndex() {
        return rowIndex;
    }

    public Long getSavingsAccountId() {
        return savingsAccountId;
    }

    public SavingsAccountTransactionEnumData getTransactionType() {
        return transactionType;
    }

    public static ClientSavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final Long savingsId, final String savingsAccountNo, final LocalDate date, final CurrencyData currency, final BigDecimal amount,
            final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance, final boolean reversed,
            final LocalDate submittedOnDate, final boolean interestedPostedAsOn, final Map<String, Map<String, Object>> datatables) {

        return new ClientSavingsAccountTransactionData(id, transactionType, savingsId, savingsAccountNo, date, currency, amount,
                outstandingChargeAmount, runningBalance, reversed, submittedOnDate, interestedPostedAsOn, datatables);
    }

    private ClientSavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final Long savingsId, final String savingsAccountNo, final LocalDate date, final CurrencyData currency, final BigDecimal amount,
            final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance, final boolean reversed,
            final LocalDate submittedOnDate, final boolean interestedPostedAsOn, final Map<String, Map<String, Object>> datatables) {
        this.id = id;
        this.transactionType = transactionType;
        this.accountId = savingsId;
        this.accountNo = savingsAccountNo;
        this.date = date;
        this.currency = currency;
        this.amount = amount;
        this.outstandingChargeAmount = outstandingChargeAmount;
        this.runningBalance = runningBalance;
        this.reversed = reversed;
        this.submittedOnDate = submittedOnDate;
        this.interestedPostedAsOn = interestedPostedAsOn;
        this.datatables = datatables;
    }

}
