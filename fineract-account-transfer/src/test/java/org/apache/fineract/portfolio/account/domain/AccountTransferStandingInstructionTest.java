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
package org.apache.fineract.portfolio.account.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.junit.jupiter.api.Test;

class AccountTransferStandingInstructionTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();

    @Test
    void allowPartialTransferCanBeTurnedOnThroughUpdate() {
        final AccountTransferStandingInstruction instruction = savingsTransferInstruction(false);

        final Map<String, Object> changes = instruction.update(command("{\"allowPartialTransfer\": true}"));

        assertThat(changes).containsEntry("allowPartialTransfer", true);
        // the new value is stored: sending it again is no longer a change
        assertThat(instruction.update(command("{\"allowPartialTransfer\": true}"))).doesNotContainKey("allowPartialTransfer");
    }

    @Test
    void updateWithoutTheFlagLeavesItUnchanged() {
        final AccountTransferStandingInstruction instruction = savingsTransferInstruction(true);

        final Map<String, Object> changes = instruction.update(command("{\"name\": \"renamed\"}"));

        assertThat(changes).doesNotContainKey("allowPartialTransfer");
        assertThat(instruction.update(command("{\"allowPartialTransfer\": true}"))).doesNotContainKey("allowPartialTransfer");
    }

    private AccountTransferStandingInstruction savingsTransferInstruction(final boolean allowPartialTransfer) {
        final AccountTransferDetails details = mock(AccountTransferDetails.class);
        when(details.fromSavingsAccount()).thenReturn(mock(SavingsAccount.class));
        when(details.toSavingsAccount()).thenReturn(mock(SavingsAccount.class));
        when(details.transferType()).thenReturn(AccountTransferType.ACCOUNT_TRANSFER);
        return AccountTransferStandingInstruction.create(details, "monthly savings", StandingInstructionPriority.MEDIUM.getValue(),
                StandingInstructionType.FIXED.getValue(), StandingInstructionStatus.ACTIVE.getValue(), BigDecimal.valueOf(100),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), AccountTransferRecurrenceType.PERIODIC.getValue(),
                PeriodFrequencyType.MONTHS.getValue(), 1, MonthDay.of(1, 15), allowPartialTransfer);
    }

    private JsonCommand command(final String json) {
        return new JsonCommand(1L, fromJsonHelper.parse(json), fromJsonHelper);
    }
}
