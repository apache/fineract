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

import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.interoperation.domain.InteropIdentifier;
import org.apache.fineract.interoperation.util.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.domain.*;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.joda.time.LocalDate;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InteropTransactionData extends CommandProcessingResult {

//    private final SavingsAccountTransactionEnumData transactionType;
//    private final PaymentDetailData paymentDetailData;
//    private final BigDecimal outstandingChargeAmount;
//    private final boolean reversed;
//    private final AccountTransferData transfer;
//    private final LocalDate submittedOnDate;
//    private final boolean interestedPostedAsOn;
//    private final String submittedByUsername;
//    // templates
//    final Collection<PaymentTypeData> paymentTypeOptions;
//    //import fields
//    private Long paymentTypeId;
//    private String checkNumber;
//    private String routingCode;
//    private String receiptNumber;
//    private String bankNumber;

//    private String transactionReference;
//    private String statementReference;
//    private CreditDebitType creditDebit;
//    private TransactionStatus status;
//    private String transactionInformation;
//    private String addressLine;

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

    private final String note;


    public InteropTransactionData(Long entityId, String accountId, String transactionId, SavingsAccountTransactionType transactionType, BigDecimal amount, BigDecimal chargeAmount,
                                  String currency, BigDecimal accountBalance, LocalDate bookingDateTime, LocalDate valueDateTime, String note) {
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
        if (transaction == null)
            return null;

        SavingsAccount savingsAccount = transaction.getSavingsAccount();

        String transactionId = transaction.getId().toString();
        SavingsAccountTransactionType transactionType = SavingsAccountTransactionType.fromInt(transaction.getTypeOf());
        BigDecimal amount = transaction.getAmount();

        BigDecimal chargeAmount = null;
        for (SavingsAccountChargePaidBy charge : transaction.getSavingsAccountChargesPaid()) {
            chargeAmount = MathUtil.add(chargeAmount, charge.getAmount());
        }

        String currency = savingsAccount.getCurrency().getCode();
        BigDecimal runningBalance = transaction.getRunningBalance(savingsAccount.getCurrency()).getAmount();

        LocalDate bookingDateTime = transaction.getTransactionLocalDate();
        LocalDate endOfBalanceLocalDate = transaction.getEndOfBalanceLocalDate();
        LocalDate valueDateTime = endOfBalanceLocalDate == null ? bookingDateTime : endOfBalanceLocalDate;

        StringBuilder sb = new StringBuilder();
        int currLength = 0;
        for (Note note : transaction.getNotes()) {
            String s = note.getNote();
            if (s == null)
                continue;

            int availableLength = 500 - currLength;
            if (availableLength <= 1)
                break;

            if (currLength > 0) {
                sb.append(' ');
                availableLength--;
            }
            if (s.length() > availableLength)
                s = s.substring(availableLength);
            sb.append(s);
            currLength = sb.length();
        }
        if (currLength == 0) {
            sb.append(SavingsEnumerations.transactionType(transactionType).getValue());
        }

        return new InteropTransactionData(savingsAccount.getId(), savingsAccount.getExternalId(), transactionId, transactionType, amount, chargeAmount, currency,
                runningBalance, bookingDateTime, valueDateTime, sb.toString());
    }
}
