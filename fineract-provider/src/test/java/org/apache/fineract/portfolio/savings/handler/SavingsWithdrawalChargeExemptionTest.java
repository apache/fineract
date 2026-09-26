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
package org.apache.fineract.portfolio.savings.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeDataValidator;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

class SavingsWithdrawalChargeExemptionTest {

    @AfterEach
    void reset() {
        ThreadLocalContextUtil.reset();
        MoneyHelper.clearCache();
    }

    @ParameterizedTest
    @ValueSource(ints = { 100, 101 })
    void manualPaymentCannotExceedOutstandingCharge(int amount) {
        ThreadLocalContextUtil
                .setTenant(new org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant(1L, "test", "Test", "UTC", null));
        MoneyHelper.initializeTenantRoundingMode("test", 6);
        var date = LocalDate.of(2026, 9, 25);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, date)));
        var currency = new MonetaryCurrency("UGX", 2, 0);
        var account = mock(SavingsAccount.class, org.mockito.Mockito.CALLS_REAL_METHODS);
        org.mockito.Mockito.doReturn(false).when(account).isClosed();
        org.mockito.Mockito.doReturn(false).when(account).isNotActive();
        var charge = mock(SavingsAccountCharge.class);
        ReflectionTestUtils.setField(account, "currency", currency);
        when(account.getCurrency()).thenReturn(currency);
        when(account.getActivationDate()).thenReturn(date.minusDays(1));
        when(charge.getAmountOutstanding(currency)).thenReturn(Money.of(currency, new BigDecimal("100")));
        var expected = mock(SavingsAccountTransaction.class);
        org.mockito.Mockito.doReturn(expected).when(account).payCharge(eq(charge), any(Money.class), eq(date), eq(false), isNull());
        if (amount == 101) {
            assertThrows(PlatformApiDataValidationException.class,
                    () -> account.payCharge(charge, BigDecimal.valueOf(amount), date, DateTimeFormatter.ISO_LOCAL_DATE, false, null));
            verify(account, never()).payCharge(eq(charge), any(Money.class), any(), anyBoolean(), any());
        } else {
            assertThat(account.payCharge(charge, BigDecimal.valueOf(amount), date, DateTimeFormatter.ISO_LOCAL_DATE, false, null))
                    .isSameAs(expected);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "toAccountId", "beneficiary", "paymentTypeId", "withdrawBalance", "transferAmount" })
    void chargePayloadRejectsDestinationAndCashWithdrawalParameters(String parameter) {
        var validator = new SavingsAccountChargeDataValidator(new FromJsonHelper());
        assertThrows(UnsupportedParameterException.class, () -> validator.validatePayCharge(
                "{\"locale\":\"en\",\"amount\":50,\"dueDate\":\"2026-09-25\",\"dateFormat\":\"yyyy-MM-dd\",\"" + parameter + "\":1}"));
    }
}
