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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.junit.jupiter.api.Test;

public class LoanTransactionDataTest {

    @Test
    public void testLoanTransactionDataBuilder() {
        // Given
        // Primitive and simple types
        Long id = 1L;
        Long officeId = 2L;
        String officeName = "Test Office";
        LocalDate date = LocalDate.of(2023, 1, 1);
        BigDecimal amount = new BigDecimal("1000.50");
        boolean manuallyReversed = true;
        Long loanId = 10L;
        String transactionType = "REPAYMENT";
        Integer rowIndex = 1;
        String dateFormat = "dd MMMM yyyy";
        String locale = "en";
        Long paymentTypeId = 1L;
        String accountNumber = "AC123456";
        Integer checkNumber = 2;
        Integer routingCode = 3;
        Integer receiptNumber = 4;
        Integer bankNumber = 5;
        Long accountId = 5L;
        BigDecimal transactionAmount = new BigDecimal("1000.50");
        Integer numberOfRepayments = 12;

        // Complex types
        LoanTransactionEnumData type = LoanEnumerations.transactionType(LoanTransactionType.REPAYMENT);
        PaymentDetailData paymentDetailData = mock(PaymentDetailData.class);
        CurrencyData currency = new CurrencyData("USD", "US Dollar", 2, 0, "$", "USD");
        ExternalId externalId = ExternalId.generate();
        ExternalId externalLoanId = ExternalId.generate();
        ExternalId reversalExternalId = ExternalId.generate();

        // Financial amounts
        BigDecimal netDisbursalAmount = new BigDecimal("2000.00");
        BigDecimal principalPortion = new BigDecimal("800.00");
        BigDecimal interestPortion = new BigDecimal("100.00");
        BigDecimal feeChargesPortion = new BigDecimal("50.25");
        BigDecimal penaltyChargesPortion = new BigDecimal("50.25");
        BigDecimal overpaymentPortion = new BigDecimal("100.00");
        BigDecimal unrecognizedIncomePortion = BigDecimal.ZERO;
        BigDecimal fixedEmiAmount = new BigDecimal("500.00");
        BigDecimal availableDisbursementAmountWithOverApplied = new BigDecimal("1500.00");

        // Dates
        LocalDate possibleNextRepaymentDate = LocalDate.of(2023, 2, 1);
        LocalDate reversedOnDate = LocalDate.of(2023, 1, 15);
        LocalDate submittedOnDate = LocalDate.of(2023, 1, 1);

        // Collections
        List<LoanTransactionRelationData> transactionRelations = mock(List.class);
        List<LoanChargePaidByData> loanChargePaidByList = mock(List.class);
        List<LoanRepaymentScheduleInstallmentData> loanRepaymentScheduleInstallments = mock(List.class);
        Collection<CodeValueData> writeOffReasonOptions = mock(List.class);
        Collection<CodeValueData> chargeOffReasonOptions = mock(List.class);
        Collection<PaymentTypeData> paymentTypeOptions = mock(List.class);
        AccountTransferData transfer = mock(AccountTransferData.class);

        // When
        LoanTransactionData data = LoanTransactionData.builder()
                // Simple fields
                .id(id).officeId(officeId).officeName(officeName).date(date).amount(amount).manuallyReversed(manuallyReversed)
                .loanId(loanId).transactionType(transactionType).rowIndex(rowIndex).dateFormat(dateFormat).locale(locale)
                .paymentTypeId(paymentTypeId).accountNumber(accountNumber).checkNumber(checkNumber).routingCode(routingCode)
                .receiptNumber(receiptNumber).bankNumber(bankNumber).accountId(accountId).transactionAmount(transactionAmount)
                .numberOfRepayments(numberOfRepayments)
                // Complex fields
                .type(type).paymentDetailData(paymentDetailData).currency(currency).externalId(externalId).externalLoanId(externalLoanId)
                .reversalExternalId(reversalExternalId)
                // Financial amounts
                .netDisbursalAmount(netDisbursalAmount).principalPortion(principalPortion).interestPortion(interestPortion)
                .feeChargesPortion(feeChargesPortion).penaltyChargesPortion(penaltyChargesPortion).overpaymentPortion(overpaymentPortion)
                .unrecognizedIncomePortion(unrecognizedIncomePortion).fixedEmiAmount(fixedEmiAmount)
                .availableDisbursementAmountWithOverApplied(availableDisbursementAmountWithOverApplied)
                // Dates
                .possibleNextRepaymentDate(possibleNextRepaymentDate).reversedOnDate(reversedOnDate).submittedOnDate(submittedOnDate)
                // Collections
                .transactionRelations(transactionRelations).loanChargePaidByList(loanChargePaidByList)
                .loanRepaymentScheduleInstallments(loanRepaymentScheduleInstallments).writeOffReasonOptions(writeOffReasonOptions)
                .chargeOffReasonOptions(chargeOffReasonOptions).paymentTypeOptions(paymentTypeOptions).transfer(transfer).build();

        // Then - Simple fields
        assertEquals(id, data.getId());
        assertEquals(officeId, data.getOfficeId());
        assertEquals(officeName, data.getOfficeName());
        assertEquals(date, data.getDate());
        assertEquals(amount, data.getAmount());
        assertEquals(manuallyReversed, data.isManuallyReversed());
        assertEquals(loanId, data.getLoanId());
        assertEquals(transactionType, data.getTransactionType());
        assertEquals(rowIndex, data.getRowIndex());
        assertEquals(dateFormat, data.getDateFormat());
        assertEquals(locale, data.getLocale());
        assertEquals(paymentTypeId, data.getPaymentTypeId());
        assertEquals(accountNumber, data.getAccountNumber());
        assertEquals(checkNumber, data.getCheckNumber());
        assertEquals(routingCode, data.getRoutingCode());
        assertEquals(receiptNumber, data.getReceiptNumber());
        assertEquals(bankNumber, data.getBankNumber());
        assertEquals(accountId, data.getAccountId());
        assertEquals(transactionAmount, data.getTransactionAmount());
        assertEquals(numberOfRepayments, data.getNumberOfRepayments());

        // Complex fields
        assertEquals(type, data.getType());
        assertEquals(paymentDetailData, data.getPaymentDetailData());
        assertEquals(currency, data.getCurrency());
        assertEquals(externalId, data.getExternalId());
        assertEquals(externalLoanId, data.getExternalLoanId());
        assertEquals(reversalExternalId, data.getReversalExternalId());

        // Financial amounts
        assertEquals(netDisbursalAmount, data.getNetDisbursalAmount());
        assertEquals(principalPortion, data.getPrincipalPortion());
        assertEquals(interestPortion, data.getInterestPortion());
        assertEquals(feeChargesPortion, data.getFeeChargesPortion());
        assertEquals(penaltyChargesPortion, data.getPenaltyChargesPortion());
        assertEquals(overpaymentPortion, data.getOverpaymentPortion());
        assertEquals(unrecognizedIncomePortion, data.getUnrecognizedIncomePortion());
        assertEquals(fixedEmiAmount, data.getFixedEmiAmount());
        assertEquals(availableDisbursementAmountWithOverApplied, data.getAvailableDisbursementAmountWithOverApplied());

        // Dates
        assertEquals(possibleNextRepaymentDate, data.getPossibleNextRepaymentDate());
        assertEquals(reversedOnDate, data.getReversedOnDate());
        assertEquals(submittedOnDate, data.getSubmittedOnDate());

        // Collections
        assertEquals(transactionRelations, data.getTransactionRelations());
        assertEquals(loanChargePaidByList, data.getLoanChargePaidByList());
        assertEquals(loanRepaymentScheduleInstallments, data.getLoanRepaymentScheduleInstallments());
        assertEquals(writeOffReasonOptions, data.getWriteOffReasonOptions());
        assertEquals(chargeOffReasonOptions, data.getChargeOffReasonOptions());
        assertEquals(paymentTypeOptions, data.getPaymentTypeOptions());
        assertEquals(transfer, data.getTransfer());
    }

