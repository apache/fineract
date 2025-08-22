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
package org.apache.fineract.portfolio.loanaccount.data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;

/**
 * Immutable data object representing a loan transaction.
 */
@Getter
@Builder(builderClassName = "Builder")
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class LoanTransactionData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final Long loanId;
    private final ExternalId externalLoanId;
    private final Long officeId;
    private final String officeName;

    private final LoanTransactionEnumData type;

    private final LocalDate date;

    private final CurrencyData currency;
    private final PaymentDetailData paymentDetailData;

    private final BigDecimal amount;
    private final BigDecimal netDisbursalAmount;
    private final BigDecimal principalPortion;
    private final BigDecimal interestPortion;
    private final BigDecimal feeChargesPortion;
    private final BigDecimal penaltyChargesPortion;
    private final BigDecimal overpaymentPortion;
    private final BigDecimal unrecognizedIncomePortion;
    private final ExternalId externalId;
    private final AccountTransferData transfer;
    private final BigDecimal fixedEmiAmount;
    private final BigDecimal outstandingLoanBalance;
    private final LocalDate submittedOnDate;
    private final boolean manuallyReversed;
    private final LocalDate possibleNextRepaymentDate;
    private final BigDecimal availableDisbursementAmountWithOverApplied;

    @Setter
    private Collection<LoanChargePaidByData> loanChargePaidByList;

    // templates
    final Collection<PaymentTypeData> paymentTypeOptions;

    private Collection<CodeValueData> writeOffReasonOptions = null;

    @Setter
    private Integer numberOfRepayments = 0;

    // import fields
    private transient Integer rowIndex;
    private String dateFormat;
    private String locale;
    private BigDecimal transactionAmount;
    private LocalDate transactionDate;
    private Long paymentTypeId;
    private String accountNumber;
    private Integer checkNumber;
    private Integer routingCode;
    private Integer receiptNumber;
    private Integer bankNumber;
    private transient Long accountId;
    private transient String transactionType;
    @Setter
    private List<LoanRepaymentScheduleInstallmentData> loanRepaymentScheduleInstallments;

    // Reverse Data
    private final ExternalId reversalExternalId;
    private LocalDate reversedOnDate;

    @Setter
    private List<LoanTransactionRelationData> transactionRelations;

    private Collection<CodeValueData> chargeOffReasonOptions = null;
    private Collection<CodeValueData> classificationOptions = null;
    private CodeValueData classification;

    public static LoanTransactionData importInstance(BigDecimal repaymentAmount, LocalDate lastRepaymentDate, Long repaymentTypeId,
            Integer rowIndex, String locale, String dateFormat) {
        return LoanTransactionData.builder().transactionAmount(repaymentAmount).transactionDate(lastRepaymentDate)
                .paymentTypeId(repaymentTypeId).rowIndex(rowIndex).locale(locale).dateFormat(dateFormat).externalLoanId(ExternalId.empty())
                .externalId(ExternalId.empty()).reversalExternalId(ExternalId.empty()).manuallyReversed(false).build();
    }

    public static LoanTransactionData importInstance(BigDecimal repaymentAmount, LocalDate repaymentDate, Long repaymentTypeId,
            String accountNumber, Integer checkNumber, Integer routingCode, Integer receiptNumber, Integer bankNumber, Long loanAccountId,
            String transactionType, Integer rowIndex, String locale, String dateFormat) {
        return LoanTransactionData.builder().transactionAmount(repaymentAmount).transactionDate(repaymentDate)
                .paymentTypeId(repaymentTypeId).accountNumber(accountNumber).checkNumber(checkNumber).routingCode(routingCode)
                .receiptNumber(receiptNumber).bankNumber(bankNumber).accountId(loanAccountId).transactionType(transactionType)
                .rowIndex(rowIndex).locale(locale).dateFormat(dateFormat).externalLoanId(ExternalId.empty()).externalId(ExternalId.empty())
                .reversalExternalId(ExternalId.empty()).manuallyReversed(false).build();
    }

    public static LoanTransactionData templateOnTop(final LoanTransactionData loanTransactionData,
            final Collection<PaymentTypeData> paymentTypeOptions) {
        return builder().id(loanTransactionData.id).officeId(loanTransactionData.officeId).officeName(loanTransactionData.officeName)
                .type(loanTransactionData.type).paymentDetailData(loanTransactionData.paymentDetailData)
                .currency(loanTransactionData.currency).date(loanTransactionData.date).amount(loanTransactionData.amount)
                .netDisbursalAmount(loanTransactionData.netDisbursalAmount).principalPortion(loanTransactionData.principalPortion)
                .interestPortion(loanTransactionData.interestPortion).feeChargesPortion(loanTransactionData.feeChargesPortion)
                .penaltyChargesPortion(loanTransactionData.penaltyChargesPortion).overpaymentPortion(loanTransactionData.overpaymentPortion)
                .unrecognizedIncomePortion(loanTransactionData.unrecognizedIncomePortion).paymentTypeOptions(paymentTypeOptions)
                .externalId(loanTransactionData.externalId).transfer(loanTransactionData.transfer)
                .fixedEmiAmount(loanTransactionData.fixedEmiAmount).outstandingLoanBalance(loanTransactionData.outstandingLoanBalance)
                .manuallyReversed(loanTransactionData.manuallyReversed).loanId(loanTransactionData.loanId)
                .externalLoanId(loanTransactionData.externalLoanId).build();
    }

    public static LoanTransactionData templateOnTop(final LoanTransactionData loanTransactionData, final LoanTransactionEnumData typeOf) {
        return builder().id(loanTransactionData.id).officeId(loanTransactionData.officeId).officeName(loanTransactionData.officeName)
                .type(typeOf).paymentDetailData(loanTransactionData.paymentDetailData).currency(loanTransactionData.currency)
                .date(loanTransactionData.date).amount(loanTransactionData.amount)
                .netDisbursalAmount(loanTransactionData.netDisbursalAmount).principalPortion(loanTransactionData.principalPortion)
                .interestPortion(loanTransactionData.interestPortion).feeChargesPortion(loanTransactionData.feeChargesPortion)
                .penaltyChargesPortion(loanTransactionData.penaltyChargesPortion).overpaymentPortion(loanTransactionData.overpaymentPortion)
                .unrecognizedIncomePortion(loanTransactionData.unrecognizedIncomePortion)
                .paymentTypeOptions(loanTransactionData.paymentTypeOptions).externalId(loanTransactionData.externalId)
                .transfer(loanTransactionData.transfer).fixedEmiAmount(loanTransactionData.fixedEmiAmount)
                .outstandingLoanBalance(loanTransactionData.outstandingLoanBalance).manuallyReversed(loanTransactionData.manuallyReversed)
                .loanId(loanTransactionData.loanId).externalLoanId(loanTransactionData.externalLoanId).build();
    }

    public static LoanTransactionData loanTransactionDataForCreditTemplate(final LoanTransactionEnumData transactionType,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final Collection<PaymentTypeData> paymentOptions,
            final CurrencyData currency, List<CodeValueData> classificationOptions) {
        return builder().type(transactionType).date(transactionDate).amount(transactionAmount).paymentTypeOptions(paymentOptions)
                .currency(currency).externalLoanId(ExternalId.empty()).externalId(ExternalId.empty()).reversalExternalId(ExternalId.empty())
                .manuallyReversed(false).classificationOptions(classificationOptions).build();
    }

    public static LoanTransactionData loanTransactionDataForDisbursalTemplate(final LoanTransactionEnumData transactionType,
            final LocalDate expectedDisbursedOnLocalDateForTemplate, final BigDecimal disburseAmountForTemplate,
            final BigDecimal netDisbursalAmount, final Collection<PaymentTypeData> paymentOptions, final BigDecimal fixedEmiAmount,
            final LocalDate possibleNextRepaymentDate, final CurrencyData currency,
            final BigDecimal availableDisbursementAmountWithOverApplied) {
        return builder().type(transactionType).date(expectedDisbursedOnLocalDateForTemplate).amount(disburseAmountForTemplate)
                .netDisbursalAmount(netDisbursalAmount).paymentTypeOptions(paymentOptions).fixedEmiAmount(fixedEmiAmount)
                .possibleNextRepaymentDate(possibleNextRepaymentDate).currency(currency)
                .availableDisbursementAmountWithOverApplied(availableDisbursementAmountWithOverApplied).externalLoanId(ExternalId.empty())
                .externalId(ExternalId.empty()).reversalExternalId(ExternalId.empty()).manuallyReversed(false).build();
    }
}
