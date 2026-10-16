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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

class LoanProductRelatedDetailUpdateUtilTest {

    private static final String PARAM = LoanProductConstants.DAYS_IN_YEAR_CUSTOM_STRATEGY_TYPE_PARAMETER_NAME;

    // The code under test dereferences enum, BigDecimal and Money getters of the mocked domain objects unconditionally.
    private static final Answer<Object> DOMAIN_DEFAULTS = new Answer<>() {

        @Override
        public Object answer(final InvocationOnMock invocation) throws Throwable {
            final Class<?> returnType = invocation.getMethod().getReturnType();
            if (returnType.isEnum()) {
                return returnType.getEnumConstants()[0];
            }
            if (returnType == BigDecimal.class) {
                return BigDecimal.ZERO;
            }
            if (returnType == Money.class) {
                return mock(Money.class, this);
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        }
    };

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final LoanProductRelatedDetailUpdateUtil underTest = new LoanProductRelatedDetailUpdateUtil();

    private LoanProductRelatedDetail detail;

    @BeforeEach
    void setUp() {
        detail = mock(LoanProductRelatedDetail.class, withSettings().defaultAnswer(DOMAIN_DEFAULTS));
        when(detail.getCurrency()).thenReturn(new MonetaryCurrency("USD", 2, null));
    }

    @Test
    void nullClearsExistingStrategy() {
        when(detail.getDaysInYearCustomStrategy()).thenReturn(DaysInYearCustomStrategyType.FULL_LEAP_YEAR);

        final Map<String, Object> changes = update("{\"" + PARAM + "\": null}");

        assertTrue(changes.containsKey(PARAM));
        assertNull(changes.get(PARAM));
        verify(detail).setDaysInYearCustomStrategy(null);
    }

    @Test
    void newValueIsRecordedAndApplied() {
        when(detail.getDaysInYearCustomStrategy()).thenReturn(DaysInYearCustomStrategyType.FULL_LEAP_YEAR);

        final Map<String, Object> changes = update("{\"" + PARAM + "\": \"FEB_29_PERIOD_ONLY\"}");

        assertEquals("FEB_29_PERIOD_ONLY", changes.get(PARAM));
        verify(detail).setDaysInYearCustomStrategy(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY);
    }

    @Test
    void unchangedValueIsNotRecorded() {
        when(detail.getDaysInYearCustomStrategy()).thenReturn(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY);

        final Map<String, Object> changes = update("{\"" + PARAM + "\": \"FEB_29_PERIOD_ONLY\"}");

        assertFalse(changes.containsKey(PARAM));
        verify(detail, never()).setDaysInYearCustomStrategy(any());
    }

    @Test
    void unchangedNullIsNotRecorded() {
        when(detail.getDaysInYearCustomStrategy()).thenReturn(null);

        final Map<String, Object> changes = update("{\"" + PARAM + "\": null}");

        assertFalse(changes.containsKey(PARAM));
        verify(detail, never()).setDaysInYearCustomStrategy(any());
    }

    @Test
    void invalidValueIsRejectedAsValidationError() {
        assertThrows(PlatformApiDataValidationException.class, () -> update("{\"" + PARAM + "\": \"NOT_A_STRATEGY\"}"));
        verify(detail, never()).setDaysInYearCustomStrategy(any());
    }

    @Test
    void absentParameterLeavesStrategyUntouched() {
        final Map<String, Object> changes = new HashMap<>();

        underTest.updateDaysInYearCustomStrategy(detail, new JsonCommand(1L, fromJsonHelper.parse("{}"), fromJsonHelper), changes);

        assertTrue(changes.isEmpty());
        verify(detail, never()).setDaysInYearCustomStrategy(any());
    }

    private Map<String, Object> update(final String json) {
        return underTest.updateLoanRepaymentSchedule(detail, new JsonCommand(1L, fromJsonHelper.parse(json), fromJsonHelper), null);
    }
}
