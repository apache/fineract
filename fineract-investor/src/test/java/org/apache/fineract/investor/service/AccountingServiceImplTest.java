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
package org.apache.fineract.investor.service;

import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.investor.accounting.journalentry.service.InvestorAccountingHelper;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerJournalEntryMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerJournalEntryMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferJournalEntryMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferJournalEntryMappingRepository;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountingServiceImplTest {

    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);

    @BeforeAll
    public static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    public static void destruct() {
        MONEY_HELPER.close();
    }

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BUSINESS_DATE, LocalDate.of(2024, 9, 27))));
    }

    @Test
    void createJournalEntriesForSaleAssetTransfer() {
        // given
        TestContext testContext = new TestContext();
        Loan loan = testContext.createMockedLoan();

        ExternalAssetOwner previousOwner = new ExternalAssetOwner();
        ExternalAssetOwner newOwner = new ExternalAssetOwner();
        ExternalAssetOwnerTransfer transfer = new ExternalAssetOwnerTransfer();
        transfer.setOwner(newOwner);

        JournalEntry principleAndInterestDebitJournalEntry = createJournalEntry(11, testContext.principleAndInterestAccount,
                JournalEntryType.DEBIT);
        JournalEntry principleAndInterestCreditJournalEntry = createJournalEntry(12, testContext.principleAndInterestAccount,
                JournalEntryType.CREDIT);
        JournalEntry feeAndPenaltyDebitJournalEntry = createJournalEntry(13, testContext.feeAndPenaltyAccount, JournalEntryType.DEBIT);
        JournalEntry feeAndPenaltyCreditJournalEntry = createJournalEntry(14, testContext.feeAndPenaltyAccount, JournalEntryType.CREDIT);
        JournalEntry transferDebitJournalEntry = createJournalEntry(15, testContext.transferAccount, JournalEntryType.DEBIT);
        JournalEntry transferCreditJournalEntry = createJournalEntry(16, testContext.transferAccount, JournalEntryType.CREDIT);

        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(false), eq(testContext.principleAndInterestAccount))).thenReturn(principleAndInterestCreditJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(false), eq(testContext.feeAndPenaltyAccount))).thenReturn(feeAndPenaltyCreditJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(false))).thenReturn(transferDebitJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(true), eq(testContext.principleAndInterestAccount))).thenReturn(principleAndInterestDebitJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(true), eq(testContext.feeAndPenaltyAccount))).thenReturn(feeAndPenaltyDebitJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(true))).thenReturn(transferCreditJournalEntry);

        // when
        testContext.testSubject.createJournalEntriesForSaleAssetTransfer(loan, transfer, previousOwner);

        // then
        ArgumentCaptor<ExternalAssetOwnerJournalEntryMapping> ownerJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerJournalEntryMappingRepository, times(4))
                .saveAndFlush(ownerJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerJournalEntryMapping> capturedOwnerJournalEntryMappings = ownerJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerJournalEntryMapping> expectedOwnerJournalEntryMappings = List.of(
                ownerJournalEntryMapping(principleAndInterestCreditJournalEntry, previousOwner),
                ownerJournalEntryMapping(feeAndPenaltyCreditJournalEntry, previousOwner),
                ownerJournalEntryMapping(principleAndInterestDebitJournalEntry, newOwner),
                ownerJournalEntryMapping(feeAndPenaltyDebitJournalEntry, newOwner));
        assertNotNull(capturedOwnerJournalEntryMappings);
        assertOwnerJournalEntryMappings(expectedOwnerJournalEntryMappings, capturedOwnerJournalEntryMappings);

        ArgumentCaptor<ExternalAssetOwnerTransferJournalEntryMapping> transferJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransferJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerTransferJournalEntryMappingRepository, times(6))
                .saveAndFlush(transferJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerTransferJournalEntryMapping> capturedTransferJournalEntryMappings = transferJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerTransferJournalEntryMapping> expectedTransferJournalEntryMappings = List.of(
                transferJournalEntryMapping(principleAndInterestCreditJournalEntry, transfer),
                transferJournalEntryMapping(feeAndPenaltyCreditJournalEntry, transfer),
                transferJournalEntryMapping(transferDebitJournalEntry, transfer),
                transferJournalEntryMapping(principleAndInterestDebitJournalEntry, transfer),
                transferJournalEntryMapping(feeAndPenaltyDebitJournalEntry, transfer),
                transferJournalEntryMapping(transferCreditJournalEntry, transfer));
        assertNotNull(capturedTransferJournalEntryMappings);
        assertTransferJournalEntryMappings(expectedTransferJournalEntryMappings, capturedTransferJournalEntryMappings);
    }

    @Test
    void createJournalEntriesForSaleAssetTransferOfOverpaidLoan() {
        // given
        TestContext testContext = new TestContext();
        Loan loan = testContext.createMockedOverpaidLoan();

        ExternalAssetOwner previousOwner = new ExternalAssetOwner();
        ExternalAssetOwner newOwner = new ExternalAssetOwner();
        ExternalAssetOwnerTransfer transfer = new ExternalAssetOwnerTransfer();
        transfer.setOwner(newOwner);

        JournalEntry overpaidDebitJournalEntry = createJournalEntry(11, testContext.overpaymentAccount, JournalEntryType.DEBIT);
        JournalEntry overpaidCreditJournalEntry = createJournalEntry(12, testContext.overpaymentAccount, JournalEntryType.CREDIT);
        JournalEntry transferDebitJournalEntry = createJournalEntry(15, testContext.transferAccount, JournalEntryType.DEBIT);
        JournalEntry transferCreditJournalEntry = createJournalEntry(16, testContext.transferAccount, JournalEntryType.CREDIT);

        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(false), eq(testContext.overpaymentAccount))).thenReturn(overpaidCreditJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(false))).thenReturn(transferDebitJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(true), eq(testContext.overpaymentAccount))).thenReturn(overpaidDebitJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(true))).thenReturn(transferCreditJournalEntry);

        // when
        testContext.testSubject.createJournalEntriesForSaleAssetTransfer(loan, transfer, previousOwner);

        // then
        ArgumentCaptor<ExternalAssetOwnerJournalEntryMapping> ownerJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerJournalEntryMappingRepository, times(2))
                .saveAndFlush(ownerJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerJournalEntryMapping> capturedOwnerJournalEntryMappings = ownerJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerJournalEntryMapping> expectedOwnerJournalEntryMappings = List.of(
                ownerJournalEntryMapping(overpaidCreditJournalEntry, newOwner),
                ownerJournalEntryMapping(overpaidDebitJournalEntry, previousOwner));
        assertNotNull(capturedOwnerJournalEntryMappings);
        assertOwnerJournalEntryMappings(expectedOwnerJournalEntryMappings, capturedOwnerJournalEntryMappings);

        ArgumentCaptor<ExternalAssetOwnerTransferJournalEntryMapping> transferJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransferJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerTransferJournalEntryMappingRepository, times(4))
                .saveAndFlush(transferJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerTransferJournalEntryMapping> capturedTransferJournalEntryMappings = transferJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerTransferJournalEntryMapping> expectedTransferJournalEntryMappings = List.of(
                transferJournalEntryMapping(overpaidCreditJournalEntry, transfer),
                transferJournalEntryMapping(transferDebitJournalEntry, transfer),
                transferJournalEntryMapping(overpaidDebitJournalEntry, transfer),
                transferJournalEntryMapping(transferCreditJournalEntry, transfer));
        assertNotNull(capturedTransferJournalEntryMappings);
        assertTransferJournalEntryMappings(expectedTransferJournalEntryMappings, capturedTransferJournalEntryMappings);
    }

    @Test
    void createJournalEntriesForSaleAssetTransferWithNullPreviousOwner() {
        // given
        TestContext testContext = new TestContext();
        Loan loan = testContext.createMockedLoan();

        ExternalAssetOwner newOwner = new ExternalAssetOwner();
        ExternalAssetOwnerTransfer transfer = new ExternalAssetOwnerTransfer();
        transfer.setOwner(newOwner);

        JournalEntry principleAndInterestDebitJournalEntry = createJournalEntry(11, testContext.principleAndInterestAccount,
                JournalEntryType.DEBIT);
        JournalEntry principleAndInterestCreditJournalEntry = createJournalEntry(12, testContext.principleAndInterestAccount,
                JournalEntryType.CREDIT);
        JournalEntry feeAndPenaltyDebitJournalEntry = createJournalEntry(13, testContext.feeAndPenaltyAccount, JournalEntryType.DEBIT);
        JournalEntry feeAndPenaltyCreditJournalEntry = createJournalEntry(14, testContext.feeAndPenaltyAccount, JournalEntryType.CREDIT);
        JournalEntry transferDebitJournalEntry = createJournalEntry(15, testContext.transferAccount, JournalEntryType.DEBIT);
        JournalEntry transferCreditJournalEntry = createJournalEntry(16, testContext.transferAccount, JournalEntryType.CREDIT);

        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(false), eq(testContext.principleAndInterestAccount))).thenReturn(principleAndInterestCreditJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(false), eq(testContext.feeAndPenaltyAccount))).thenReturn(feeAndPenaltyCreditJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(false))).thenReturn(transferDebitJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(true), eq(testContext.principleAndInterestAccount))).thenReturn(principleAndInterestDebitJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(true), eq(testContext.feeAndPenaltyAccount))).thenReturn(feeAndPenaltyDebitJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(true))).thenReturn(transferCreditJournalEntry);

        // when
        testContext.testSubject.createJournalEntriesForSaleAssetTransfer(loan, transfer, null);

        // then
        ArgumentCaptor<ExternalAssetOwnerJournalEntryMapping> ownerJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerJournalEntryMappingRepository, times(2))
                .saveAndFlush(ownerJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerJournalEntryMapping> capturedOwnerJournalEntryMappings = ownerJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerJournalEntryMapping> expectedOwnerJournalEntryMappings = List.of(
                ownerJournalEntryMapping(principleAndInterestDebitJournalEntry, newOwner),
                ownerJournalEntryMapping(feeAndPenaltyDebitJournalEntry, newOwner));
        assertNotNull(capturedOwnerJournalEntryMappings);
        assertOwnerJournalEntryMappings(expectedOwnerJournalEntryMappings, capturedOwnerJournalEntryMappings);

        ArgumentCaptor<ExternalAssetOwnerTransferJournalEntryMapping> transferJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransferJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerTransferJournalEntryMappingRepository, times(6))
                .saveAndFlush(transferJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerTransferJournalEntryMapping> capturedTransferJournalEntryMappings = transferJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerTransferJournalEntryMapping> expectedTransferJournalEntryMappings = List.of(
                transferJournalEntryMapping(principleAndInterestCreditJournalEntry, transfer),
                transferJournalEntryMapping(feeAndPenaltyCreditJournalEntry, transfer),
                transferJournalEntryMapping(transferDebitJournalEntry, transfer),
                transferJournalEntryMapping(principleAndInterestDebitJournalEntry, transfer),
                transferJournalEntryMapping(feeAndPenaltyDebitJournalEntry, transfer),
                transferJournalEntryMapping(transferCreditJournalEntry, transfer));
        assertNotNull(capturedTransferJournalEntryMappings);
        assertTransferJournalEntryMappings(expectedTransferJournalEntryMappings, capturedTransferJournalEntryMappings);
    }

    @Test
    void createJournalEntriesForSaleAssetTransferOfOverpaidLoanWithNullPreviousOwner() {
        // given
        TestContext testContext = new TestContext();
        Loan loan = testContext.createMockedOverpaidLoan();

        ExternalAssetOwner newOwner = new ExternalAssetOwner();
        ExternalAssetOwnerTransfer transfer = new ExternalAssetOwnerTransfer();
        transfer.setOwner(newOwner);

        JournalEntry overpaidDebitJournalEntry = createJournalEntry(11, testContext.overpaymentAccount, JournalEntryType.DEBIT);
        JournalEntry overpaidCreditJournalEntry = createJournalEntry(12, testContext.overpaymentAccount, JournalEntryType.CREDIT);
        JournalEntry transferDebitJournalEntry = createJournalEntry(15, testContext.transferAccount, JournalEntryType.DEBIT);
        JournalEntry transferCreditJournalEntry = createJournalEntry(16, testContext.transferAccount, JournalEntryType.CREDIT);

        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(false), eq(testContext.overpaymentAccount))).thenReturn(overpaidCreditJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(false))).thenReturn(transferDebitJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(true), eq(testContext.overpaymentAccount))).thenReturn(overpaidDebitJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(true))).thenReturn(transferCreditJournalEntry);

        // when
        testContext.testSubject.createJournalEntriesForSaleAssetTransfer(loan, transfer, null);

        // then
        ArgumentCaptor<ExternalAssetOwnerJournalEntryMapping> ownerJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerJournalEntryMappingRepository).saveAndFlush(ownerJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerJournalEntryMapping> capturedOwnerJournalEntryMappings = ownerJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerJournalEntryMapping> expectedOwnerJournalEntryMappings = List
                .of(ownerJournalEntryMapping(overpaidCreditJournalEntry, newOwner));
        assertNotNull(capturedOwnerJournalEntryMappings);
        assertOwnerJournalEntryMappings(expectedOwnerJournalEntryMappings, capturedOwnerJournalEntryMappings);

        ArgumentCaptor<ExternalAssetOwnerTransferJournalEntryMapping> transferJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransferJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerTransferJournalEntryMappingRepository, times(4))
                .saveAndFlush(transferJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerTransferJournalEntryMapping> capturedTransferJournalEntryMappings = transferJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerTransferJournalEntryMapping> expectedTransferJournalEntryMappings = List.of(
                transferJournalEntryMapping(overpaidCreditJournalEntry, transfer),
                transferJournalEntryMapping(transferDebitJournalEntry, transfer),
                transferJournalEntryMapping(overpaidDebitJournalEntry, transfer),
                transferJournalEntryMapping(transferCreditJournalEntry, transfer));
        assertNotNull(capturedTransferJournalEntryMappings);
        assertTransferJournalEntryMappings(expectedTransferJournalEntryMappings, capturedTransferJournalEntryMappings);
    }

    @Test
    void createJournalEntriesForBuybackAssetTransfer() {
        // given
        TestContext testContext = new TestContext();
        Loan loan = testContext.createMockedLoan();

        ExternalAssetOwner previousOwner = new ExternalAssetOwner();
        ExternalAssetOwnerTransfer transfer = new ExternalAssetOwnerTransfer();
        transfer.setOwner(previousOwner);

        JournalEntry principleAndInterestDebitJournalEntry = createJournalEntry(11, testContext.principleAndInterestAccount,
                JournalEntryType.DEBIT);
        JournalEntry principleAndInterestCreditJournalEntry = createJournalEntry(12, testContext.principleAndInterestAccount,
                JournalEntryType.CREDIT);
        JournalEntry feeAndPenaltyDebitJournalEntry = createJournalEntry(13, testContext.feeAndPenaltyAccount, JournalEntryType.DEBIT);
        JournalEntry feeAndPenaltyCreditJournalEntry = createJournalEntry(14, testContext.feeAndPenaltyAccount, JournalEntryType.CREDIT);
        JournalEntry transferDebitJournalEntry = createJournalEntry(15, testContext.transferAccount, JournalEntryType.DEBIT);
        JournalEntry transferCreditJournalEntry = createJournalEntry(16, testContext.transferAccount, JournalEntryType.CREDIT);

        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(false), eq(testContext.principleAndInterestAccount))).thenReturn(principleAndInterestCreditJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(false), eq(testContext.feeAndPenaltyAccount))).thenReturn(feeAndPenaltyCreditJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(false))).thenReturn(transferDebitJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(true), eq(testContext.principleAndInterestAccount))).thenReturn(principleAndInterestDebitJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(true), eq(testContext.feeAndPenaltyAccount))).thenReturn(feeAndPenaltyDebitJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(true))).thenReturn(transferCreditJournalEntry);

        // when
        testContext.testSubject.createJournalEntriesForBuybackAssetTransfer(loan, transfer);

        // then
        ArgumentCaptor<ExternalAssetOwnerJournalEntryMapping> ownerJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerJournalEntryMappingRepository, times(2))
                .saveAndFlush(ownerJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerJournalEntryMapping> capturedOwnerJournalEntryMappings = ownerJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerJournalEntryMapping> expectedOwnerJournalEntryMappings = List.of(
                ownerJournalEntryMapping(principleAndInterestCreditJournalEntry, previousOwner),
                ownerJournalEntryMapping(feeAndPenaltyCreditJournalEntry, previousOwner));
        assertNotNull(capturedOwnerJournalEntryMappings);
        assertOwnerJournalEntryMappings(expectedOwnerJournalEntryMappings, capturedOwnerJournalEntryMappings);

        ArgumentCaptor<ExternalAssetOwnerTransferJournalEntryMapping> transferJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransferJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerTransferJournalEntryMappingRepository, times(6))
                .saveAndFlush(transferJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerTransferJournalEntryMapping> capturedTransferJournalEntryMappings = transferJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerTransferJournalEntryMapping> expectedTransferJournalEntryMappings = List.of(
                transferJournalEntryMapping(principleAndInterestDebitJournalEntry, transfer),
                transferJournalEntryMapping(feeAndPenaltyDebitJournalEntry, transfer),
                transferJournalEntryMapping(transferCreditJournalEntry, transfer),
                transferJournalEntryMapping(principleAndInterestCreditJournalEntry, transfer),
                transferJournalEntryMapping(feeAndPenaltyCreditJournalEntry, transfer),
                transferJournalEntryMapping(transferDebitJournalEntry, transfer));
        assertNotNull(capturedTransferJournalEntryMappings);
        assertTransferJournalEntryMappings(expectedTransferJournalEntryMappings, capturedTransferJournalEntryMappings);
    }

    @Test
    void createJournalEntriesForBuybackAssetTransferOfOverpaidLoan() {
        // given
        TestContext testContext = new TestContext();
        Loan loan = testContext.createMockedOverpaidLoan();

        ExternalAssetOwner previousOwner = new ExternalAssetOwner();
        ExternalAssetOwnerTransfer transfer = new ExternalAssetOwnerTransfer();
        transfer.setOwner(previousOwner);

        JournalEntry overpaidDebitJournalEntry = createJournalEntry(11, testContext.overpaymentAccount, JournalEntryType.DEBIT);
        JournalEntry overpaidCreditJournalEntry = createJournalEntry(12, testContext.overpaymentAccount, JournalEntryType.CREDIT);
        JournalEntry transferDebitJournalEntry = createJournalEntry(15, testContext.transferAccount, JournalEntryType.DEBIT);
        JournalEntry transferCreditJournalEntry = createJournalEntry(16, testContext.transferAccount, JournalEntryType.CREDIT);

        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(false), eq(testContext.overpaymentAccount))).thenReturn(overpaidCreditJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(false))).thenReturn(transferDebitJournalEntry);
        when(testContext.investorAccountingHelper.createCreditJournalEntryOrReversalForInvestor(any(), any(), any(), any(), any(), any(),
                eq(true), eq(testContext.overpaymentAccount))).thenReturn(overpaidDebitJournalEntry);
        when(testContext.investorAccountingHelper.createDebitJournalEntryOrReversalForInvestor(any(), any(), anyInt(), any(), any(), any(),
                any(), any(), eq(true))).thenReturn(transferCreditJournalEntry);

        // when
        testContext.testSubject.createJournalEntriesForBuybackAssetTransfer(loan, transfer);

        // then
        ArgumentCaptor<ExternalAssetOwnerJournalEntryMapping> ownerJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerJournalEntryMappingRepository, times(1))
                .saveAndFlush(ownerJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerJournalEntryMapping> capturedOwnerJournalEntryMappings = ownerJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerJournalEntryMapping> expectedOwnerJournalEntryMappings = List
                .of(ownerJournalEntryMapping(overpaidDebitJournalEntry, previousOwner));
        assertNotNull(capturedOwnerJournalEntryMappings);
        assertOwnerJournalEntryMappings(expectedOwnerJournalEntryMappings, capturedOwnerJournalEntryMappings);

        ArgumentCaptor<ExternalAssetOwnerTransferJournalEntryMapping> transferJournalEntryMappingArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerTransferJournalEntryMapping.class);
        verify(testContext.externalAssetOwnerTransferJournalEntryMappingRepository, times(4))
                .saveAndFlush(transferJournalEntryMappingArgumentCaptor.capture());
        List<ExternalAssetOwnerTransferJournalEntryMapping> capturedTransferJournalEntryMappings = transferJournalEntryMappingArgumentCaptor
                .getAllValues();

        List<ExternalAssetOwnerTransferJournalEntryMapping> expectedTransferJournalEntryMappings = List.of(
                transferJournalEntryMapping(overpaidDebitJournalEntry, transfer),
                transferJournalEntryMapping(transferCreditJournalEntry, transfer),
                transferJournalEntryMapping(overpaidCreditJournalEntry, transfer),
                transferJournalEntryMapping(transferDebitJournalEntry, transfer));
        assertNotNull(capturedTransferJournalEntryMappings);
        assertTransferJournalEntryMappings(expectedTransferJournalEntryMappings, capturedTransferJournalEntryMappings);
    }

    private JournalEntry createJournalEntry(long id, GLAccount glAccount, JournalEntryType journalEntryType) {
        JournalEntry journalEntry = JournalEntry.createNew(null, null, glAccount, null, null, false, null, journalEntryType, null, null,
                null, null, null, null, null, null, null);
        journalEntry.setId(id);
        return journalEntry;
    }

    private ExternalAssetOwnerJournalEntryMapping ownerJournalEntryMapping(JournalEntry journalEntry, ExternalAssetOwner owner) {
        ExternalAssetOwnerJournalEntryMapping ownerJournalEntryMapping = new ExternalAssetOwnerJournalEntryMapping();
        ownerJournalEntryMapping.setJournalEntry(journalEntry);
        ownerJournalEntryMapping.setOwner(owner);
        return ownerJournalEntryMapping;
    }

    private void assertOwnerJournalEntryMappings(List<ExternalAssetOwnerJournalEntryMapping> expectedOwnerJournalEntryMappings,
            List<ExternalAssetOwnerJournalEntryMapping> actualOwnerJournalEntryMappings) {
        assertEquals(expectedOwnerJournalEntryMappings.size(), actualOwnerJournalEntryMappings.size());
        for (int i = 0; i < expectedOwnerJournalEntryMappings.size(); i++) {
            assertOwnerJournalEntryMapping(expectedOwnerJournalEntryMappings.get(i), actualOwnerJournalEntryMappings.get(i));
        }
    }

    private void assertOwnerJournalEntryMapping(ExternalAssetOwnerJournalEntryMapping expectedOwnerJournalEntryMapping,
            ExternalAssetOwnerJournalEntryMapping actualOwnerJournalEntryMapping) {
        assertNotNull(actualOwnerJournalEntryMapping);
        assertSame(expectedOwnerJournalEntryMapping.getJournalEntry(), actualOwnerJournalEntryMapping.getJournalEntry());
        assertSame(expectedOwnerJournalEntryMapping.getOwner(), actualOwnerJournalEntryMapping.getOwner());
    }

    private ExternalAssetOwnerTransferJournalEntryMapping transferJournalEntryMapping(JournalEntry journalEntry,
            ExternalAssetOwnerTransfer transfer) {
        ExternalAssetOwnerTransferJournalEntryMapping transferJournalEntryMapping = new ExternalAssetOwnerTransferJournalEntryMapping();
        transferJournalEntryMapping.setJournalEntry(journalEntry);
        transferJournalEntryMapping.setOwnerTransfer(transfer);
        return transferJournalEntryMapping;
    }

    private void assertTransferJournalEntryMappings(
            List<ExternalAssetOwnerTransferJournalEntryMapping> expectedTransferJournalEntryMappings,
            List<ExternalAssetOwnerTransferJournalEntryMapping> actualTransferJournalEntryMappings) {
        assertEquals(expectedTransferJournalEntryMappings.size(), actualTransferJournalEntryMappings.size());
        for (int i = 0; i < expectedTransferJournalEntryMappings.size(); i++) {
            assertTransferJournalEntryMapping(expectedTransferJournalEntryMappings.get(i), actualTransferJournalEntryMappings.get(i));
        }
    }

    private void assertTransferJournalEntryMapping(ExternalAssetOwnerTransferJournalEntryMapping expectedTransferJournalEntryMapping,
            ExternalAssetOwnerTransferJournalEntryMapping actualTransferJournalEntryMapping) {
        assertNotNull(actualTransferJournalEntryMapping);
        assertSame(expectedTransferJournalEntryMapping.getJournalEntry(), actualTransferJournalEntryMapping.getJournalEntry());
        assertSame(expectedTransferJournalEntryMapping.getOwnerTransfer(), actualTransferJournalEntryMapping.getOwnerTransfer());
    }

    private static class TestContext {

        private static final Long LOAN_ID = 3001L;
        private static final Long LOAN_PRODUCT_ID = 3002L;
        private static final Long TRANSFER_ID = 3003L;

        @Mock
        private InvestorAccountingHelper investorAccountingHelper;

        @Mock
        private ExternalAssetOwnerTransferJournalEntryMappingRepository externalAssetOwnerTransferJournalEntryMappingRepository;

        @Mock
        private ExternalAssetOwnerJournalEntryMappingRepository externalAssetOwnerJournalEntryMappingRepository;

        @Mock
        private FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository;

        @Mock
        private ExternalAssetOwnerTransferOutstandingInterestCalculation externalAssetOwnerTransferOutstandingInterestCalculation;

        @InjectMocks
        private AccountingServiceImpl testSubject;

        private final GLAccount principleAndInterestAccount = new GLAccount();
        private final GLAccount feeAndPenaltyAccount = new GLAccount();
        private final GLAccount transferAccount = new GLAccount();
        private final GLAccount overpaymentAccount = new GLAccount();

        TestContext() {
            MockitoAnnotations.openMocks(this);
            setupAccounts();
        }

        public Loan createMockedLoan() {
            Loan loan = mock(Loan.class);
            when(loan.getId()).thenReturn(LOAN_ID);
            when(loan.productId()).thenReturn(LOAN_PRODUCT_ID);

            Office office = Office.headOffice("office", LocalDate.of(2024, 9, 27), new ExternalId("officeId"));
            when(loan.getOffice()).thenReturn(office);

            LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
            when(loanSummary.getTotalPrincipalOutstanding()).thenReturn(BigDecimal.ONE);
            when(loanSummary.getTotalFeeChargesOutstanding()).thenReturn(BigDecimal.ONE);
            when(loanSummary.getTotalPenaltyChargesOutstanding()).thenReturn(BigDecimal.ONE);
            when(loan.getSummary()).thenReturn(loanSummary);

            return loan;
        }

        public Loan createMockedOverpaidLoan() {
            Loan loan = mock(Loan.class);
            when(loan.getId()).thenReturn(LOAN_ID);
            when(loan.productId()).thenReturn(LOAN_PRODUCT_ID);
            when(loan.getStatus()).thenReturn(LoanStatus.OVERPAID);

            Office office = Office.headOffice("office", LocalDate.of(2024, 9, 27), new ExternalId("officeId"));
            when(loan.getOffice()).thenReturn(office);

            LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
            when(loan.getSummary()).thenReturn(loanSummary);

            when(loan.getTotalOverpaid()).thenReturn(BigDecimal.ONE);

            return loan;
        }

        private void setupAccounts() {
            principleAndInterestAccount.setId(1L);
            feeAndPenaltyAccount.setId(2L);
            transferAccount.setId(3L);
            overpaymentAccount.setId(4L);

            FinancialActivityAccount financialActivityAccount = new FinancialActivityAccount(transferAccount,
                    AccountingConstants.FinancialActivity.ASSET_TRANSFER.getValue());
            when(financialActivityAccountRepository
                    .findByFinancialActivityTypeWithNotFoundDetection(AccountingConstants.FinancialActivity.ASSET_TRANSFER.getValue()))
                    .thenReturn(financialActivityAccount);

            lenient().when(investorAccountingHelper.getLinkedGLAccountForLoanProduct(LOAN_PRODUCT_ID,
                    AccountingConstants.AccrualAccountsForLoan.LOAN_PORTFOLIO.getValue())).thenReturn(principleAndInterestAccount);
            lenient()
                    .when(investorAccountingHelper.getLinkedGLAccountForLoanProduct(LOAN_PRODUCT_ID,
                            AccountingConstants.AccrualAccountsForLoan.INTEREST_RECEIVABLE.getValue()))
                    .thenReturn(principleAndInterestAccount);
            lenient().when(investorAccountingHelper.getLinkedGLAccountForLoanProduct(LOAN_PRODUCT_ID,
                    AccountingConstants.AccrualAccountsForLoan.FEES_RECEIVABLE.getValue())).thenReturn(feeAndPenaltyAccount);
            lenient().when(investorAccountingHelper.getLinkedGLAccountForLoanProduct(LOAN_PRODUCT_ID,
                    AccountingConstants.AccrualAccountsForLoan.PENALTIES_RECEIVABLE.getValue())).thenReturn(feeAndPenaltyAccount);
            lenient().when(investorAccountingHelper.getLinkedGLAccountForLoanProduct(LOAN_PRODUCT_ID,
                    AccountingConstants.AccrualAccountsForLoan.OVERPAYMENT.getValue())).thenReturn(overpaymentAccount);
        }
    }
}
