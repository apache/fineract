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
package org.apache.fineract.accounting.journalentry.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.junit.jupiter.api.Test;

class JournalEntryCommandTest {

    @Test
    void validateForCreateRejectsZeroCreditAndDebit() {
        final JournalEntryCommand command = commandWith(BigDecimal.ZERO, BigDecimal.ZERO);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                command::validateForCreate);

        assertThat(exception.getErrors()).extracting(ApiParameterError::getDefaultUserMessage).containsExactly(
                "The parameter `credits[0].amount` must be greater than 0.", "The parameter `debits[0].amount` must be greater than 0.");
    }

    @Test
    void validateForCreateRejectsZeroWrittenWithDecimals() {
        final JournalEntryCommand command = commandWith(new BigDecimal("0.00"), BigDecimal.TEN);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                command::validateForCreate);

        assertThat(exception.getErrors()).extracting(ApiParameterError::getParameterName).containsExactly("credits[0].amount");
    }

    @Test
    void validateForCreateRejectsNegativeAmount() {
        final JournalEntryCommand command = commandWith(new BigDecimal("-5"), new BigDecimal("-5"));

        assertThrows(PlatformApiDataValidationException.class, command::validateForCreate);
    }

    @Test
    void validateForCreateAcceptsPositiveAmounts() {
        final JournalEntryCommand command = commandWith(new BigDecimal("0.01"), new BigDecimal("0.01"));

        assertThatCode(command::validateForCreate).doesNotThrowAnyException();
    }

    @Test
    void validateForOpeningBalanceStillAcceptsZeroLine() {
        final JournalEntryCommand command = commandWith(BigDecimal.ZERO, BigDecimal.ZERO);

        assertThatCode(command::validateForOpeningBalance).doesNotThrowAnyException();
    }

    @Test
    void validateForOpeningBalanceStillRejectsNegativeAmount() {
        final JournalEntryCommand command = commandWith(new BigDecimal("-5"), BigDecimal.TEN);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                command::validateForOpeningBalance);

        assertThat(exception.getErrors()).extracting(ApiParameterError::getDefaultUserMessage)
                .containsExactly("The parameter `credits[0].amount` must be greater than or equal to 0.");
    }

    private JournalEntryCommand commandWith(final BigDecimal creditAmount, final BigDecimal debitAmount) {
        final SingleDebitOrCreditEntryCommand[] credits = { line(1L, creditAmount) };
        final SingleDebitOrCreditEntryCommand[] debits = { line(2L, debitAmount) };
        return new JournalEntryCommand(1L, "USD", LocalDate.of(2026, 11, 5), "comment", null, null, null, null, null, null, null, null,
                null, credits, debits, "en", "dd MMMM yyyy", null);
    }

    private SingleDebitOrCreditEntryCommand line(final Long glAccountId, final BigDecimal amount) {
        return new SingleDebitOrCreditEntryCommand(glAccountId, amount, null, Set.of());
    }
}
