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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.stream.Stream;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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

    private static Stream<Arguments> loanStatusTestCases() {
        return Stream.of(Arguments.of(true, false), // Loan is closed
                Arguments.of(false, true) // Loan is overpaid
        );
    }
}