    @Test
    public void testImportInstanceSimple() {
        // Given
        BigDecimal repaymentAmount = new BigDecimal("1500.75");
        LocalDate lastRepaymentDate = LocalDate.of(2023, 6, 15);
        Long repaymentTypeId = 1L;
        Integer rowIndex = 1;
        String locale = "en";
        String dateFormat = "dd MMMM yyyy";

        // When
        LoanTransactionData data = LoanTransactionData.importInstance(repaymentAmount, lastRepaymentDate, repaymentTypeId, rowIndex, locale,
                dateFormat);

        // Then
        assertEquals(repaymentAmount, data.getTransactionAmount());
        assertEquals(lastRepaymentDate, data.getTransactionDate());
        assertEquals(repaymentTypeId, data.getPaymentTypeId());
        assertEquals(rowIndex, data.getRowIndex());
        assertEquals(locale, data.getLocale());
        assertEquals(dateFormat, data.getDateFormat());
        assertFalse(data.isManuallyReversed());
        assertEquals(ExternalId.empty(), data.getExternalId());
        assertEquals(ExternalId.empty(), data.getExternalLoanId());
        assertEquals(ExternalId.empty(), data.getReversalExternalId());
    }

    @Test
    public void testImportInstanceWithAllFields() {
        // Given
        BigDecimal repaymentAmount = new BigDecimal("2000.50");
        LocalDate repaymentDate = LocalDate.of(2023, 7, 20);
        Long repaymentTypeId = 2L;
        String accountNumber = "ACC123";
        Integer checkNumber = 1001;
        Integer routingCode = 12345;
        Integer receiptNumber = 5001;
        Integer bankNumber = 10;
        Long loanAccountId = 100L;
        String transactionType = "REPAYMENT";
        Integer rowIndex = 2;
        String locale = "en";
        String dateFormat = "dd MMMM yyyy";

        // When
        LoanTransactionData data = LoanTransactionData.importInstance(repaymentAmount, repaymentDate, repaymentTypeId, accountNumber,
                checkNumber, routingCode, receiptNumber, bankNumber, loanAccountId, transactionType, rowIndex, locale, dateFormat);

        // Then
        assertEquals(repaymentAmount, data.getTransactionAmount());
        assertEquals(repaymentDate, data.getTransactionDate());
        assertEquals(repaymentTypeId, data.getPaymentTypeId());
        assertEquals(accountNumber, data.getAccountNumber());
        assertEquals(checkNumber, data.getCheckNumber());
        assertEquals(routingCode, data.getRoutingCode());
        assertEquals(receiptNumber, data.getReceiptNumber());
        assertEquals(bankNumber, data.getBankNumber());
        assertEquals(loanAccountId, data.getAccountId());
        assertEquals(transactionType, data.getTransactionType());
        assertEquals(rowIndex, data.getRowIndex());
        assertEquals(locale, data.getLocale());
        assertEquals(dateFormat, data.getDateFormat());
        assertFalse(data.isManuallyReversed());
        assertEquals(ExternalId.empty(), data.getExternalId());
        assertEquals(ExternalId.empty(), data.getExternalLoanId());
        assertEquals(ExternalId.empty(), data.getReversalExternalId());
    }

