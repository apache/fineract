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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgressiveLoanInterestRefundServiceImplTest {

    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, 1);
    private static final Long LOAN_ID = 1L;

    private final MockedStatic<MoneyHelper> moneyHelper = mockStatic(MoneyHelper.class);

    @Mock
    private EMICalculator emiCalculator;
    @Mock
    private LoanAssembler loanAssembler;
    @Mock
    private LoanScheduleService loanScheduleService;
    @Mock
    private LoanUtilService loanUtilService;
    @Mock
    private LoanTransactionProcessingService loanTransactionProcessingService;

    @InjectMocks
    private ProgressiveLoanInterestRefundServiceImpl underTest;

    @BeforeEach
    void setUp() {
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 22))));
    }

    @AfterEach
    void tearDown() {
        moneyHelper.close();
        ThreadLocalContextUtil.reset();
    }

    @Test
    void chargePaymentTransactionKeepsItsLoanChargePaidByMappingDuringSimulation() {
        assertSimulatedTransactionKeepsChargeMappings(LoanTransactionType.CHARGE_PAYMENT);
    }

    @Test
    void repaymentAtDisbursementTransactionKeepsItsLoanChargePaidByMappingDuringSimulation() {
        assertSimulatedTransactionKeepsChargeMappings(LoanTransactionType.REPAYMENT_AT_DISBURSEMENT);
    }

    private void assertSimulatedTransactionKeepsChargeMappings(LoanTransactionType transactionType) {
        LocalDate transactionDate = LocalDate.of(2024, 1, 10);
        LocalDate refundDate = LocalDate.of(2024, 1, 22);

        Loan loan = mock(Loan.class);
        Office office = mock(Office.class);
        LoanChargePaidBy chargePaidBy = mock(LoanChargePaidBy.class);
        LoanTransaction chargePaymentTransaction = mock(LoanTransaction.class);

        when(chargePaymentTransaction.getId()).thenReturn(10L);
        when(chargePaymentTransaction.isNotReversed()).thenReturn(true);
        when(chargePaymentTransaction.isAccrualRelated()).thenReturn(false);
        when(chargePaymentTransaction.isInterestRefund()).thenReturn(false);
        when(chargePaymentTransaction.getTypeOf()).thenReturn(transactionType);
        when(chargePaymentTransaction.getLoan()).thenReturn(loan);
        when(chargePaymentTransaction.getDateOf()).thenReturn(transactionDate);
        when(chargePaymentTransaction.getAmount()).thenReturn(BigDecimal.TEN);
        // Mutable set: production code must copy from this instance, not from the freshly created simulation copy.
        when(chargePaymentTransaction.getLoanChargesPaid()).thenReturn(new HashSet<>(Set.of(chargePaidBy)));

        when(loanAssembler.assembleFrom(LOAN_ID)).thenReturn(loan);

        List<LoanTransaction> loanTransactions = new ArrayList<>(List.of(chargePaymentTransaction));
        when(loan.getLoanTransactions()).thenReturn(loanTransactions);
        when(loan.getOffice()).thenReturn(office);
        when(loan.getDisbursementDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(loan.getCurrency()).thenReturn(CURRENCY);
        when(loan.getSupportedInterestRefundTransactionTypes()).thenReturn(List.of(LoanTransactionType.MERCHANT_ISSUED_REFUND));

        AdvancedPaymentScheduleTransactionProcessor processor = mock(AdvancedPaymentScheduleTransactionProcessor.class);
        when(processor.reprocessProgressiveLoanTransactions(any(), any(), anyList(), any(), any(), any()))
                .thenReturn(Pair.of(new ChangedTransactionDetail(), mock(ProgressiveLoanInterestScheduleModel.class)));

        when(loanUtilService.buildScheduleGeneratorDTO(loan, null)).thenReturn(mock(ScheduleGeneratorDTO.class));
        Money zeroInterest = Money.zero(CURRENCY);
        when(emiCalculator.getSumOfDueInterestsOnDate(any(), any())).thenReturn(zeroInterest);

        underTest.totalInterestByTransactions(processor, LOAN_ID, refundDate, List.of(), List.of(10L), List.of());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<LoanTransaction>> transactionsToReprocessCaptor = ArgumentCaptor.forClass(List.class);
        verify(processor).reprocessProgressiveLoanTransactions(any(), any(), transactionsToReprocessCaptor.capture(), any(), any(), any());

        List<LoanTransaction> simulatedTransactions = transactionsToReprocessCaptor.getValue();
        LoanTransaction simulatedCopy = simulatedTransactions.stream().filter(t -> transactionType.equals(t.getTypeOf())).findFirst()
                .orElseThrow();

        assertEquals(1, simulatedCopy.getLoanChargesPaid().size());
        assertTrue(simulatedCopy.getLoanChargesPaid().contains(chargePaidBy));
    }
}
