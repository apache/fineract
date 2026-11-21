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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionFeeRefundPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class LoanFeeRefundServiceTest {

    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);
    private static final LocalDate JAN_1 = LocalDate.of(2026, 1, 1);

    private final BusinessEventNotifierService businessEventNotifierService = mock(BusinessEventNotifierService.class);
    private final ExternalIdFactory externalIdFactory = mock(ExternalIdFactory.class);
    private final LoanFeeRefundService underTest = new LoanFeeRefundService(businessEventNotifierService, externalIdFactory);
    private final Loan loan = feeRefundLoan();

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
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 1, 20))));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testCreateMissingFeeRefundsCreatesTheFeeRefundOfARefundFreedByAnUndo() {
        // UC15 after the undo of the 1st refund: 600 on Jan 12 = 15, so 100 on Jan 14 gets 2.5
        refund("600", LocalDate.of(2026, 1, 10)).reverse();
        withFeeRefund(refund("600", LocalDate.of(2026, 1, 12)), "15");
        final LoanTransaction refundWithoutFeeRefund = refund("100", LocalDate.of(2026, 1, 14));

        final List<LoanTransaction> feeRefunds = underTest.createMissingFeeRefunds(loan);

        assertEquals(1, feeRefunds.size());
        final LoanTransaction feeRefund = feeRefunds.get(0);
        assertTrue(feeRefund.isFeeRefund());
        assertEquals(0, new BigDecimal("2.5").compareTo(feeRefund.getAmount()));
        assertEquals(refundWithoutFeeRefund.getTransactionDate(), feeRefund.getTransactionDate());
        assertEquals(List.of(refundWithoutFeeRefund),
                feeRefund
                        .getLoanTransactionRelations(relation -> LoanTransactionRelationTypeEnum.RELATED.equals(relation.getRelationType()))
                        .stream().map(LoanTransactionRelation::getToTransaction).toList());
        verify(loan).addLoanTransaction(feeRefund);
        verify(businessEventNotifierService).notifyPreBusinessEvent(any(LoanTransactionFeeRefundPreBusinessEvent.class));
    }

    @Test
    public void testCreateMissingFeeRefundsCreatesNothingWhenTheCapIsUsedUp() {
        withFeeRefund(refund("600", LocalDate.of(2026, 1, 10)), "15");
        withFeeRefund(refund("600", LocalDate.of(2026, 1, 12)), "10");
        refund("100", LocalDate.of(2026, 1, 14));

        assertTrue(underTest.createMissingFeeRefunds(loan).isEmpty());
        verify(loan, never()).addLoanTransaction(any());
        verify(businessEventNotifierService, never()).notifyPreBusinessEvent(any());
    }

    // 1000 disbursed with a fee of 25, Fee refund on Merchant issued refund
    private Loan feeRefundLoan() {
        final Loan feeRefundLoan = mock(Loan.class);
        final LoanTransaction disbursement = mock(LoanTransaction.class);
        lenient().when(disbursement.isNotReversed()).thenReturn(true);
        lenient().when(disbursement.isDisbursement()).thenReturn(true);
        lenient().when(disbursement.getTypeOf()).thenReturn(LoanTransactionType.DISBURSEMENT);
        lenient().when(disbursement.getTransactionDate()).thenReturn(JAN_1);
        lenient().when(disbursement.getAmount()).thenReturn(BigDecimal.valueOf(1000));
        final LoanCharge fee = mock(LoanCharge.class);
        lenient().when(fee.isActive()).thenReturn(true);
        lenient().when(fee.isDueAtDownPayment()).thenReturn(true);
        lenient().when(fee.getDueLocalDate()).thenReturn(JAN_1);
        lenient().when(fee.amount()).thenReturn(BigDecimal.valueOf(25));
        when(feeRefundLoan.getLoanTransactions()).thenReturn(new ArrayList<>(List.of(disbursement)));
        lenient().when(feeRefundLoan.getLoanCharges()).thenReturn(Set.of(fee));
        lenient().when(feeRefundLoan.getCurrency()).thenReturn(new MonetaryCurrency("EUR", 2, null));
        when(feeRefundLoan.getSupportedFeeRefundTransactionTypes()).thenReturn(List.of(LoanTransactionType.MERCHANT_ISSUED_REFUND));
        lenient().when(externalIdFactory.create()).thenReturn(ExternalId.empty());
        return feeRefundLoan;
    }

    private LoanTransaction refund(final String amount, final LocalDate date) {
        final LoanTransaction refund = LoanTransaction.refund(loan, LoanTransactionType.MERCHANT_ISSUED_REFUND, new BigDecimal(amount),
                null, date, null);
        loan.getLoanTransactions().add(refund);
        return refund;
    }

    private void withFeeRefund(final LoanTransaction refund, final String amount) {
        final LoanTransaction feeRefund = LoanTransaction.feeRefund(loan, new BigDecimal(amount), refund.getTransactionDate(), null);
        feeRefund.getLoanTransactionRelations()
                .add(LoanTransactionRelation.linkToTransaction(feeRefund, refund, LoanTransactionRelationTypeEnum.RELATED));
        loan.getLoanTransactions().add(feeRefund);
    }
}
