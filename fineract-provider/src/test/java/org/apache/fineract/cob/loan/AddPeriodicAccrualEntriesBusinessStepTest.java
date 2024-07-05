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
package org.apache.fineract.cob.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomUtils;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.MultiException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualsProcessingService;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AddPeriodicAccrualEntriesBusinessStepTest {

    @Mock
    private LoanAccrualsProcessingService loanAccrualsProcessingService;

    private AddPeriodicAccrualEntriesBusinessStep underTest;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
        underTest = new AddPeriodicAccrualEntriesBusinessStep(loanAccrualsProcessingService);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void givenLoanWithAccrual() throws MultiException {
        // given
        final Loan loanForProcessing = Mockito.mock(Loan.class);
        // when
        final Loan processedLoan = underTest.execute(loanForProcessing);
        // then
        verify(loanAccrualsProcessingService, times(1)).addPeriodicAccruals(any(LocalDate.class), eq(loanForProcessing));
        assertEquals(processedLoan, loanForProcessing);
    }

    @Test
    public void givenLoanWithAccrualThrowException() throws MultiException {
        // given
        final Long loanId = RandomUtils.nextLong();
        final Loan loanForProcessing = Mockito.mock(Loan.class);
        when(loanForProcessing.getId()).thenReturn(loanId);
        doThrow(new MultiException(Collections.singletonList(new RuntimeException()))).when(loanAccrualsProcessingService)
                .addPeriodicAccruals(any(LocalDate.class), eq(loanForProcessing));
        // when
        final BusinessStepException businessStepException = Assert.assertThrows(BusinessStepException.class,
                () -> underTest.execute(loanForProcessing));
        // then
        verify(loanAccrualsProcessingService, times(1)).addPeriodicAccruals(any(LocalDate.class), eq(loanForProcessing));
        assertEquals(String.format("Fail to process period accrual for loan id [%s]", loanId), businessStepException.getMessage());
    }

    @Test
    public void testGetEnumStyledNameSuccessScenario() {
        final String actualEnumName = underTest.getEnumStyledName();
        assertNotNull(actualEnumName);
        assertEquals("ADD_PERIODIC_ACCRUAL_ENTRIES", actualEnumName);
    }

    @Test
    public void testGetHumanReadableNameSuccessScenario() {
        final String actualEnumName = underTest.getHumanReadableName();
        assertNotNull(actualEnumName);
        assertEquals("Add periodic accrual entries", actualEnumName);
    }
}
