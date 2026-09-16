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
package org.apache.fineract.accounting.journalentry.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelation;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccrualWithDeferredRevenueAmortizationAccountingProcessorForWorkingCapitalLoanTest {

    private static final Long PRODUCT_ID = 10L;
    private static final Long LOAN_ID = 100L;
    private static final Long TXN_ID = 200L;
    private static final String CURRENCY_CODE = "USD";
    private static final int WORKING_CAPITAL_LOAN_ENTITY_TYPE = PortfolioProductType.WORKING_CAPITAL_LOAN.getValue();

    @Mock
    private AccountingProcessorHelper helper;
    @Mock
    private JournalEntryRepository journalEntryRepository;

    @InjectMocks
    private AccrualWithDeferredRevenueAmortizationAccountingProcessorForWorkingCapitalLoan processor;

    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanTransaction txn;
    @Mock
    private WorkingCapitalLoanTransactionAllocation allocation;
    @Mock
    private WorkingCapitalLoanProduct loanProduct;
    @Mock
    private WorkingCapitalLoanProductRelatedDetails loanProductRelatedDetails;
    @Mock
    private MonetaryCurrency currency;
    @Mock
    private Client client;
    @Mock
    private Office office;

    @Mock
    private GLAccount fundSourceGLAccount;
    @Mock
    private GLAccount loanPortfolioGLAccount;
    @Mock
    private GLAccount overpaymentGLAccount;
    @Mock
    private GLAccount feesReceivableGLAccount;
    @Mock
    private GLAccount penaltiesReceivableGLAccount;
    @Mock
    private GLAccount incomeFromRecoveryGLAccount;
    @Mock
    private GLAccount incomeFromFeesGLAccount;
    @Mock
    private GLAccount incomeFromPenaltiesGLAccount;
    @Mock
    private GLAccount incomeFromChargeOffFeesGLAccount;
    @Mock
    private GLAccount incomeFromChargeOffPenaltyGLAccount;
    @Mock
    private GLAccount goodwillCreditGLAccount;
    @Mock
    private GLAccount incomeFromGoodwillCreditFeesGLAccount;
    @Mock
    private GLAccount incomeFromGoodwillCreditPenaltyGLAccount;
    @Mock
    private GLAccount chargeOffExpenseGLAccount;
    @Mock
    private GLAccount chargeOffFraudExpenseGLAccount;
    @Mock
    private GLAccount lossesWrittenOffGLAccount;
    @Mock
    private GLAccount writeOffReasonExpenseGLAccount;
    @Mock
    private CodeValue writeOffReason;
    @Mock
    private ProductToGLAccountMapping writeOffReasonMapping;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(
                Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 5, 1), BusinessDateType.COB_DATE, LocalDate.of(2026, 4, 30))));

        lenient().when(loan.getClient()).thenReturn(client);
        lenient().when(client.getOffice()).thenReturn(office);
        lenient().when(office.getId()).thenReturn(1L);
        lenient().when(loan.getLoanProduct()).thenReturn(loanProduct);
        lenient().when(loanProduct.getId()).thenReturn(PRODUCT_ID);
        lenient().when(loan.getLoanProductRelatedDetails()).thenReturn(loanProductRelatedDetails);
        lenient().when(loanProductRelatedDetails.getCurrency()).thenReturn(currency);
        lenient().when(currency.getCode()).thenReturn(CURRENCY_CODE);
        lenient().when(loan.getCurrencyCode()).thenReturn(CURRENCY_CODE);
        lenient().when(txn.getWcLoan()).thenReturn(loan);
        lenient().when(txn.getTypeOf()).thenReturn(LoanTransactionType.REPAYMENT);
        lenient().when(loan.getId()).thenReturn(LOAN_ID);
        lenient().when(txn.getId()).thenReturn(TXN_ID);
        lenient().when(txn.getTransactionDate()).thenReturn(LocalDate.of(2026, 5, 1));
        lenient().when(txn.getPaymentDetail()).thenReturn(null);
        lenient().when(helper.getLatestClosureByBranch(anyLong())).thenReturn(null);

        lenient().when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID), eq(CashAccountsForLoan.FUND_SOURCE.getValue()),
                any())).thenReturn(fundSourceGLAccount);
        lenient().when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID),
                eq(CashAccountsForLoan.LOAN_PORTFOLIO.getValue()), any())).thenReturn(loanPortfolioGLAccount);
        lenient().when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID), eq(CashAccountsForLoan.OVERPAYMENT.getValue()),
                any())).thenReturn(overpaymentGLAccount);
        lenient().when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID),
                eq(CashAccountsForLoan.FEES_RECEIVABLE.getValue()), any())).thenReturn(feesReceivableGLAccount);
        lenient().when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID),
                eq(CashAccountsForLoan.PENALTIES_RECEIVABLE.getValue()), any())).thenReturn(penaltiesReceivableGLAccount);
        lenient().when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID),
                eq(CashAccountsForLoan.INCOME_FROM_RECOVERY.getValue()), any())).thenReturn(incomeFromRecoveryGLAccount);
        lenient().when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID),
                eq(CashAccountsForLoan.INCOME_FROM_FEES.getValue()), any())).thenReturn(incomeFromFeesGLAccount);
        lenient().when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID),
                eq(CashAccountsForLoan.INCOME_FROM_PENALTIES.getValue()), any())).thenReturn(incomeFromPenaltiesGLAccount);
        lenient()
                .when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID),
                        eq(CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES.getValue()), any()))
                .thenReturn(incomeFromChargeOffFeesGLAccount);
        lenient().when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID),
                eq(CashAccountsForLoan.LOSSES_WRITTEN_OFF.getValue()), any())).thenReturn(lossesWrittenOffGLAccount);
    }

    private void mockChargeAdjustmentRelation(final boolean penaltyCharge) {
        final WorkingCapitalLoanCharge charge = org.mockito.Mockito.mock(WorkingCapitalLoanCharge.class);
        lenient().when(charge.isPenaltyCharge()).thenReturn(penaltyCharge);
        final WorkingCapitalLoanTransactionRelation relation = org.mockito.Mockito.mock(WorkingCapitalLoanTransactionRelation.class);
        lenient().when(relation.getToCharge()).thenReturn(charge);
        lenient().when(relation.getRelationType()).thenReturn(LoanTransactionRelationTypeEnum.CHARGE_ADJUSTMENT);
        lenient().when(txn.getLoanTransactionRelations()).thenReturn(Set.of(relation));
    }

    @AfterEach
    void tearDown() {
        MoneyHelper.clearCacheForTenant("default");
        ThreadLocalContextUtil.reset();
    }

    @Test
    void testRegularRepaymentWithFeesAndPenalties() {
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("1500"));
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("1000"));
        when(allocation.getFeeChargesPortion()).thenReturn(new BigDecimal("300"));
        when(allocation.getPenaltyChargesPortion()).thenReturn(new BigDecimal("200"));

        processor.postJournalEntries(loan, txn, allocation, false);

        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(loanPortfolioGLAccount), eq(LOAN_ID), eq(TXN_ID),
                any(), eq(new BigDecimal("1000")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(feesReceivableGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("300")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(penaltiesReceivableGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("200")), isNull());
        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(fundSourceGLAccount), eq(LOAN_ID), eq(TXN_ID),
                any(), eq(new BigDecimal("1500")), isNull());
    }

    @Test
    void testRegularRepaymentWithOverpayment() {
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("5200"));
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("5000"));
        when(allocation.getFeeChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getOverpaymentPortion()).thenReturn(new BigDecimal("200"));

        processor.postJournalEntries(loan, txn, allocation, false);

        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(loanPortfolioGLAccount), eq(LOAN_ID), eq(TXN_ID),
                any(), eq(new BigDecimal("5000")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(overpaymentGLAccount), eq(LOAN_ID), eq(TXN_ID),
                any(), eq(new BigDecimal("200")), isNull());
        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(fundSourceGLAccount), eq(LOAN_ID), eq(TXN_ID),
                any(), eq(new BigDecimal("5200")), isNull());
    }

    @Test
    void testRegularRepaymentOnAlreadyClosedOrOverpaidLoanPostsOnlyOverpaymentCredit() {
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("750"));
        when(allocation.getPrincipalPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getFeeChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getOverpaymentPortion()).thenReturn(new BigDecimal("750"));

        processor.postJournalEntries(loan, txn, allocation, false);

        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(fundSourceGLAccount), eq(LOAN_ID), eq(TXN_ID),
                any(), eq(new BigDecimal("750")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(overpaymentGLAccount), eq(LOAN_ID), eq(TXN_ID),
                any(), eq(new BigDecimal("750")), isNull());
        verify(helper, org.mockito.Mockito.never()).createCreditJournalEntryForWorkingCapitalLoan(any(), any(), eq(loanPortfolioGLAccount), any(),
                any(), any(), any(), any());
        verify(helper, org.mockito.Mockito.never()).createCreditJournalEntryForWorkingCapitalLoan(any(), any(), eq(feesReceivableGLAccount), any(),
                any(), any(), any(), any());
        verify(helper, org.mockito.Mockito.never()).createCreditJournalEntryForWorkingCapitalLoan(any(), any(), eq(penaltiesReceivableGLAccount),
                any(), any(), any(), any(), any());
    }

    @Test
    void testChargedOffRepaymentCreatesSeparateRecoveryEntries() {
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("1500"));
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("1000"));
        when(allocation.getFeeChargesPortion()).thenReturn(new BigDecimal("300"));
        when(allocation.getPenaltyChargesPortion()).thenReturn(new BigDecimal("200"));

        processor.postJournalEntries(loan, txn, allocation, true);

        // 3 separate credit entries to recovery income
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(incomeFromRecoveryGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("1000")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(incomeFromRecoveryGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("300")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(incomeFromRecoveryGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("200")), isNull());
        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(fundSourceGLAccount), eq(LOAN_ID), eq(TXN_ID),
                any(), eq(new BigDecimal("1500")), isNull());
    }

    @Test
    void testChargedOffRepaymentWithOverpayment() {
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("6000"));
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("5000"));
        when(allocation.getFeeChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getOverpaymentPortion()).thenReturn(new BigDecimal("1000"));

        processor.postJournalEntries(loan, txn, allocation, true);

        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(incomeFromRecoveryGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("5000")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(overpaymentGLAccount), eq(LOAN_ID), eq(TXN_ID),
                any(), eq(new BigDecimal("1000")), isNull());
        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(fundSourceGLAccount), eq(LOAN_ID), eq(TXN_ID),
                any(), eq(new BigDecimal("6000")), isNull());
    }

    @Test
    void testWriteOffDebitsReasonMappedExpenseAccount() {
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.WRITEOFF);
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("1000"));
        when(allocation.getFeeChargesPortion()).thenReturn(new BigDecimal("300"));
        when(allocation.getPenaltyChargesPortion()).thenReturn(new BigDecimal("200"));
        when(loan.getWriteOffReason()).thenReturn(writeOffReason);
        when(writeOffReason.getId()).thenReturn(55L);
        when(helper.getWriteOffMappingByCodeValue(PRODUCT_ID, PortfolioProductType.WORKING_CAPITAL_LOAN, 55L))
                .thenReturn(writeOffReasonMapping);
        when(writeOffReasonMapping.getGlAccount()).thenReturn(writeOffReasonExpenseGLAccount);

        processor.postJournalEntries(loan, txn, allocation, false);

        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(writeOffReasonExpenseGLAccount),
                eq(LOAN_ID), eq(TXN_ID), any(), eq(new BigDecimal("1500")), isNull());
        verify(helper, never()).createDebitJournalEntryForWorkingCapitalLoan(any(), any(), eq(lossesWrittenOffGLAccount), any(), any(),
                any(), any(), any());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(loanPortfolioGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("1000")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(feesReceivableGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("300")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(penaltiesReceivableGLAccount),
                eq(LOAN_ID), eq(TXN_ID), any(), eq(new BigDecimal("200")), isNull());
    }

    @Test
    void testWriteOffWithUnmappedReasonFallsBackToLossesWrittenOff() {
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.WRITEOFF);
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("1000"));
        when(allocation.getFeeChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(loan.getWriteOffReason()).thenReturn(writeOffReason);
        when(writeOffReason.getId()).thenReturn(55L);
        when(helper.getWriteOffMappingByCodeValue(PRODUCT_ID, PortfolioProductType.WORKING_CAPITAL_LOAN, 55L)).thenReturn(null);

        processor.postJournalEntries(loan, txn, allocation, false);

        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(lossesWrittenOffGLAccount),
                eq(LOAN_ID), eq(TXN_ID), any(), eq(new BigDecimal("1000")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(loanPortfolioGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("1000")), isNull());
    }

    @Test
    void testWriteOffWithoutReasonDebitsLossesWrittenOff() {
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.WRITEOFF);
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("1000"));
        when(allocation.getFeeChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(loan.getWriteOffReason()).thenReturn(null);

        processor.postJournalEntries(loan, txn, allocation, false);

        verify(helper, never()).getWriteOffMappingByCodeValue(any(), any(), any());
        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(lossesWrittenOffGLAccount),
                eq(LOAN_ID), eq(TXN_ID), any(), eq(new BigDecimal("1000")), isNull());
    }

    @Test
    void testReversalCreatesInverseEntriesAndMarksOriginalReversed() {
        when(txn.getReversedOnDate()).thenReturn(LocalDate.of(2026, 5, 2));

        JournalEntry originalDebit = JournalEntry.createNew(office, null, fundSourceGLAccount, CURRENCY_CODE, "WC" + TXN_ID, false,
                LocalDate.of(2026, 5, 1), JournalEntryType.DEBIT, new BigDecimal("5000"), null, WORKING_CAPITAL_LOAN_ENTITY_TYPE, LOAN_ID, null, TXN_ID, null,
                null, null);
        JournalEntry originalCredit = JournalEntry.createNew(office, null, loanPortfolioGLAccount, CURRENCY_CODE, "WC" + TXN_ID, false,
                LocalDate.of(2026, 5, 1), JournalEntryType.CREDIT, new BigDecimal("5000"), null, WORKING_CAPITAL_LOAN_ENTITY_TYPE, LOAN_ID, null, TXN_ID,
                null, null, null);

        when(journalEntryRepository.findJournalEntries("WC" + TXN_ID, WORKING_CAPITAL_LOAN_ENTITY_TYPE))
                .thenReturn(List.of(originalDebit, originalCredit));
        when(helper.persistJournalEntry(any())).thenAnswer(invocation -> invocation.getArgument(0));

        processor.postReversalJournalEntries(loan, txn);

        // 4 persists: 2 reversals + 2 originals marked reversed
        verify(helper, org.mockito.Mockito.times(4)).persistJournalEntry(any());
        assertTrue(originalDebit.isReversed());
        assertTrue(originalCredit.isReversed());
    }

    @Test
    void testCreditBalanceRefundPostsOverpaymentDebitAndFundSourceCredit() {
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.CREDIT_BALANCE_REFUND);
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("50"));
        // a booked refund is fully funded by the overpayment, which its allocation records
        when(allocation.getOverpaymentPortion()).thenReturn(new BigDecimal("50"));

        processor.postJournalEntries(loan, txn, allocation, false);

        // debit Overpayment Liability, credit Fund source
        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(overpaymentGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("50")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(fundSourceGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("50")), isNull());
    }

    @Test
    void testCreditBalanceRefundReversalSwapsToFundSourceDebitAndOverpaymentCredit() {
        when(txn.getReversedOnDate()).thenReturn(LocalDate.of(2026, 5, 3));

        final JournalEntry overpaymentDebit = JournalEntry.createNew(office, null, overpaymentGLAccount, CURRENCY_CODE, "WC" + TXN_ID, false,
                LocalDate.of(2026, 5, 1), JournalEntryType.DEBIT, new BigDecimal("50"), null, WORKING_CAPITAL_LOAN_ENTITY_TYPE, LOAN_ID,
                null, TXN_ID, null, null, null);
        final JournalEntry fundSourceCredit = JournalEntry.createNew(office, null, fundSourceGLAccount, CURRENCY_CODE, "WC" + TXN_ID, false,
                LocalDate.of(2026, 5, 1), JournalEntryType.CREDIT, new BigDecimal("50"), null, WORKING_CAPITAL_LOAN_ENTITY_TYPE, LOAN_ID,
                null, TXN_ID, null, null, null);

        when(journalEntryRepository.findJournalEntries("WC" + TXN_ID, WORKING_CAPITAL_LOAN_ENTITY_TYPE))
                .thenReturn(List.of(overpaymentDebit, fundSourceCredit));

        processor.postReversalJournalEntries(loan, txn);

        final ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(helper, org.mockito.Mockito.times(4)).persistJournalEntry(captor.capture());

        final List<JournalEntry> reversalEntries = captor.getAllValues().stream()
                .filter(entry -> entry != overpaymentDebit && entry != fundSourceCredit).toList();

        final JournalEntry overpaymentReversal = reversalEntries.stream().filter(entry -> entry.getGlAccount() == overpaymentGLAccount)
                .findFirst().orElseThrow();
        final JournalEntry fundSourceReversal = reversalEntries.stream().filter(entry -> entry.getGlAccount() == fundSourceGLAccount)
                .findFirst().orElseThrow();

        // reversal flips the original CBR direction: debit Fund source, credit Overpayment Liability
        assertTrue(fundSourceReversal.isDebitEntry());
        assertTrue(overpaymentReversal.isCreditEntry());
        assertEquals(new BigDecimal("50"), fundSourceReversal.getAmount());
        assertEquals(new BigDecimal("50"), overpaymentReversal.getAmount());
    }

    @Test
    void testAdvanceAccountingUsesPaymentChannelFundSource() {
        GLAccount paymentChannelFundSource = org.mockito.Mockito.mock(GLAccount.class);
        PaymentDetail paymentDetail = org.mockito.Mockito.mock(PaymentDetail.class);
        PaymentType paymentType = org.mockito.Mockito.mock(PaymentType.class);

        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("1000"));
        when(txn.getPaymentDetail()).thenReturn(paymentDetail);
        when(paymentDetail.getPaymentType()).thenReturn(paymentType);
        when(paymentType.getId()).thenReturn(5L);
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("1000"));
        when(allocation.getFeeChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);

        when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID), eq(CashAccountsForLoan.FUND_SOURCE.getValue()), eq(5L)))
                .thenReturn(paymentChannelFundSource);

        processor.postJournalEntries(loan, txn, allocation, false);

        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(paymentChannelFundSource),
                eq(LOAN_ID), eq(TXN_ID), any(), eq(new BigDecimal("1000")), eq(paymentDetail));
    }

    @Test
    void testChargeAdjustmentOnFeeChargeDebitsFeeIncomeAndCreditsFeeReceivable() {
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_ADJUSTMENT);
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("40"));
        mockChargeAdjustmentRelation(false);
        when(allocation.getPrincipalPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getFeeChargesPortion()).thenReturn(new BigDecimal("40"));
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);

        processor.postJournalEntries(loan, txn, allocation, false);

        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(incomeFromFeesGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("40")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(feesReceivableGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("40")), isNull());
    }

    @Test
    void testChargeAdjustmentOnPenaltyChargeDebitsPenaltyIncomeAndCreditsPenaltyReceivable() {
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_ADJUSTMENT);
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("25"));
        mockChargeAdjustmentRelation(true);
        when(allocation.getPrincipalPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getFeeChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getPenaltyChargesPortion()).thenReturn(new BigDecimal("25"));

        processor.postJournalEntries(loan, txn, allocation, false);

        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(incomeFromPenaltiesGLAccount),
                eq(LOAN_ID), eq(TXN_ID), any(), eq(new BigDecimal("25")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(penaltiesReceivableGLAccount),
                eq(LOAN_ID), eq(TXN_ID), any(), eq(new BigDecimal("25")), isNull());
    }

    @Test
    void testChargeAdjustmentSpillingOntoPrincipalDebitsFullAmountAgainstChargesOwnIncomeAccount() {
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_ADJUSTMENT);
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("60"));
        mockChargeAdjustmentRelation(false);
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("40"));
        when(allocation.getFeeChargesPortion()).thenReturn(new BigDecimal("20"));
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);

        processor.postJournalEntries(loan, txn, allocation, false);

        // Debit is the FULL transaction amount against the adjusted charge's own income account, regardless of
        // where the credit side ends up.
        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(incomeFromFeesGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("60")), isNull());
        // Credit is split by where the allocation actually landed.
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(feesReceivableGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("20")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(loanPortfolioGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("40")), isNull());
    }

    @Test
    void testChargedOffFeeChargeAdjustmentDebitsFeeIncomeAndCreditsChargeOffFeeIncome() {
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_ADJUSTMENT);
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("40"));
        mockChargeAdjustmentRelation(false);
        when(allocation.getPrincipalPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getFeeChargesPortion()).thenReturn(new BigDecimal("40"));
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);

        processor.postJournalEntries(loan, txn, allocation, true);

        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(incomeFromFeesGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("40")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(incomeFromChargeOffFeesGLAccount),
                eq(LOAN_ID), eq(TXN_ID), any(), eq(new BigDecimal("40")), isNull());
    }

    @Test
    void testChargeAdjustmentWithoutChargeLinkFailsFast() {
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.CHARGE_ADJUSTMENT);
        when(txn.getLoanTransactionRelations()).thenReturn(Set.of());
        when(allocation.getPrincipalPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getFeeChargesPortion()).thenReturn(new BigDecimal("40"));
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);

        assertThrows(IllegalStateException.class, () -> processor.postJournalEntries(loan, txn, allocation, false));
    }

    // --- restatement of a surviving transaction after a reprocess re-splits it (append-only) ---

    private JournalEntry postedEntry(final GLAccount account, final JournalEntryType type, final String amount) {
        return JournalEntry.createNew(office, null, account, CURRENCY_CODE, "WC" + TXN_ID, false, LocalDate.of(2026, 5, 1), type,
                new BigDecimal(amount), null, WORKING_CAPITAL_LOAN_ENTITY_TYPE, LOAN_ID, null, TXN_ID, null, null, null);
    }

    private void stubGLAccountIds() {
        lenient().when(fundSourceGLAccount.getId()).thenReturn(1L);
        lenient().when(loanPortfolioGLAccount.getId()).thenReturn(2L);
        lenient().when(overpaymentGLAccount.getId()).thenReturn(3L);
        lenient().when(feesReceivableGLAccount.getId()).thenReturn(4L);
        lenient().when(penaltiesReceivableGLAccount.getId()).thenReturn(5L);
    }

    private void stubLiveEntries(final JournalEntry... entries) {
        when(journalEntryRepository.findJournalEntries("WC" + TXN_ID, WORKING_CAPITAL_LOAN_ENTITY_TYPE)).thenReturn(List.of(entries));
    }

    /** A refund that partly outlived its overpayment: the remainder became newly-lent principal. */
    @Test
    void testRestateSplitsCreditBalanceRefundBetweenOverpaymentAndExcessPrincipal() {
        stubGLAccountIds();
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.CREDIT_BALANCE_REFUND);
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("300"));
        final JournalEntry overpaymentDebit = postedEntry(overpaymentGLAccount, JournalEntryType.DEBIT, "300");
        final JournalEntry fundSourceCredit = postedEntry(fundSourceGLAccount, JournalEntryType.CREDIT, "300");
        stubLiveEntries(overpaymentDebit, fundSourceCredit);
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("200"));
        when(allocation.getOverpaymentPortion()).thenReturn(new BigDecimal("100"));

        processor.restateJournalEntries(loan, txn, allocation, false);

        // the stale lines are cancelled, never deleted
        verify(journalEntryRepository, org.mockito.Mockito.never()).deleteAll(any());
        assertTrue(overpaymentDebit.isReversed());
        assertTrue(fundSourceCredit.isReversed());

        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(overpaymentGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("100")), isNull());
        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(loanPortfolioGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("200")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(fundSourceGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("300")), isNull());
    }

    /** A refund left with no overpayment behind it at all books entirely as newly-lent principal. */
    @Test
    void testRestateBooksFullyUnbackedCreditBalanceRefundAsPrincipal() {
        stubGLAccountIds();
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.CREDIT_BALANCE_REFUND);
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("1000"));
        stubLiveEntries(postedEntry(overpaymentGLAccount, JournalEntryType.DEBIT, "1000"),
                postedEntry(fundSourceGLAccount, JournalEntryType.CREDIT, "1000"));
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("1000"));
        when(allocation.getOverpaymentPortion()).thenReturn(BigDecimal.ZERO);

        processor.restateJournalEntries(loan, txn, allocation, false);

        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(loanPortfolioGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("1000")), isNull());
        // nothing is left to take off the overpayment liability
        verify(helper, org.mockito.Mockito.never()).createDebitJournalEntryForWorkingCapitalLoan(any(), any(), eq(overpaymentGLAccount),
                anyLong(), anyLong(), any(), any(), any());
    }

    /** A refund still fully funded by overpayment must keep booking to the overpayment liability. */
    @Test
    void testRestateKeepsFullyFundedCreditBalanceRefundOnOverpayment() {
        stubGLAccountIds();
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.CREDIT_BALANCE_REFUND);
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("300"));
        // ledger currently shows it as partly principal, so a restatement back to fully-funded is required
        stubLiveEntries(postedEntry(overpaymentGLAccount, JournalEntryType.DEBIT, "100"),
                postedEntry(loanPortfolioGLAccount, JournalEntryType.DEBIT, "200"),
                postedEntry(fundSourceGLAccount, JournalEntryType.CREDIT, "300"));
        when(allocation.getPrincipalPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getOverpaymentPortion()).thenReturn(new BigDecimal("300"));

        processor.restateJournalEntries(loan, txn, allocation, false);

        verify(helper).createDebitJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(overpaymentGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("300")), isNull());
        verify(helper, org.mockito.Mockito.never()).createDebitJournalEntryForWorkingCapitalLoan(any(), any(), eq(loanPortfolioGLAccount),
                anyLong(), anyLong(), any(), any(), any());
    }

    /** An unchanged split must not be re-posted, or every undo would pile cancelling noise onto untouched entries. */
    @Test
    void testRestateSkipsTransactionWhoseSplitDidNotChange() {
        stubGLAccountIds();
        when(txn.getTypeOf()).thenReturn(LoanTransactionType.CREDIT_BALANCE_REFUND);
        // the guard compares every leg of the split, the fund source credit included
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("300"));
        final JournalEntry overpaymentDebit = postedEntry(overpaymentGLAccount, JournalEntryType.DEBIT, "100");
        final JournalEntry loanPortfolioDebit = postedEntry(loanPortfolioGLAccount, JournalEntryType.DEBIT, "200");
        stubLiveEntries(overpaymentDebit, loanPortfolioDebit, postedEntry(fundSourceGLAccount, JournalEntryType.CREDIT, "300"));
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("200"));
        when(allocation.getOverpaymentPortion()).thenReturn(new BigDecimal("100"));

        processor.restateJournalEntries(loan, txn, allocation, false);

        assertTrue(!overpaymentDebit.isReversed());
        assertTrue(!loanPortfolioDebit.isReversed());
        verify(helper, org.mockito.Mockito.never()).persistJournalEntry(any());
        verify(helper, org.mockito.Mockito.never()).createDebitJournalEntryForWorkingCapitalLoan(any(), any(), any(), anyLong(), anyLong(),
                any(), any(), any());
    }

    /** A repayment whose principal/overpayment split moved is restated from the recomputed allocation. */
    @Test
    void testRestateRepaymentUsesRecomputedAllocationSplit() {
        stubGLAccountIds();
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("9100"));
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("9000"));
        when(allocation.getFeeChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getOverpaymentPortion()).thenReturn(new BigDecimal("100"));
        final JournalEntry loanPortfolioCredit = postedEntry(loanPortfolioGLAccount, JournalEntryType.CREDIT, "8800");
        stubLiveEntries(postedEntry(fundSourceGLAccount, JournalEntryType.DEBIT, "9100"), loanPortfolioCredit,
                postedEntry(overpaymentGLAccount, JournalEntryType.CREDIT, "300"));

        processor.restateJournalEntries(loan, txn, allocation, false);

        assertTrue(loanPortfolioCredit.isReversed());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(loanPortfolioGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("9000")), isNull());
        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(overpaymentGLAccount), eq(LOAN_ID),
                eq(TXN_ID), any(), eq(new BigDecimal("100")), isNull());
    }

    /**
     * A restatement's own offsetting mirrors must be flagged reversed, so they drop out of the live set. Left live, the
     * next reversal (a second restatement, or an undo of this transaction) would mirror the mirrors and leave the
     * original booking amounts standing on the ledger instead of cancelling to zero.
     */
    @Test
    void testRestateSupersedesItsOwnMirrorsSoALaterReversalCannotCompound() {
        stubGLAccountIds();
        when(txn.getTransactionAmount()).thenReturn(new BigDecimal("9100"));
        when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("9000"));
        when(allocation.getFeeChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(allocation.getOverpaymentPortion()).thenReturn(new BigDecimal("100"));
        stubLiveEntries(postedEntry(fundSourceGLAccount, JournalEntryType.DEBIT, "9100"),
                postedEntry(loanPortfolioGLAccount, JournalEntryType.CREDIT, "8800"),
                postedEntry(overpaymentGLAccount, JournalEntryType.CREDIT, "300"));

        processor.restateJournalEntries(loan, txn, allocation, false);

        final ArgumentCaptor<JournalEntry> persisted = ArgumentCaptor.forClass(JournalEntry.class);
        verify(helper, atLeastOnce()).persistJournalEntry(persisted.capture());
        assertTrue(persisted.getAllValues().stream().allMatch(JournalEntry::isReversed),
                "every entry the restatement persisted - the superseded originals and their mirrors alike - must be flagged reversed");
    }

    // -----------------------------------------------------------------------
    // Restatement guard: the plan the poster books must be exactly what the guard compares against, for every
    // transaction type and both charged-off states. Otherwise a restatement either re-posts an unchanged split
    // (cancelling noise) or, worse, skips a changed one.
    // -----------------------------------------------------------------------

    private static Stream<Arguments> restatableTransactions() {
        return Stream.of(Arguments.of(LoanTransactionType.REPAYMENT, false, false),
                Arguments.of(LoanTransactionType.REPAYMENT, true, false), Arguments.of(LoanTransactionType.GOODWILL_CREDIT, false, false),
                Arguments.of(LoanTransactionType.GOODWILL_CREDIT, true, false),
                Arguments.of(LoanTransactionType.PAYOUT_REFUND, false, false), Arguments.of(LoanTransactionType.PAYOUT_REFUND, true, false),
                Arguments.of(LoanTransactionType.PAYOUT_REFUND, true, true),
                Arguments.of(LoanTransactionType.CHARGE_ADJUSTMENT, false, false),
                Arguments.of(LoanTransactionType.CHARGE_ADJUSTMENT, true, false),
                Arguments.of(LoanTransactionType.CREDIT_BALANCE_REFUND, false, false),
                Arguments.of(LoanTransactionType.CREDIT_BALANCE_REFUND, true, false),
                Arguments.of(LoanTransactionType.ACCRUAL, false, false), Arguments.of(LoanTransactionType.ACCRUAL, true, false),
                Arguments.of(LoanTransactionType.CHARGE_OFF, true, false), Arguments.of(LoanTransactionType.CHARGE_OFF, true, true));
    }

    /**
     * Booking a transaction and then restating it from the same allocation must leave the ledger alone. This is the
     * invariant that keeps the guard honest: it holds only while the accounts the guard expects are the accounts the
     * poster books, for every type and both charged-off states.
     */
    @ParameterizedTest(name = "{0}, chargedOff={1}, fraud={2}")
    @MethodSource("restatableTransactions")
    void testPostThenRestateIsANoOp(final LoanTransactionType type, final boolean isChargedOff, final boolean isFraud) {
        stubAllGLAccounts();
        setUpTransaction(type, isFraud);

        final List<JournalEntry> booked = captureBookedEntries(() -> processor.postJournalEntries(loan, txn, allocation, isChargedOff));
        assertTrue(!booked.isEmpty(), "the poster must book something, otherwise the test proves nothing");

        clearInvocations(helper);
        stubLiveEntries(booked.toArray(new JournalEntry[0]));

        processor.restateJournalEntries(loan, txn, allocation, isChargedOff);

        assertTrue(booked.stream().noneMatch(JournalEntry::isReversed), "an unchanged split must not be reversed");
        verify(helper, never()).persistJournalEntry(any());
        verify(helper, never()).createDebitJournalEntryForWorkingCapitalLoan(any(), any(), any(), anyLong(), anyLong(), any(), any(),
                any());
        verify(helper, never()).createCreditJournalEntryForWorkingCapitalLoan(any(), any(), any(), anyLong(), anyLong(), any(), any(),
                any());
    }

    /**
     * A charge-off routes the same amounts to different accounts. Comparing amounts per portfolio/receivable account
     * alone cannot see that, so the guard has to compare positions - which account holds what, on which side.
     */
    @Test
    void testRestateDetectsRoutingChangeWithUnchangedAmounts() {
        stubAllGLAccounts();
        setUpTransaction(LoanTransactionType.REPAYMENT, false);

        // The ledger holds the regular routing; the loan is charged off now, so the credits belong to recovery income.
        stubLiveEntries(postedEntry(fundSourceGLAccount, JournalEntryType.DEBIT, "560"),
                postedEntry(loanPortfolioGLAccount, JournalEntryType.CREDIT, "500"),
                postedEntry(feesReceivableGLAccount, JournalEntryType.CREDIT, "30"),
                postedEntry(penaltiesReceivableGLAccount, JournalEntryType.CREDIT, "20"),
                postedEntry(overpaymentGLAccount, JournalEntryType.CREDIT, "10"));

        processor.restateJournalEntries(loan, txn, allocation, true);

        verify(helper).createCreditJournalEntryForWorkingCapitalLoan(eq(office), eq(CURRENCY_CODE), eq(incomeFromRecoveryGLAccount),
                eq(LOAN_ID), eq(TXN_ID), any(), eq(new BigDecimal("500")), isNull());
        verify(helper, never()).createCreditJournalEntryForWorkingCapitalLoan(any(), any(), eq(loanPortfolioGLAccount), anyLong(),
                anyLong(), any(), any(), any());
    }

    /** Sets the portions and amount up so every leg of the type under test is non-zero and the entry balances. */
    private void setUpTransaction(final LoanTransactionType type, final boolean isFraud) {
        lenient().when(txn.getTypeOf()).thenReturn(type);
        lenient().when(loan.isFraud()).thenReturn(isFraud);
        if (type == LoanTransactionType.CHARGE_ADJUSTMENT) {
            mockChargeAdjustmentRelation(false);
        }
        if (type == LoanTransactionType.CREDIT_BALANCE_REFUND) {
            // A refund debits only the overpayment and principal legs, and credits the fund source with their total.
            lenient().when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("500"));
            lenient().when(allocation.getOverpaymentPortion()).thenReturn(new BigDecimal("60"));
            lenient().when(txn.getTransactionAmount()).thenReturn(new BigDecimal("560"));
            return;
        }
        lenient().when(allocation.getPrincipalPortion()).thenReturn(new BigDecimal("500"));
        lenient().when(allocation.getFeeChargesPortion()).thenReturn(new BigDecimal("30"));
        lenient().when(allocation.getPenaltyChargesPortion()).thenReturn(new BigDecimal("20"));
        lenient().when(allocation.getOverpaymentPortion()).thenReturn(new BigDecimal("10"));
        lenient().when(txn.getTransactionAmount()).thenReturn(new BigDecimal("560"));
    }

    /** Runs a posting action and returns the entries it booked, rebuilt as the ledger would hold them. */
    private List<JournalEntry> captureBookedEntries(final Runnable postingAction) {
        postingAction.run();

        final List<JournalEntry> booked = new ArrayList<>();
        final ArgumentCaptor<GLAccount> debitAccounts = ArgumentCaptor.forClass(GLAccount.class);
        final ArgumentCaptor<BigDecimal> debitAmounts = ArgumentCaptor.forClass(BigDecimal.class);
        verify(helper, atLeast(0)).createDebitJournalEntryForWorkingCapitalLoan(any(), any(), debitAccounts.capture(), anyLong(), anyLong(),
                any(), debitAmounts.capture(), any());
        for (int i = 0; i < debitAccounts.getAllValues().size(); i++) {
            booked.add(postedEntry(debitAccounts.getAllValues().get(i), JournalEntryType.DEBIT,
                    debitAmounts.getAllValues().get(i).toPlainString()));
        }

        final ArgumentCaptor<GLAccount> creditAccounts = ArgumentCaptor.forClass(GLAccount.class);
        final ArgumentCaptor<BigDecimal> creditAmounts = ArgumentCaptor.forClass(BigDecimal.class);
        verify(helper, atLeast(0)).createCreditJournalEntryForWorkingCapitalLoan(any(), any(), creditAccounts.capture(), anyLong(),
                anyLong(), any(), creditAmounts.capture(), any());
        for (int i = 0; i < creditAccounts.getAllValues().size(); i++) {
            booked.add(postedEntry(creditAccounts.getAllValues().get(i), JournalEntryType.CREDIT,
                    creditAmounts.getAllValues().get(i).toPlainString()));
        }
        return booked;
    }

    /** Every account the type matrix can reach, each with a distinct id so positions are comparable. */
    private void stubAllGLAccounts() {
        stubGLAccountIds();
        lenient().when(incomeFromRecoveryGLAccount.getId()).thenReturn(6L);
        lenient().when(incomeFromFeesGLAccount.getId()).thenReturn(7L);
        lenient().when(incomeFromPenaltiesGLAccount.getId()).thenReturn(8L);
        lenient().when(incomeFromChargeOffFeesGLAccount.getId()).thenReturn(9L);
        lenient().when(incomeFromChargeOffPenaltyGLAccount.getId()).thenReturn(10L);
        lenient().when(goodwillCreditGLAccount.getId()).thenReturn(11L);
        lenient().when(incomeFromGoodwillCreditFeesGLAccount.getId()).thenReturn(12L);
        lenient().when(incomeFromGoodwillCreditPenaltyGLAccount.getId()).thenReturn(13L);
        lenient().when(chargeOffExpenseGLAccount.getId()).thenReturn(14L);
        lenient().when(chargeOffFraudExpenseGLAccount.getId()).thenReturn(15L);

        stubGLAccountMapping(CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY, incomeFromChargeOffPenaltyGLAccount);
        stubGLAccountMapping(CashAccountsForLoan.GOODWILL_CREDIT, goodwillCreditGLAccount);
        stubGLAccountMapping(CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES, incomeFromGoodwillCreditFeesGLAccount);
        stubGLAccountMapping(CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY, incomeFromGoodwillCreditPenaltyGLAccount);
        stubGLAccountMapping(CashAccountsForLoan.CHARGE_OFF_EXPENSE, chargeOffExpenseGLAccount);
        stubGLAccountMapping(CashAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE, chargeOffFraudExpenseGLAccount);
    }

    private void stubGLAccountMapping(final CashAccountsForLoan accountType, final GLAccount account) {
        lenient().when(helper.getLinkedGLAccountForWorkingCapitalLoanProduct(eq(PRODUCT_ID), eq(accountType.getValue()), any()))
                .thenReturn(account);
    }

    /** An undo keeps its mirrors live: they are the visible reversal, not bookkeeping noise. */
    @Test
    void testReversalKeepsItsMirrorsLive() {
        when(txn.getReversedOnDate()).thenReturn(LocalDate.of(2026, 5, 2));
        stubLiveEntries(postedEntry(fundSourceGLAccount, JournalEntryType.DEBIT, "5000"),
                postedEntry(loanPortfolioGLAccount, JournalEntryType.CREDIT, "5000"));

        processor.postReversalJournalEntries(loan, txn);

        final ArgumentCaptor<JournalEntry> persisted = ArgumentCaptor.forClass(JournalEntry.class);
        verify(helper, atLeastOnce()).persistJournalEntry(persisted.capture());
        assertTrue(persisted.getAllValues().stream().anyMatch(entry -> !entry.isReversed()), "the reversal mirrors must stay live");
    }
}
