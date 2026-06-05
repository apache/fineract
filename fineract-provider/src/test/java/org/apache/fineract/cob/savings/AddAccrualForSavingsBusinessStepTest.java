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
package org.apache.fineract.cob.savings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.exception.MultiException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.service.SavingsAccrualWritePlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddAccrualForSavingsBusinessStepTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2024, 6, 1);
    private static final Long ACCOUNT_ID = 42L;

    @Mock
    private SavingsAccrualWritePlatformService savingsAccrualWritePlatformService;
    @Mock
    private SavingsAccount savingsAccount;
    @InjectMocks
    private AddAccrualForSavingsBusinessStep underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldAddAccrualEntriesUpToBusinessDate() throws MultiException {
        final SavingsAccount result = underTest.execute(savingsAccount);

        // accruals are booked till the business date 2024-06-01 for this account only
        verify(savingsAccrualWritePlatformService).addAccrualEntries(savingsAccount, BUSINESS_DATE);
        verifyNoMoreInteractions(savingsAccrualWritePlatformService);
        assertThat(result).isSameAs(savingsAccount);
    }

    @Test
    void shouldWrapMultiExceptionIntoBusinessStepExceptionMentioningTheAccountId() throws MultiException {
        when(savingsAccount.getId()).thenReturn(ACCOUNT_ID);
        final MultiException failure = new MultiException(List.of(new IllegalStateException("accrual failed")));
        doThrow(failure).when(savingsAccrualWritePlatformService).addAccrualEntries(savingsAccount, BUSINESS_DATE);

        assertThatThrownBy(() -> underTest.execute(savingsAccount)) //
                .isExactlyInstanceOf(BusinessStepException.class) //
                .hasMessage("Fail to process accrual transactions for savings account id [" + ACCOUNT_ID + "]") //
                .hasCause(failure);
    }

    @Test
    void shouldExposeStepNames() {
        assertThat(underTest.getEnumStyledName()).isEqualTo("ADD_ACCRUAL_TRANSACTIONS_FOR_SAVINGS");
        assertThat(underTest.getHumanReadableName()).isEqualTo("Add accrual transactions for savings");
    }
}
