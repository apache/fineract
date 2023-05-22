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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.domain.AccountTransferTransaction;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.useradministration.domain.AppUser;

@Getter
@AllArgsConstructor
public class SavingsAccountTransactionDTOV2 {

    private final Long transactionId;
    private final Integer transactionType;
    private final LocalDate transactionDate;
    private final BigDecimal transactionAmount;
    private final Long releaseIdOfHoldAmountTransaction;
    private final String reasonForBlock;
    private final LocalDateTime createdDate;
    private final AppUser appUser;
    private final String note;
    private final BigDecimal runningBalance;
    private final boolean reversed;
    private final boolean reversalTransaction;
    private final Long originalTxnId;
    private final Boolean lienTransaction;
    private final boolean isManualTransaction;
    private final AccountTransferTransaction fromSavingsTransaction;
    private final AccountTransferTransaction toSavingsTransaction;
    private final SavingsAccount savingsAccount;
    private final PaymentDetail paymentDetail;
    private final ApplicationCurrency currency;

    public static final SavingsAccountTransactionData tosavingsAccountTransactionData(SavingsAccountTransactionDTOV2 dto) {
        final Long id = dto.getTransactionId();
        final int transactionTypeInt = dto.getTransactionType();

        final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations.transactionType(transactionTypeInt);

        final LocalDate date = dto.getTransactionDate();
        final LocalDate submittedOnDate = Optional.ofNullable(dto.getCreatedDate().toLocalDate()).orElse(null);
        final BigDecimal amount = dto.getTransactionAmount();
        final Long releaseTransactionId = Optional.ofNullable(dto.getReleaseIdOfHoldAmountTransaction()).orElse(0L);
        final String reasonForBlock = dto.getReasonForBlock();
        final BigDecimal outstandingChargeAmount = null;
        final BigDecimal runningBalance = dto.getRunningBalance();
        final boolean reversed = dto.isReversed();
        final boolean isReversal = dto.isReversalTransaction();
        final Long originalTransactionId = Optional.ofNullable(dto.getOriginalTxnId()).orElse(0L);
        final Boolean lienTransaction = dto.getLienTransaction();

        final Long savingsId = dto.getSavingsAccount().getId();
        final String accountNo = dto.getSavingsAccount().getAccountNumber();
        final boolean postInterestAsOn = dto.isManualTransaction;

        PaymentDetailData paymentDetailData = null;
        if (transactionType.isDepositOrWithdrawal()) {
            final Long paymentTypeId = dto.getPaymentDetail().getPaymentType().getId();
            if (paymentTypeId != null) {
                final String typeName = dto.getPaymentDetail().getPaymentType().getName();
                final PaymentTypeData paymentType = PaymentTypeData.instance(paymentTypeId, typeName);
                final String accountNumber = dto.getPaymentDetail().getAccountNumber();
                final String checkNumber = dto.getPaymentDetail().getCheckNumber();
                final String routingCode = dto.getPaymentDetail().getRoutingCode();
                final String receiptNumber = dto.getPaymentDetail().getReceiptNumber();
                final String bankNumber = dto.getPaymentDetail().getBankNumber();
                paymentDetailData = new PaymentDetailData(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                        bankNumber);
            }
        }

        final String currencyCode = dto.getSavingsAccount().getCurrency().getCode();
        final String currencyName = dto.getCurrency().getName();
        final String currencyNameCode = dto.getCurrency().getNameCode();
        final String currencyDisplaySymbol = dto.getCurrency().getDisplaySymbol();
        final Integer currencyDigits = dto.getSavingsAccount().getCurrency().getDigitsAfterDecimal();
        final Integer inMultiplesOf = dto.getSavingsAccount().getCurrency().getCurrencyInMultiplesOf();
        final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                currencyNameCode);

        AccountTransferData transfer = null;
        AccountTransferTransaction transferFrom = dto.getFromSavingsTransaction();
        AccountTransferTransaction transferTo = dto.getToSavingsTransaction();
        if (Objects.nonNull(transferFrom)) {
            final Long fromTransferId = transferFrom.getId();
            final LocalDate fromTransferDate = transferFrom.getDate();
            final BigDecimal fromTransferAmount = getOrDefault(transferFrom.getAmount(), BigDecimal.ZERO);
            final boolean fromTransferReversed = transferFrom.isReversed();
            final String fromTransferDescription = transferFrom.getDescription();

            transfer = AccountTransferData.transferBasicDetails(fromTransferId, currency, fromTransferAmount, fromTransferDate,
                    fromTransferDescription, fromTransferReversed);
        } else if (Objects.nonNull(transferTo)) {
            final Long toTransferId = transferTo.getId();
            final LocalDate toTransferDate = transferTo.getDate();
            final BigDecimal toTransferAmount = getOrDefault(transferTo.getAmount(), BigDecimal.ZERO);
            final boolean toTransferReversed = transferTo.isReversed();
            final String toTransferDescription = transferTo.getDescription();

            transfer = AccountTransferData.transferBasicDetails(toTransferId, currency, toTransferAmount, toTransferDate,
                    toTransferDescription, toTransferReversed);
        }
        String submittedByUsername = null;
        if (Objects.nonNull(dto.getAppUser())) {
            submittedByUsername = getOrDefault(dto.getAppUser().getUsername(), null);
        }
        final String note = dto.getNote();

        return SavingsAccountTransactionData.create(id, transactionType, paymentDetailData, savingsId, accountNo, date, currency, amount,
                outstandingChargeAmount, runningBalance, reversed, transfer, submittedOnDate, postInterestAsOn, submittedByUsername, note,
                isReversal, originalTransactionId, lienTransaction, releaseTransactionId, reasonForBlock);
    }

    private static <T> T getOrDefault(T input, T defaultValue) {
        return Optional.ofNullable(input).orElse(defaultValue);
    }
}
