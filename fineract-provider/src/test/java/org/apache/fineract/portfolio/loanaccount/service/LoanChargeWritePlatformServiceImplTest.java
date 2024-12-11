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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanChargeWritePlatformServiceImplTest {

    private static final Long LOAN_ID = 1L;
    private static final Integer SPECIFIED_DUE_DATE = 2;
    private static final LocalDate MATURITY_DATE = LocalDate.of(2024, 2, 15);
    private static final LocalDate BUSINESS_DATE_AFTER = LocalDate.of(2024, 2, 26);
    private static final LocalDate BUSINESS_DATE_ON = MATURITY_DATE;
    private static final LocalDate BUSINESS_DATE_BEFORE = LocalDate.of(2024, 2, 14);

    @InjectMocks
    private LoanChargeWritePlatformServiceImpl loanChargeWritePlatformService;

    @Mock
    private JsonCommand jsonCommand;

    @Mock
    private LoanChargeApiJsonValidator loanChargeApiJsonValidator;

    @Mock
    private LoanAssembler loanAssembler;

    @Mock
    private Loan loan;

    @Mock
    private ChargeRepositoryWrapper chargeRepository;

    @Mock
    private Charge chargeDefinition;

    @Mock
    private LoanChargeAssembler loanChargeAssembler;

    @Mock
    private LoanCharge loanCharge;

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private LoanProductRelatedDetail loanRepaymentScheduleDetail;

    @Mock
    private LoanChargeRepository loanChargeRepository;

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private LoanTransactionRepository loanTransactionRepository;

    @Mock
    private LoanTransaction loanTransaction;

    @Mock
    private LoanAccountDomainService loanAccountDomainService;

    @Mock
    private MonetaryCurrency monetaryCurrency;

    @Mock
    private JournalEntryWritePlatformService journalEntryWritePlatformService;

    @Mock
    private LoanChargeValidator loanChargeValidator;

    @Mock
    private LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService;

    @BeforeEach
    void setUp() {
        when(loanAssembler.assembleFrom(LOAN_ID)).thenReturn(loan);
        when(chargeRepository.findOneWithNotFoundDetection(anyLong())).thenReturn(chargeDefinition);
        when(chargeDefinition.getChargeTimeType()).thenReturn(SPECIFIED_DUE_DATE);
        when(loanChargeAssembler.createNewFromJson(loan, chargeDefinition, jsonCommand)).thenReturn(loanCharge);
        when(loan.repaymentScheduleDetail()).thenReturn(loanRepaymentScheduleDetail);
        when(loan.hasCurrencyCodeOf(any())).thenReturn(true);
        when(loanCharge.getChargePaymentMode()).thenReturn(ChargePaymentMode.REGULAR);
        when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);
        when(loanChargeRepository.saveAndFlush(any(LoanCharge.class))).thenReturn(loanCharge);
        when(loan.getCurrency()).thenReturn(monetaryCurrency);
        when(loanAccountDomainService.saveAndFlushLoanWithDataIntegrityViolationChecks(any())).thenReturn(loan);
    }

    @ParameterizedTest
    @MethodSource("loanChargeAccrualTestCases")
    void shouldHandleAccrualBasedOnConfigurationAndDates(boolean isAccrualEnabled, LocalDate businessDate, LocalDate maturityDate, boolean isAccrualExpected) {
        when(configurationDomainService.isImmediateChargeAccrualPostMaturityEnabled()).thenReturn(isAccrualEnabled);
        when(loan.getMaturityDate()).thenReturn(maturityDate);
        when(loan.handleChargeAppliedTransaction(loanCharge, null)).thenReturn(loanTransaction);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(businessDate);

            loanChargeWritePlatformService.addLoanCharge(LOAN_ID, jsonCommand);
        }

        if (isAccrualExpected) {
            verify(loanTransactionRepository, times(1)).saveAndFlush(any(LoanTransaction.class));
            verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(any(LoanAccrualTransactionCreatedBusinessEvent.class));
        } else {
            verify(loanTransactionRepository, never()).saveAndFlush(any(LoanTransaction.class));
            verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any(LoanAccrualTransactionCreatedBusinessEvent.class));
        }
    }

    private static Stream<Arguments> loanChargeAccrualTestCases() {
        return Stream.of(Arguments.of(true, BUSINESS_DATE_AFTER, MATURITY_DATE, true),
                Arguments.of(false, BUSINESS_DATE_AFTER, MATURITY_DATE, false), Arguments.of(true, BUSINESS_DATE_ON, MATURITY_DATE, false),
                Arguments.of(true, BUSINESS_DATE_BEFORE, MATURITY_DATE, false));
    }
}
