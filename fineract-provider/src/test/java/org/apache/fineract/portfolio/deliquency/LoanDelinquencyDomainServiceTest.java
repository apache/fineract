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
package org.apache.fineract.portfolio.deliquency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.service.LoanDelinquencyDomainServiceImpl;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionReadService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanDelinquencyDomainServiceTest {

    @Mock
    private LoanProductRelatedDetail loanProductRelatedDetail;
    @Mock
    private ConfigurationDomainService staticConfigurationDomainService;
    @Mock
    private MoneyHelper moneyHelper;
    @Mock
    private Loan loan;
    @Mock
    private LoanProduct loanProduct;
    @Mock
    private DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;
    @InjectMocks
    private LoanDelinquencyDomainServiceImpl underTest;
    @Mock
    private LoanTransactionReadService loanTransactionReadService;

    private LocalDate businessDate;
    private MonetaryCurrency currency;
    private MockedStatic<MoneyHelper> moneyHelperStatic;

    private final BigDecimal principal = BigDecimal.valueOf(1000);
    private final BigDecimal zeroAmount = BigDecimal.ZERO;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));

        businessDate = DateUtils.getBusinessLocalDate();
        currency = new MonetaryCurrency("USD", 2, null);

        moneyHelperStatic = Mockito.mockStatic(MoneyHelper.class);
        moneyHelperStatic.when(() -> MoneyHelper.getRoundingMode()).thenReturn(RoundingMode.UP);
    }

    @AfterEach
    public void deregister() {
        ThreadLocalContextUtil.reset();
        moneyHelperStatic.close();
    }

    @Test
    public void givenLoanAccountWithoutOverdueThenCalculateDelinquentData() {
        // given
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        final LocalDate fromDate = businessDate.minusMonths(1);
        final LocalDate dueDate = businessDate;
        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = Arrays.asList(new LoanRepaymentScheduleInstallment(loan, 1,
                fromDate, dueDate, principal, zeroAmount, zeroAmount, zeroAmount, false, new HashSet<>(), zeroAmount));

        // when
        when(loanProductRelatedDetail.getGraceOnArrearsAgeing()).thenReturn(0);
        when(loan.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        when(loan.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);
        when(loan.getCurrency()).thenReturn(currency);
        when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        CollectionData collectionData = underTest.getOverdueCollectionData(loan, effectiveDelinquencyList);

        // then
        assertEquals(0L, collectionData.getDelinquentDays());
        assertEquals(null, collectionData.getDelinquentDate());
        assertEquals(collectionData.getDelinquentDays(), collectionData.getPastDueDays());

    }

    @Test
    public void givenLoanAccountWithOverdueThenCalculateDelinquentData() {
        // given
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        final Long daysDiff = 2L;
        final LocalDate fromDate = businessDate.minusMonths(1).minusDays(daysDiff);
        final LocalDate dueDate = businessDate.minusDays(daysDiff);
        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = Arrays.asList(new LoanRepaymentScheduleInstallment(loan, 1,
                fromDate, dueDate, principal, zeroAmount, zeroAmount, zeroAmount, false, new HashSet<>(), zeroAmount));

        // when
        when(loanProductRelatedDetail.getGraceOnArrearsAgeing()).thenReturn(0);
        when(loan.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        when(loan.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);
        when(loanTransactionReadService.fetchLoanTransactionsByType(loan.getId(), null, LoanTransactionType.CHARGEBACK.getValue()))
                .thenReturn(Collections.emptyList());
        when(loan.getLastLoanRepaymentScheduleInstallment()).thenReturn(repaymentScheduleInstallments.get(0));
        when(loan.getCurrency()).thenReturn(currency);
        when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);
        when(delinquencyEffectivePauseHelper.getPausedDaysBeforeDate(effectiveDelinquencyList, businessDate)).thenReturn(0L);

        CollectionData collectionData = underTest.getOverdueCollectionData(loan, effectiveDelinquencyList);

        // then
        assertEquals(daysDiff, collectionData.getDelinquentDays());
        assertEquals(dueDate, collectionData.getDelinquentDate());
        assertEquals(collectionData.getDelinquentDays(), collectionData.getPastDueDays());

    }

    @Test
    public void givenLoanAccountWithoutOverdueWithChargebackThenCalculateDelinquentData() {
        // given
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        PaymentDetail paymentDetail = Mockito.mock(PaymentDetail.class);
        Long daysDiff = 2L;
        final LocalDate fromDate = businessDate.minusMonths(1).plusDays(daysDiff);
        final LocalDate dueDate = businessDate.plusDays(daysDiff);
        final LocalDate transactionDate = businessDate.minusDays(daysDiff);

        final Money zeroMoney = Money.zero(currency);
        LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan, 1, fromDate, dueDate, principal,
                zeroAmount, zeroAmount, zeroAmount, false, new HashSet<>(), zeroAmount);
        LoanTransaction loanTransaction = LoanTransaction.chargeback(loan, Money.of(currency, principal), paymentDetail, transactionDate,
                null);
        installment.getLoanTransactionToRepaymentScheduleMappings().add(LoanTransactionToRepaymentScheduleMapping
                .createFrom(loanTransaction, installment, zeroMoney, zeroMoney, zeroMoney, zeroMoney));

        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = Arrays.asList(installment);

        // when
        when(loanProductRelatedDetail.getGraceOnArrearsAgeing()).thenReturn(0);
        when(loan.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        when(loan.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);
        when(loan.getCurrency()).thenReturn(currency);
        when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);

        CollectionData collectionData = underTest.getOverdueCollectionData(loan, effectiveDelinquencyList);

        // then
        assertEquals(0L, collectionData.getDelinquentDays());
        assertEquals(null, collectionData.getDelinquentDate());
        assertEquals(collectionData.getDelinquentDays(), collectionData.getPastDueDays());

    }

    @Test
    public void givenLoanInstallmentWithOverdueEnableInstallmentDelinquencyThenCalculateDelinquentData() {
        // given
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        final Long daysDiff = 2L;
        final LocalDate fromDate = businessDate.minusMonths(1).minusDays(daysDiff);
        final LocalDate dueDate = businessDate.minusDays(daysDiff);

        LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan, 1, fromDate, dueDate, principal,
                zeroAmount, zeroAmount, zeroAmount, false, new HashSet<>(), zeroAmount);
        installment.setId(1L);
        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = Arrays.asList(installment);

        // when
        when(loanProductRelatedDetail.getGraceOnArrearsAgeing()).thenReturn(0);
        when(loan.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        when(loan.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);
        when(loanTransactionReadService.fetchLoanTransactionsByType(loan.getId(), null, LoanTransactionType.CHARGEBACK.getValue()))
                .thenReturn(Collections.emptyList());
        when(loan.getLastLoanRepaymentScheduleInstallment()).thenReturn(repaymentScheduleInstallments.get(0));
        when(loan.getCurrency()).thenReturn(currency);
        when(loan.isEnableInstallmentLevelDelinquency()).thenReturn(true);
        when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);
        when(delinquencyEffectivePauseHelper.getPausedDaysBeforeDate(effectiveDelinquencyList, businessDate)).thenReturn(0L);

        LoanDelinquencyData collectionData = underTest.getLoanDelinquencyData(loan, effectiveDelinquencyList);

        // then
        assertNotNull(collectionData);
        assertNotNull(collectionData.getLoanInstallmentsCollectionData());
        assertEquals(1L, collectionData.getLoanInstallmentsCollectionData().size());

        CollectionData loanCollectionData = collectionData.getLoanCollectionData();
        CollectionData installmentCollectionData = collectionData.getLoanInstallmentsCollectionData().get(1L);

        assertEquals(daysDiff, loanCollectionData.getDelinquentDays());
        assertEquals(dueDate, loanCollectionData.getDelinquentDate());
        assertEquals(loanCollectionData.getDelinquentDays(), loanCollectionData.getPastDueDays());

        assertEquals(daysDiff, installmentCollectionData.getDelinquentDays());
        assertEquals(dueDate, installmentCollectionData.getDelinquentDate());
        assertEquals(installmentCollectionData.getDelinquentDays(), installmentCollectionData.getPastDueDays());

    }

    @Test
    public void givenLoanInstallmentWithoutOverdueWithChargebackAndEnableInstallmentDelinquencyThenCalculateDelinquentData() {

        // given
        final List<LoanDelinquencyActionData> effectiveDelinquencyList = Collections.emptyList();
        PaymentDetail paymentDetail = Mockito.mock(PaymentDetail.class);
        Long daysDiff = 2L;
        final LocalDate fromDate = businessDate.minusMonths(1).plusDays(daysDiff);
        final LocalDate dueDate = businessDate.plusDays(daysDiff);
        final LocalDate transactionDate = businessDate.minusDays(daysDiff);

        final Money zeroMoney = Money.zero(currency);
        LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan, 1, fromDate, dueDate, principal,
                zeroAmount, zeroAmount, zeroAmount, false, new HashSet<>(), zeroAmount);
        installment.setId(1L);
        LoanTransaction loanTransaction = LoanTransaction.chargeback(loan, Money.of(currency, principal), paymentDetail, transactionDate,
                null);
        installment.getLoanTransactionToRepaymentScheduleMappings().add(LoanTransactionToRepaymentScheduleMapping
                .createFrom(loanTransaction, installment, zeroMoney, zeroMoney, zeroMoney, zeroMoney));

        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = Arrays.asList(installment);
        // when
        when(loanProductRelatedDetail.getGraceOnArrearsAgeing()).thenReturn(0);
        when(loan.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        when(loan.getRepaymentScheduleInstallments()).thenReturn(repaymentScheduleInstallments);
        when(loan.isEnableInstallmentLevelDelinquency()).thenReturn(true);
        when(loan.getCurrency()).thenReturn(currency);
        when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);
        when(loanTransactionReadService.fetchLoanTransactionsByType(loan.getId(), null, LoanTransactionType.CHARGEBACK.getValue()))
                .thenReturn(Arrays.asList(loanTransaction));
        when(delinquencyEffectivePauseHelper.getPausedDaysBeforeDate(effectiveDelinquencyList, businessDate)).thenReturn(0L);

        LoanDelinquencyData collectionData = underTest.getLoanDelinquencyData(loan, effectiveDelinquencyList);

        // then
        assertNotNull(collectionData);
        assertNotNull(collectionData.getLoanInstallmentsCollectionData());
        assertEquals(1L, collectionData.getLoanInstallmentsCollectionData().size());

        CollectionData loanCollectionData = collectionData.getLoanCollectionData();
        CollectionData installmentCollectionData = collectionData.getLoanInstallmentsCollectionData().get(1L);

        assertEquals(daysDiff, loanCollectionData.getDelinquentDays());
        assertEquals(transactionDate, loanCollectionData.getDelinquentDate());
        assertEquals(loanCollectionData.getDelinquentDays(), loanCollectionData.getPastDueDays());

        // then
        assertEquals(daysDiff, installmentCollectionData.getDelinquentDays());
        assertEquals(transactionDate, installmentCollectionData.getDelinquentDate());
        assertEquals(installmentCollectionData.getDelinquentDays(), installmentCollectionData.getPastDueDays());
        assertEquals(0, principal.compareTo(installmentCollectionData.getDelinquentAmount()));

    }

}
