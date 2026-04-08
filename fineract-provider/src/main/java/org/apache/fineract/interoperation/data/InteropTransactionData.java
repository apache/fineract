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
package org.apache.fineract.interoperation.data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargePaidBy;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;

@Getter
public class InteropTransactionData extends CommandProcessingResult {

    @NotNull
    private final String accountId;
    @NotNull
    private final String savingTransactionId;
    @NotNull
    private final SavingsAccountTransactionType transactionType;
    @NotNull
    private final BigDecimal amount;

    private final BigDecimal chargeAmount;
    @NotNull
    private final String currency;
    @NotNull
    private final BigDecimal accountBalance;
    @NotNull
    private final LocalDate bookingDateTime;
    @NotNull
    private final LocalDate valueDateTime;

    private String note;

    public InteropTransactionData(Long entityId, String accountId, String transactionId, SavingsAccountTransactionType transactionType,
            BigDecimal amount, BigDecimal chargeAmount, String currency, BigDecimal accountBalance, LocalDate bookingDateTime,
            LocalDate valueDateTime, String note) {
        super(entityId);
        this.accountId = accountId;
        this.savingTransactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.chargeAmount = chargeAmount;
        this.currency = currency;
        this.accountBalance = accountBalance;
        this.bookingDateTime = bookingDateTime;
        this.valueDateTime = valueDateTime;
        this.note = note;
    }

    public static InteropTransactionData build(SavingsAccountTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        SavingsAccount savingsAccount = transaction.getSavingsAccount();

        String transactionId = transaction.getId().toString();
        SavingsAccountTransactionType transactionType = transaction.getTransactionType();
        BigDecimal amount = transaction.getAmount();

        BigDecimal chargeAmount = null;
        for (SavingsAccountChargePaidBy charge : transaction.getSavingsAccountChargesPaid()) {
            chargeAmount = MathUtil.add(chargeAmount, charge.getAmount());
        }

        String currency = savingsAccount.getCurrency().getCode();
        BigDecimal runningBalance = transaction.getRunningBalance(savingsAccount.getCurrency()).getAmount();

        LocalDate bookingDateTime = transaction.getTransactionDate();
        LocalDate endOfBalanceLocalDate = transaction.getEndOfBalanceDate();
        LocalDate valueDateTime = endOfBalanceLocalDate == null ? bookingDateTime : endOfBalanceLocalDate;

        StringBuilder sb = new StringBuilder();
        sb.append(SavingsEnumerations.transactionType(transactionType).getValue());

        return new InteropTransactionData(savingsAccount.getId(), savingsAccount.getExternalId().getValue(), transactionId, transactionType,
                amount, chargeAmount, currency, runningBalance, bookingDateTime, valueDateTime, sb.toString());
    }

    public void updateNote(String note) {
        this.note = note;
    }

}
