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
public class SavingsTransactionSearchResult {

    private Long transactionId;
    private Integer transactionType;
    private LocalDate transactionDate;
    private BigDecimal transactionAmount;
    private Long releaseIdOfHoldAmountTransaction;
    private String reasonForBlock;
    private LocalDateTime createdDate;
    private AppUser appUser;
    private String note;
    private BigDecimal runningBalance;
    private boolean reversed;
    private boolean reversalTransaction;
    private Long originalTxnId;
    private Boolean lienTransaction;
    private boolean isManualTransaction;
    private AccountTransferTransaction fromSavingsTransaction;
    private AccountTransferTransaction toSavingsTransaction;
    private SavingsAccount savingsAccount;
    private PaymentDetail paymentDetail;
    private ApplicationCurrency currency;

    public static final SavingsAccountTransactionData toSavingsAccountTransactionData(SavingsTransactionSearchResult dto) {
        final Long id = dto.getTransactionId();
        final int transactionTypeInt = dto.getTransactionType();

        final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations.transactionType(transactionTypeInt);

        final LocalDate date = dto.getTransactionDate();
        final LocalDate submittedOnDate = Optional.ofNullable(dto.getCreatedDate()).map(LocalDateTime::toLocalDate).orElse(null);
        final BigDecimal amount = Optional.ofNullable(dto.getTransactionAmount()).orElse(BigDecimal.ZERO);
        final Long releaseTransactionId = Optional.ofNullable(dto.getReleaseIdOfHoldAmountTransaction()).orElse(null);
        final String reasonForBlock = Optional.ofNullable(dto.getReasonForBlock()).orElse(null);
        final BigDecimal outstandingChargeAmount = null;
        final BigDecimal runningBalance = Optional.ofNullable(dto.getRunningBalance()).orElse(BigDecimal.ZERO);
        final boolean reversed = dto.isReversed();
        final boolean isReversal = dto.isReversalTransaction();
        final Long originalTransactionId = Optional.ofNullable(dto.getOriginalTxnId()).orElse(null);
        final Boolean lienTransaction = dto.getLienTransaction();

        final Long savingsId = Optional.ofNullable(dto.getSavingsAccount()).map(savingsAccount -> savingsAccount.getId()).orElse(null);
        final String accountNo = Optional.ofNullable(dto.getSavingsAccount()).map(savingsAccount -> savingsAccount.getAccountNumber())
                .orElse(null);
        final boolean postInterestAsOn = dto.isManualTransaction();

        PaymentDetailData paymentDetailData = null;
        if (Objects.nonNull(transactionType) && transactionType.isDepositOrWithdrawal()) {
            final PaymentDetail paymentDetail = dto.getPaymentDetail();
            if (Objects.nonNull(paymentDetail)) {
                final Long paymentTypeId = Optional.ofNullable(paymentDetail.getPaymentType()).map(paymentType -> paymentType.getId())
                        .orElse(null);
                if (Objects.nonNull(paymentTypeId)) {
                    final String typeName = Optional.ofNullable(paymentDetail.getPaymentType()).map(paymentType -> paymentType.getName())
                            .orElse(null);
                    final PaymentTypeData paymentType = PaymentTypeData.instance(paymentTypeId, typeName);
                    final String accountNumber = paymentDetail.getAccountNumber();
                    final String checkNumber = paymentDetail.getCheckNumber();
                    final String routingCode = paymentDetail.getRoutingCode();
                    final String receiptNumber = paymentDetail.getReceiptNumber();
                    final String bankNumber = paymentDetail.getBankNumber();
                    paymentDetailData = new PaymentDetailData(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                            bankNumber);
                }
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
            final BigDecimal fromTransferAmount = Optional.ofNullable(transferFrom.getAmount()).orElse(BigDecimal.ZERO);
            final boolean fromTransferReversed = transferFrom.isReversed();
            final String fromTransferDescription = transferFrom.getDescription();

            transfer = AccountTransferData.transferBasicDetails(fromTransferId, currency, fromTransferAmount, fromTransferDate,
                    fromTransferDescription, fromTransferReversed);
        } else if (Objects.nonNull(transferTo)) {
            final Long toTransferId = transferTo.getId();
            final LocalDate toTransferDate = transferTo.getDate();
            final BigDecimal toTransferAmount = Optional.ofNullable(transferTo.getAmount()).orElse(BigDecimal.ZERO);
            final boolean toTransferReversed = transferTo.isReversed();
            final String toTransferDescription = transferTo.getDescription();

            transfer = AccountTransferData.transferBasicDetails(toTransferId, currency, toTransferAmount, toTransferDate,
                    toTransferDescription, toTransferReversed);
        }
        final String submittedByUsername = Optional.ofNullable(dto.getAppUser()).map(user -> user.getUsername()).orElse(null);
        final String note = Optional.ofNullable(dto.getNote()).orElse(null);
        return SavingsAccountTransactionData.create(id, transactionType, paymentDetailData, savingsId, accountNo, date, currency, amount,
                outstandingChargeAmount, runningBalance, reversed, transfer, submittedOnDate, postInterestAsOn, submittedByUsername, note,
                isReversal, originalTransactionId, lienTransaction, releaseTransactionId, reasonForBlock);

    }

}
