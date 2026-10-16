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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionPriority;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

/**
 * Builds the standing-instruction stubs the job tests share: a fixed daily savings-to-savings mandate that falls due on
 * any date, which each test then bends to whatever it needs.
 */
final class StandingInstructionTestData {

    private StandingInstructionTestData() {

    }

    static StandingInstructionData dueFixedSavingsToSavings(final Long id) {
        return dueFixedSavingsToSavings(id, StandingInstructionPriority.MEDIUM);
    }

    static StandingInstructionData dueFixedSavingsToSavings(final Long id, final StandingInstructionPriority priority) {
        final StandingInstructionData data = mock(StandingInstructionData.class);
        final PortfolioAccountData fromAccount = mock(PortfolioAccountData.class);
        final PortfolioAccountData toAccount = mock(PortfolioAccountData.class);
        lenient().when(fromAccount.getId()).thenReturn(id * 10);
        lenient().when(toAccount.getId()).thenReturn(id * 10 + 1);
        lenient().when(data.getId()).thenReturn(id);
        lenient().when(data.getPriority())
                .thenReturn(new EnumOptionData(priority.getValue().longValue(), priority.getCode(), priority.toString()));
        lenient().when(data.getRecurrenceTypeEnum()).thenReturn(AccountTransferRecurrenceType.PERIODIC);
        lenient().when(data.getRecurrenceFrequencyEnum()).thenReturn(PeriodFrequencyType.DAYS);
        lenient().when(data.getValidFrom()).thenReturn(LocalDate.of(2020, 1, 1));
        lenient().when(data.getRecurrenceInterval()).thenReturn(1);
        lenient().when(data.getInstructionTypeEnum()).thenReturn(StandingInstructionType.FIXED);
        lenient().when(data.getAmount()).thenReturn(new BigDecimal("100"));
        lenient().when(data.getFromAccountTypeEnum()).thenReturn(PortfolioAccountType.SAVINGS);
        lenient().when(data.getToAccountTypeEnum()).thenReturn(PortfolioAccountType.SAVINGS);
        lenient().when(data.getFromAccount()).thenReturn(fromAccount);
        lenient().when(data.getToAccount()).thenReturn(toAccount);
        lenient().when(data.getName()).thenReturn("SI-" + id);
        lenient().when(data.getTransferTypeEnum()).thenReturn(AccountTransferType.ACCOUNT_TRANSFER);
        lenient().when(data.toTransferType()).thenReturn(AccountTransferType.ACCOUNT_TRANSFER.getValue());
        return data;
    }
}