    @Test
    public void testTemplateOnTopWithPaymentOptions() {
        // Given
        LoanTransactionData original = createSampleTransactionData();
        Collection<PaymentTypeData> newPaymentOptions = mock(Collection.class);

        // When
        LoanTransactionData result = LoanTransactionData.templateOnTop(original, newPaymentOptions);

        // Then
        assertBasicFieldsCopied(original, result);
        assertEquals(newPaymentOptions, result.getPaymentTypeOptions());
    }

    @Test
    public void testTemplateOnTopWithTransactionType() {
        // Given
        LoanTransactionData original = createSampleTransactionData();
        LoanTransactionEnumData newType = new LoanTransactionEnumData(2L, "code", "NEW_TYPE");

        // When
        LoanTransactionData result = LoanTransactionData.templateOnTop(original, newType);

        // Then
        assertBasicFieldsCopied(original, result);
        assertEquals(newType, result.getType());
    }

    @Test
    public void testLoanTransactionDataForCreditTemplate() {
        // Given
        LoanTransactionEnumData transactionType = new LoanTransactionEnumData(1L, "code", "CREDIT");
        LocalDate transactionDate = LocalDate.of(2023, 8, 1);
        BigDecimal transactionAmount = new BigDecimal("3000.00");
        Collection<PaymentTypeData> paymentOptions = mock(Collection.class);
        CurrencyData currency = new CurrencyData("USD", "US Dollar", 2, 0, "$", "USD");
        List<CodeValueData> classificationOptions = mock(List.class);

        // When
        LoanTransactionData result = LoanTransactionData.loanTransactionDataForCreditTemplate(transactionType, transactionDate,
                transactionAmount, paymentOptions, currency, classificationOptions);

        // Then
        assertEquals(transactionType, result.getType());
        assertEquals(transactionDate, result.getDate());
        assertEquals(transactionAmount, result.getAmount());
        assertEquals(paymentOptions, result.getPaymentTypeOptions());
        assertEquals(currency, result.getCurrency());
        assertFalse(result.isManuallyReversed());
        assertEquals(ExternalId.empty(), result.getExternalId());
        assertEquals(ExternalId.empty(), result.getExternalLoanId());
        assertEquals(ExternalId.empty(), result.getReversalExternalId());
        assertEquals(classificationOptions, result.getClassificationOptions());
    }

