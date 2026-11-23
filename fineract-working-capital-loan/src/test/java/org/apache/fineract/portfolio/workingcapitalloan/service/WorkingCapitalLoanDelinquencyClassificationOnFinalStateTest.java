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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The classification step at the end of a Working Capital loan command classifies as of the business date, and goes
 * through {@code classifyDelinquency}, so an extension that overrides it sees every command's classification. Only the
 * in-place repayment path depends on the instant delinquency calculation setting, and only while the loan stays in the
 * COB scope.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDelinquencyClassificationOnFinalStateTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 6, 1);

    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleRepository delinquencyRangeScheduleRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository delinquencyRangeScheduleTagHistoryRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyActionRepository delinquencyActionRepository;
    @Mock
    private GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private GlobalConfigurationPropertyData instantDelinquencyCalculation;
    @Mock
    private WorkingCapitalLoan loan;

    private WorkingCapitalLoanDelinquencyClassificationServiceImpl classificationService;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
        when(globalConfigurationRepository
                .findOneByNameWithNotFoundDetection(GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION))
                .thenReturn(instantDelinquencyCalculation);
        classificationService = spy(new WorkingCapitalLoanDelinquencyClassificationServiceImpl(delinquencyRangeScheduleRepository,
                delinquencyRangeScheduleTagHistoryRepository, delinquencyActionRepository, globalConfigurationRepository,
                businessEventNotifierService));
        doNothing().when(classificationService).classifyDelinquency(any(), any());
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void alwaysClassifiesAsOfTheBusinessDateWhateverTheInstantSetting(final boolean instantEnabled) {
        when(instantDelinquencyCalculation.isEnabled()).thenReturn(instantEnabled);

        classificationService.classifyDelinquencyOnFinalState(loan, WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);

        verify(classificationService).classifyDelinquency(loan, BUSINESS_DATE);
    }

    @Test
    void instantClassifiesAsOfTheBusinessDateWhenTheInstantSettingIsOn() {
        when(instantDelinquencyCalculation.isEnabled()).thenReturn(true);

        classificationService.classifyDelinquencyOnFinalState(loan, WorkingCapitalLoanDelinquencyClassificationMode.INSTANT);

        verify(classificationService).classifyDelinquency(loan, BUSINESS_DATE);
    }

    @Test
    void instantDoesNotClassifyAnActiveLoanWhenTheInstantSettingIsOff() {
        when(instantDelinquencyCalculation.isEnabled()).thenReturn(false);
        when(loan.getLoanStatus()).thenReturn(LoanStatus.ACTIVE);

        classificationService.classifyDelinquencyOnFinalState(loan, WorkingCapitalLoanDelinquencyClassificationMode.INSTANT);

        verify(classificationService, never()).classifyDelinquency(any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = LoanStatus.class, names = { "CLOSED_OBLIGATIONS_MET", "OVERPAID" })
    void instantClassifiesALoanLeftOutOfTheCobScopeWhenTheInstantSettingIsOff(final LoanStatus finalStatus) {
        when(instantDelinquencyCalculation.isEnabled()).thenReturn(false);
        when(loan.getLoanStatus()).thenReturn(finalStatus);

        classificationService.classifyDelinquencyOnFinalState(loan, WorkingCapitalLoanDelinquencyClassificationMode.INSTANT);

        verify(classificationService).classifyDelinquency(loan, BUSINESS_DATE);
    }

    @Test
    void noneDoesNotClassify() {
        when(instantDelinquencyCalculation.isEnabled()).thenReturn(true);

        classificationService.classifyDelinquencyOnFinalState(loan, WorkingCapitalLoanDelinquencyClassificationMode.NONE);

        verify(classificationService, never()).classifyDelinquency(any(), any());
    }

    @Test
    void theStrongerModeWinsWhenACommandUpdatesTheScheduleTwice() {
        assertThat(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS.combine(WorkingCapitalLoanDelinquencyClassificationMode.INSTANT))
                .isEqualTo(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        assertThat(WorkingCapitalLoanDelinquencyClassificationMode.INSTANT.combine(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS))
                .isEqualTo(WorkingCapitalLoanDelinquencyClassificationMode.ALWAYS);
        assertThat(WorkingCapitalLoanDelinquencyClassificationMode.NONE.combine(WorkingCapitalLoanDelinquencyClassificationMode.INSTANT))
                .isEqualTo(WorkingCapitalLoanDelinquencyClassificationMode.INSTANT);
        assertThat(WorkingCapitalLoanDelinquencyClassificationMode.INSTANT.combine(null))
                .isEqualTo(WorkingCapitalLoanDelinquencyClassificationMode.INSTANT);
    }
}
