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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.businessdate.data.service.BusinessDateDTO;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDate;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateRepository;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.mapper.BusinessDateMapper;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformServiceImpl;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalSubmittedOnDateTest {

    private static final LocalDate CONFIGURED_BUSINESS_DATE = LocalDate.of(2019, 3, 14);
    private static final LocalDate RATE_EFFECTIVE_DATE = LocalDate.of(2018, 6, 1);

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private BusinessDateRepository repository;

    @Mock
    private BusinessDateMapper businessDateMapper;

    @InjectMocks
    private BusinessDateReadPlatformServiceImpl businessDateReadPlatformService;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void periodPaymentRateChangeSubmittedOnDateIsConfiguredDateWhenBusinessDateEnabled() {
        givenBusinessDateEnabled(CONFIGURED_BUSINESS_DATE);
        loadResolvedDatesOntoRequestContext();

        final WorkingCapitalLoanPeriodPaymentRateChange change = createPeriodPaymentRateChange();

        assertThat(change.getSubmittedOnDate()).isEqualTo(CONFIGURED_BUSINESS_DATE);
        assertThat(change.getSubmittedOnDate()).isNotEqualTo(DateUtils.getLocalDateOfTenant());
    }

    @Test
    void periodPaymentRateChangeSubmittedOnDateIsTenantDateWhenBusinessDateDisabled() {
        givenBusinessDateDisabled();
        loadResolvedDatesOntoRequestContext();

        final WorkingCapitalLoanPeriodPaymentRateChange change = createPeriodPaymentRateChange();

        assertThat(change.getSubmittedOnDate()).isEqualTo(DateUtils.getLocalDateOfTenant());
        verify(repository, never()).findAllBusinessDates();
    }

    @Test
    void nearBreachActionSubmittedOnDateIsConfiguredDateWhenBusinessDateEnabled() {
        givenBusinessDateEnabled(CONFIGURED_BUSINESS_DATE);
        loadResolvedDatesOntoRequestContext();

        final WorkingCapitalLoanNearBreachAction action = createNearBreachAction();

        assertThat(action.getSubmittedOnDate()).isEqualTo(CONFIGURED_BUSINESS_DATE);
        assertThat(action.getSubmittedOnDate()).isNotEqualTo(DateUtils.getLocalDateOfTenant());
    }

    @Test
    void nearBreachActionSubmittedOnDateIsTenantDateWhenBusinessDateDisabled() {
        givenBusinessDateDisabled();
        loadResolvedDatesOntoRequestContext();

        final WorkingCapitalLoanNearBreachAction action = createNearBreachAction();

        assertThat(action.getSubmittedOnDate()).isEqualTo(DateUtils.getLocalDateOfTenant());
        verify(repository, never()).findAllBusinessDates();
    }

    private void givenBusinessDateEnabled(final LocalDate configuredBusinessDate) {
        final List<BusinessDate> stored = List.of(mock(BusinessDate.class));
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(true);
        given(repository.findAllBusinessDates()).willReturn(stored);
        given(businessDateMapper.mapEntity(stored))
                .willReturn(List.of(BusinessDateDTO.builder().type(BusinessDateType.BUSINESS_DATE).date(configuredBusinessDate).build()));
    }

    private void givenBusinessDateDisabled() {
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(false);
    }

    private void loadResolvedDatesOntoRequestContext() {
        ThreadLocalContextUtil.setBusinessDates(businessDateReadPlatformService.getBusinessDates());
    }

    private static WorkingCapitalLoanPeriodPaymentRateChange createPeriodPaymentRateChange() {
        return WorkingCapitalLoanPeriodPaymentRateChange.create(mock(WorkingCapitalLoan.class), RATE_EFFECTIVE_DATE, BigDecimal.ONE,
                new BigDecimal("12.5"));
    }

    private static WorkingCapitalLoanNearBreachAction createNearBreachAction() {
        return WorkingCapitalLoanNearBreachAction.create(mock(WorkingCapitalLoan.class), NearBreachActionType.RESCHEDULE,
                new BigDecimal("10"), 7, WorkingCapitalLoanPeriodFrequencyType.DAYS);
    }
}