    @Test
    public void testLoanTransactionDataForDisbursalTemplate() {
        // Given
        LoanTransactionEnumData transactionType = new LoanTransactionEnumData(1L, "code", "DISBURSAL");
        LocalDate expectedDisbursedOn = LocalDate.of(2023, 8, 1);
        BigDecimal disburseAmount = new BigDecimal("5000.00");
        BigDecimal netDisbursalAmount = new BigDecimal("4950.00");
        Collection<PaymentTypeData> paymentOptions = mock(Collection.class);
        BigDecimal fixedEmiAmount = new BigDecimal("500.00");
        LocalDate possibleNextRepaymentDate = LocalDate.of(2023, 9, 1);
        CurrencyData currency = new CurrencyData("USD", "US Dollar", 2, 0, "$", "USD");
        BigDecimal availableDisbursementAmount = new BigDecimal("10000.00");

        // When
        LoanTransactionData result = LoanTransactionData.loanTransactionDataForDisbursalTemplate(transactionType, expectedDisbursedOn,
                disburseAmount, netDisbursalAmount, paymentOptions, fixedEmiAmount, possibleNextRepaymentDate, currency,
                availableDisbursementAmount);

        // Then
        assertEquals(transactionType, result.getType());
        assertEquals(expectedDisbursedOn, result.getDate());
        assertEquals(disburseAmount, result.getAmount());
        assertEquals(netDisbursalAmount, result.getNetDisbursalAmount());
        assertEquals(paymentOptions, result.getPaymentTypeOptions());
        assertEquals(fixedEmiAmount, result.getFixedEmiAmount());
        assertEquals(possibleNextRepaymentDate, result.getPossibleNextRepaymentDate());
        assertEquals(currency, result.getCurrency());
        assertEquals(availableDisbursementAmount, result.getAvailableDisbursementAmountWithOverApplied());
        assertFalse(result.isManuallyReversed());
        assertEquals(ExternalId.empty(), result.getExternalId());
        assertEquals(ExternalId.empty(), result.getExternalLoanId());
        assertEquals(ExternalId.empty(), result.getReversalExternalId());
    }

