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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoanAccrualsProcessingServiceImplTest {

    @InjectMocks
    private LoanAccrualsProcessingServiceImpl accrualsProcessingService;

    @Mock
    private Loan loan;

    @Mock
    private LoanStatus loanStatus;

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private LoanTransactionRepository loanTransactionRepository;

    @Mock
    private JournalEntryWritePlatformService journalEntryWritePlatformService;

    @BeforeEach
    void setUp() {
        when(loan.isClosed()).thenReturn(false);
        when(loan.getStatus()).thenReturn(loanStatus);
        when(loanStatus.isOverpaid()).thenReturn(false);

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "Test Tenant", "America/Mexico_City", null));
        MoneyHelper.initializeTenantRoundingMode("test", 6);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
        MoneyHelper.clearCache();
    }

    @ParameterizedTest
    @MethodSource("loanStatusTestCases")
    void addPeriodicAccruals_ShouldNotProceed_WhenLoanIsClosedOrOverpaid(final boolean isClosed, final boolean isOverpaid) {
        // Given
        final LocalDate tillDate = LocalDate.now(ZoneId.systemDefault());
        when(loan.isClosed()).thenReturn(isClosed);

        when(loan.getStatus()).thenReturn(loanStatus);
        when(loanStatus.isOverpaid()).thenReturn(isOverpaid);

        // When
        accrualsProcessingService.addPeriodicAccruals(tillDate, loan);

        // Then
        verify(loan, times(1)).isClosed();

        verify(loanTransactionRepository, never()).saveAndFlush(any());
        verifyNoInteractions(journalEntryWritePlatformService);
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
        verify(loan, never()).addLoanTransaction(any());
    }

    @Test
    void calcInterestTransactionWaivedAmount_ShouldSkipMappingsWithoutTransaction() {
        // Given
        LocalDate tillDate = LocalDate.now(ZoneId.systemDefault());
        LoanRepaymentScheduleInstallment installment = mock(LoanRepaymentScheduleInstallment.class);
        LoanTransactionToRepaymentScheduleMapping nullTransactionMapping = mock(LoanTransactionToRepaymentScheduleMapping.class);
        LoanTransactionToRepaymentScheduleMapping interestWaiverMapping = mock(LoanTransactionToRepaymentScheduleMapping.class);
        LoanTransaction interestWaiverTransaction = mock(LoanTransaction.class);

        when(nullTransactionMapping.getLoanTransaction()).thenReturn(null);
        when(interestWaiverMapping.getLoanTransaction()).thenReturn(interestWaiverTransaction);
        when(interestWaiverMapping.getInterestPortion()).thenReturn(new BigDecimal("12.34"));
        when(interestWaiverTransaction.isReversed()).thenReturn(false);
        when(interestWaiverTransaction.isInterestWaiver()).thenReturn(true);
        when(interestWaiverTransaction.getTransactionDate()).thenReturn(tillDate);
        when(installment.getLoanTransactionToRepaymentScheduleMappings()).thenReturn(Set.of(nullTransactionMapping, interestWaiverMapping));

        // When
        BigDecimal result = ReflectionTestUtils.invokeMethod(accrualsProcessingService, "calcInterestTransactionWaivedAmount", installment,
                tillDate);

        // Then
        assertThat(result).isEqualByComparingTo("12.34");
    }

    @Test
    void calcChargeWaivedAmount_ShouldSkipMappingsWithoutTransaction() {
        // Given
        LocalDate tillDate = LocalDate.now(ZoneId.systemDefault());
        LoanChargePaidBy nullTransactionPaidBy = mock(LoanChargePaidBy.class);
        LoanChargePaidBy chargeWaiverPaidBy = mock(LoanChargePaidBy.class);
        LoanTransaction chargeWaiverTransaction = mock(LoanTransaction.class);

        when(nullTransactionPaidBy.getLoanTransaction()).thenReturn(null);
        when(chargeWaiverPaidBy.getLoanTransaction()).thenReturn(chargeWaiverTransaction);
        when(chargeWaiverPaidBy.getAmount()).thenReturn(new BigDecimal("12.34"));
        when(chargeWaiverTransaction.isReversed()).thenReturn(false);
        when(chargeWaiverTransaction.isWaiveCharge()).thenReturn(true);
        when(chargeWaiverTransaction.getTransactionDate()).thenReturn(tillDate);

        // When
        BigDecimal result = ReflectionTestUtils.invokeMethod(accrualsProcessingService, "calcChargeWaivedAmount",
                List.of(nullTransactionPaidBy, chargeWaiverPaidBy), tillDate);

        // Then
        assertThat(result).isEqualByComparingTo("12.34");
    }

    private static Stream<Arguments> loanStatusTestCases() {
        return Stream.of(Arguments.of(true, false), // Loan is closed
                Arguments.of(false, true) // Loan is overpaid
        );
    }
}
