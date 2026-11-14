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
package org.apache.fineract.portfolio.savings.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SavingsAccountTransactionDateErrorTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 11, 6);
    private static final LocalDate FUTURE_DATE = LocalDate.of(2030, 1, 1);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Beirut", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void futureDepositReportsTheDateMessageNotTheParameterName() {
        ApiParameterError error = error(() -> account(SavingsAccountStatusType.ACTIVE).deposit(dto(FUTURE_DATE),
                SavingsAccountTransactionType.DEPOSIT, false, 0L, null));

        assertThat(error.getDefaultUserMessage()).isEqualTo("Transaction date cannot be in the future.");
        assertThat(error.getParameterName()).isEqualTo("transactionDate");
        assertThat(error.getValue()).isEqualTo("01 January 2030");
    }

    @Test
    void futureWithdrawalReportsTheDateMessageNotTheParameterName() {
        ApiParameterError error = error(() -> account(SavingsAccountStatusType.ACTIVE).withdraw(dto(FUTURE_DATE), false, false, 0L, null));

        assertThat(error.getDefaultUserMessage()).isEqualTo("Transaction date cannot be in the future.");
        assertThat(error.getParameterName()).isEqualTo("transactionDate");
    }

    @Test
    void depositOnInactiveAccountReportsTheAccountMessage() {
        ApiParameterError error = error(() -> account(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL).deposit(dto(BUSINESS_DATE),
                SavingsAccountTransactionType.DEPOSIT, false, 0L, null));

        assertThat(error.getDefaultUserMessage()).isEqualTo("Transaction is not allowed. Account is not active.");
        assertThat(error.getParameterName()).isEqualTo("transactionDate");
    }

    private static SavingsAccount account(SavingsAccountStatusType status) {
        SavingsAccount account = new SavingsAccount();
        ReflectionTestUtils.setField(account, "status", status.getValue());
        return account;
    }

    private static SavingsAccountTransactionDTO dto(LocalDate transactionDate) {
        return new SavingsAccountTransactionDTO(FORMATTER, transactionDate, BigDecimal.TEN, null, 1L, null);
    }

    private static ApiParameterError error(Runnable transaction) {
        PlatformApiDataValidationException exception = catchThrowableOfType(PlatformApiDataValidationException.class, transaction::run);
        assertThat(exception).isNotNull();
        assertThat(exception.getErrors()).hasSize(1);
        return exception.getErrors().get(0);
    }
}