    @Test
    public void testLoanTransactionDataWithNullValues() {
        // When
        LoanTransactionData data = LoanTransactionData.builder().build();

        // Then - Simple fields
        assertNull(data.getId());
        assertNull(data.getOfficeId());
        assertNull(data.getOfficeName());
        assertNull(data.getDate());
        assertNull(data.getAmount());
        assertFalse(data.isManuallyReversed()); // Default boolean
        assertNull(data.getLoanId());
        assertNull(data.getTransactionType());
        assertNull(data.getRowIndex());
        assertNull(data.getDateFormat());
        assertNull(data.getLocale());
        assertNull(data.getPaymentTypeId());
        assertNull(data.getAccountNumber());
        assertNull(data.getCheckNumber());
        assertNull(data.getRoutingCode());
        assertNull(data.getReceiptNumber());
        assertNull(data.getBankNumber());
        assertNull(data.getAccountId());
        assertNull(data.getTransactionAmount());
        assertNull(data.getNumberOfRepayments());

        // Complex fields
        assertNull(data.getType());
        assertNull(data.getPaymentDetailData());
        assertNull(data.getCurrency());
        assertNull(data.getExternalId());
        assertNull(data.getExternalLoanId());
        assertNull(data.getReversalExternalId());

        // Financial amounts
        assertNull(data.getNetDisbursalAmount());
        assertNull(data.getPrincipalPortion());
        assertNull(data.getInterestPortion());
        assertNull(data.getFeeChargesPortion());
        assertNull(data.getPenaltyChargesPortion());
        assertNull(data.getOverpaymentPortion());
        assertNull(data.getUnrecognizedIncomePortion());
        assertNull(data.getFixedEmiAmount());
        assertNull(data.getAvailableDisbursementAmountWithOverApplied());

        // Dates
        assertNull(data.getPossibleNextRepaymentDate());
        assertNull(data.getReversedOnDate());
        assertNull(data.getSubmittedOnDate());

        // Collections
        assertNull(data.getTransactionRelations());
        assertNull(data.getLoanChargePaidByList());
        assertNull(data.getLoanRepaymentScheduleInstallments());
        assertNull(data.getWriteOffReasonOptions());
        assertNull(data.getChargeOffReasonOptions());
        assertNull(data.getPaymentTypeOptions());
        assertNull(data.getTransfer());
    }

    // Helper methods
    private LoanTransactionData createSampleTransactionData() {
        return LoanTransactionData.builder().id(1L).officeId(1L).officeName("Test Office")
                .type(new LoanTransactionEnumData(1L, "code", "REPAYMENT")).date(LocalDate.of(2023, 1, 1)).amount(new BigDecimal("1000.00"))
                .currency(new CurrencyData("USD", "US Dollar", 2, 0, "$", "USD")).paymentDetailData(mock(PaymentDetailData.class))
                .externalId(ExternalId.generate()).externalLoanId(ExternalId.generate()).reversalExternalId(ExternalId.generate())
                .manuallyReversed(false).loanId(1L).build();
    }

    private void assertBasicFieldsCopied(LoanTransactionData original, LoanTransactionData result) {
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getOfficeId(), result.getOfficeId());
        assertEquals(original.getOfficeName(), result.getOfficeName());
        assertEquals(original.getPaymentDetailData(), result.getPaymentDetailData());
        assertEquals(original.getCurrency(), result.getCurrency());
        assertEquals(original.getDate(), result.getDate());
        assertEquals(original.getAmount(), result.getAmount());
        assertEquals(original.getNetDisbursalAmount(), result.getNetDisbursalAmount());
        assertEquals(original.getPrincipalPortion(), result.getPrincipalPortion());
        assertEquals(original.getInterestPortion(), result.getInterestPortion());
        assertEquals(original.getFeeChargesPortion(), result.getFeeChargesPortion());
        assertEquals(original.getPenaltyChargesPortion(), result.getPenaltyChargesPortion());
        assertEquals(original.getOverpaymentPortion(), result.getOverpaymentPortion());
        assertEquals(original.getUnrecognizedIncomePortion(), result.getUnrecognizedIncomePortion());
        assertEquals(original.getExternalId(), result.getExternalId());
        assertEquals(original.getTransfer(), result.getTransfer());
        assertEquals(original.getFixedEmiAmount(), result.getFixedEmiAmount());
        assertEquals(original.getOutstandingLoanBalance(), result.getOutstandingLoanBalance());
        assertEquals(original.isManuallyReversed(), result.isManuallyReversed());
        assertEquals(original.getLoanId(), result.getLoanId());
        assertEquals(original.getExternalLoanId(), result.getExternalLoanId());
    }

}
