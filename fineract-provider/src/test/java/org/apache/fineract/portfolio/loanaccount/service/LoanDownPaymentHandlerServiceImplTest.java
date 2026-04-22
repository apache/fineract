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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionDownPaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionDownPaymentPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanRefundValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoanDownPaymentHandlerServiceImplTest {

    private final MockedStatic<MoneyHelper> moneyHelper = mockStatic(MoneyHelper.class);
    private final MockedStatic<MathUtil> mathUtilMock = mockStatic(MathUtil.class);
    private final MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
    private final MockedStatic<TemporaryConfigurationServiceContainer> tempConfigServiceMock = mockStatic(
            TemporaryConfigurationServiceContainer.class);

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private LoanTransactionRepository loanTransactionRepository;

    @Mock
    private LoanTransaction loanTransaction;

    @Mock
    private JsonCommand command;

    @Mock
    private LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator;

    @Mock
    private LoanScheduleService loanScheduleService;

    @Mock
    private LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor;

    @Mock
    private LoanRefundService loanRefundService;

    @Mock
    private LoanRefundValidator loanRefundValidator;

    @Mock
    private ReprocessLoanTransactionsService reprocessLoanTransactionsService;

    @Mock
    private LoanTransactionProcessingService loanTransactionProcessingService;

    @Mock
    private LoanLifecycleStateMachine loanLifecycleStateMachine;

    @Mock
    private LoanBalanceService loanBalanceService;

    @Mock
    private LoanTransactionService loanTransactionService;

    @Mock
    private LoanJournalEntryPoster loanJournalEntryPoster;

    private LoanDownPaymentHandlerServiceImpl underTest;

    @BeforeEach
    public void setUp() {
        underTest = new LoanDownPaymentHandlerServiceImpl(loanTransactionRepository, businessEventNotifierService,
                loanDownPaymentTransactionValidator, loanScheduleService, loanRefundService, loanRefundValidator,
                reprocessLoanTransactionsService, loanTransactionProcessingService, loanLifecycleStateMachine, loanBalanceService,
                loanTransactionService, loanJournalEntryPoster);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.UP));
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.UP);
        tempConfigServiceMock.when(TemporaryConfigurationServiceContainer::isExternalIdAutoGenerationEnabled).thenReturn(true);
    }

    @AfterEach
    public void reset() {
        moneyHelper.close();
        tempConfigServiceMock.close();
        mathUtilMock.close();
        dateUtilsMock.close();
    }

    @Test
    public void testDownPaymentHandler() {
        // given
        final Loan loanForProcessing = Mockito.mock(Loan.class);
        final LoanTransaction disbursement = Mockito.mock(LoanTransaction.class);
        final Money mockAdjustedDownPaymentMoney = Mockito.mock(Money.class);
        final LoanProductRelatedDetail loanRepaymentRelatedDetail = Mockito.mock(LoanProductRelatedDetail.class);
        final LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        final LoanProductRelatedDetail loanProductRelatedDetail = Mockito.mock(LoanProductRelatedDetail.class);
        final ScheduleGeneratorDTO scheduleGeneratorDTO = Mockito.mock(ScheduleGeneratorDTO.class);
        final HolidayDetailDTO holidayDetailDTO = Mockito.mock(HolidayDetailDTO.class);
        final ChangedTransactionDetail changedTransactionDetail = Mockito.mock(ChangedTransactionDetail.class);

        final MonetaryCurrency loanCurrency = new MonetaryCurrency("USD", 2, 1);

        final Money downPaymentMoney = Mockito.mock(Money.class);
        final Money overPaymentPortionMoney = Mockito.mock(Money.class);
        final Money calculatedMoney = Money.of(loanCurrency, BigDecimal.valueOf(500));

        when(downPaymentMoney.getCurrencyCode()).thenReturn(loanCurrency.getCode());
        when(overPaymentPortionMoney.getCurrencyCode()).thenReturn(loanCurrency.getCode());

        when(loanRepaymentRelatedDetail.getInstallmentAmountInMultiplesOf()).thenReturn(10);
        when(loanForProcessing.getLoanRepaymentScheduleDetail()).thenReturn(loanRepaymentRelatedDetail);
        when(loanForProcessing.getLoanProductRelatedDetail()).thenReturn(loanRepaymentRelatedDetail);
        when(loanRepaymentRelatedDetail.isInterestRecalculationEnabled()).thenReturn(true);
        when(loanForProcessing.isInterestBearingAndInterestRecalculationEnabled()).thenReturn(true);
        when(loanRepaymentRelatedDetail.getDisbursedAmountPercentageForDownPayment()).thenReturn(BigDecimal.valueOf(10));
        when(loanForProcessing.getCurrency()).thenReturn(loanCurrency);
        when(loanForProcessing.loanCurrency()).thenReturn(loanCurrency);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanForProcessing.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        when(loanTransactionProcessingService.reprocessLoanTransactions(any(), any(), any(), any(), any(), any()))
                .thenReturn(changedTransactionDetail);
        when(loanProductRelatedDetail.getLoanScheduleType()).thenReturn(LoanScheduleType.PROGRESSIVE);

        when(disbursement.getOverPaymentPortion(loanCurrency)).thenReturn(overPaymentPortionMoney);

        when(downPaymentMoney.minus(overPaymentPortionMoney)).thenReturn(calculatedMoney);
        mathUtilMock.when(() -> MathUtil.negativeToZero(any(Money.class))).thenReturn(mockAdjustedDownPaymentMoney);
        when(mockAdjustedDownPaymentMoney.isGreaterThanZero()).thenReturn(true);

        dateUtilsMock.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2024, 11, 19));

        when(scheduleGeneratorDTO.getHolidayDetailDTO()).thenReturn(holidayDetailDTO);

        doNothing().when(businessEventNotifierService).notifyPreBusinessEvent(any(LoanTransactionDownPaymentPreBusinessEvent.class));
        doNothing().when(businessEventNotifierService).notifyPostBusinessEvent(any(LoanTransactionDownPaymentPostBusinessEvent.class));
        doNothing().when(businessEventNotifierService).notifyPostBusinessEvent(any(LoanBalanceChangedBusinessEvent.class));
        when(loanTransactionRepository.saveAndFlush(any(LoanTransaction.class))).thenReturn(loanTransaction);

        // when
        final LoanTransaction actual = underTest.handleDownPayment(scheduleGeneratorDTO, command, disbursement, loanForProcessing);

        // then
        assertNotNull(actual);
        verify(businessEventNotifierService, Mockito.times(1))
                .notifyPreBusinessEvent(Mockito.any(LoanTransactionDownPaymentPreBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(1))
                .notifyPostBusinessEvent(Mockito.any(LoanTransactionDownPaymentPostBusinessEvent.class));
    }

    @Test
    public void testDownPaymentHandlerNoNewTransaction() {
        // given
        final Loan loanForProcessing = Mockito.mock(Loan.class);
        final LoanTransaction disbursement = Mockito.mock(LoanTransaction.class);
        final Money mockAdjustedDownPaymentMoney = Mockito.mock(Money.class);
        final LoanProductRelatedDetail loanRepaymentRelatedDetail = Mockito.mock(LoanProductRelatedDetail.class);
        final LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        final LoanProductRelatedDetail loanProductRelatedDetail = Mockito.mock(LoanProductRelatedDetail.class);
        final ScheduleGeneratorDTO scheduleGeneratorDTO = Mockito.mock(ScheduleGeneratorDTO.class);
        final HolidayDetailDTO holidayDetailDTO = Mockito.mock(HolidayDetailDTO.class);

        final MonetaryCurrency loanCurrency = new MonetaryCurrency("USD", 2, 1);

        final Money downPaymentMoney = Mockito.mock(Money.class);
        final Money overPaymentPortionMoney = Mockito.mock(Money.class);
        final Money calculatedMoney = Money.of(loanCurrency, BigDecimal.valueOf(500));

        when(downPaymentMoney.getCurrencyCode()).thenReturn(loanCurrency.getCode());
        when(overPaymentPortionMoney.getCurrencyCode()).thenReturn(loanCurrency.getCode());

        when(loanForProcessing.getLoanRepaymentScheduleDetail()).thenReturn(loanRepaymentRelatedDetail);
        when(loanRepaymentRelatedDetail.getDisbursedAmountPercentageForDownPayment()).thenReturn(BigDecimal.valueOf(10));
        when(loanForProcessing.getCurrency()).thenReturn(loanCurrency);
        when(loanForProcessing.getLoanProduct()).thenReturn(loanProduct);
        when(loanForProcessing.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        when(loanProductRelatedDetail.getLoanScheduleType()).thenReturn(LoanScheduleType.PROGRESSIVE);

        when(disbursement.getOverPaymentPortion(loanCurrency)).thenReturn(overPaymentPortionMoney);

        when(downPaymentMoney.minus(overPaymentPortionMoney)).thenReturn(calculatedMoney);
        mathUtilMock.when(() -> MathUtil.negativeToZero(any(Money.class))).thenReturn(mockAdjustedDownPaymentMoney);
        when(mockAdjustedDownPaymentMoney.isGreaterThanZero()).thenReturn(false);

        dateUtilsMock.when(DateUtils::getBusinessLocalDate).thenReturn(LocalDate.of(2024, 11, 19));

        when(scheduleGeneratorDTO.getHolidayDetailDTO()).thenReturn(holidayDetailDTO);

        doNothing().when(businessEventNotifierService).notifyPreBusinessEvent(any(LoanTransactionDownPaymentPreBusinessEvent.class));

        // when
        LoanTransaction actual = underTest.handleDownPayment(scheduleGeneratorDTO, command, disbursement, loanForProcessing);

        // then
        assertNull(actual);
        verify(businessEventNotifierService, Mockito.times(1))
                .notifyPreBusinessEvent(Mockito.any(LoanTransactionDownPaymentPreBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.never())
                .notifyPostBusinessEvent(Mockito.any(LoanTransactionDownPaymentPostBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.never()).notifyPostBusinessEvent(Mockito.any(LoanBalanceChangedBusinessEvent.class));
        verify(loanTransactionRepository, Mockito.never()).saveAndFlush(any(LoanTransaction.class));
    }
}
