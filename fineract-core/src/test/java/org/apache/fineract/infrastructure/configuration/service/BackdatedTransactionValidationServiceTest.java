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
package org.apache.fineract.infrastructure.configuration.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BackdatedTransactionValidationServiceTest {

    @Mock
    private ConfigurationDomainService configurationDomainService;

    private BackdatedTransactionValidationService underTest;

    @BeforeEach
    public void setUp() {
        underTest = new BackdatedTransactionValidationService(configurationDomainService);
        setBusinessDate(LocalDate.of(2026, 7, 11));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    private void setBusinessDate(final LocalDate date) {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, date)));
    }

    @Test
    public void whenConfigurationDisabledAnyDateIsAllowed() {
        when(configurationDomainService.isBackdatedTransactionsDisallowed()).thenReturn(false);

        assertDoesNotThrow(() -> underTest.validateTransactionDate(LocalDate.of(2020, 1, 1)));
    }

    @Test
    public void nullTransactionDateIsIgnored() {
        assertDoesNotThrow(() -> underTest.validateTransactionDate(null));
    }

    @Test
    public void currentBusinessDateIsAllowedByDefault() {
        when(configurationDomainService.isBackdatedTransactionsDisallowed()).thenReturn(true);

        assertDoesNotThrow(() -> underTest.validateTransactionDate(LocalDate.of(2026, 7, 11)));
    }

    @Test
    public void previousDayIsRejectedByDefault() {
        when(configurationDomainService.isBackdatedTransactionsDisallowed()).thenReturn(true);

        assertThrows(GeneralPlatformDomainRuleException.class, () -> underTest.validateTransactionDate(LocalDate.of(2026, 7, 10)));
    }

    @Test
    public void toleranceDaysExtendTheAllowedWindow() {
        when(configurationDomainService.isBackdatedTransactionsDisallowed()).thenReturn(true);
        when(configurationDomainService.retrieveBackdatedTransactionsToleranceDays()).thenReturn(7L);

        // business date 2026-07-11 with a 7-day window: earliest allowed date is 2026-07-04
        assertDoesNotThrow(() -> underTest.validateTransactionDate(LocalDate.of(2026, 7, 4)));
        assertThrows(GeneralPlatformDomainRuleException.class, () -> underTest.validateTransactionDate(LocalDate.of(2026, 7, 3)));
    }
}
