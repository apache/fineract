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

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Recovery payments are capped by what is still recoverable, not by the gross amount that was written off. Term loan
 * caps each recovery against the gross figure instead, so N recoveries can each pass on their own and together collect
 * more than was ever written off; Working Capital deliberately diverges here.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanBalanceRecoveryCapTest {

    @Mock
    private WorkingCapitalLoan loan;

    private WorkingCapitalLoanBalance balance;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());
        balance = WorkingCapitalLoanBalance.createFor(loan);
        // A loan written off for 100 principal + 10 fee + 5 penalty.
        balance.setPrincipalWrittenOff(new BigDecimal("100"));
        balance.setFeeWrittenOff(new BigDecimal("10"));
        balance.setPenaltyWrittenOff(new BigDecimal("5"));
    }

    @AfterEach
    void tearDown() {
        MoneyHelper.clearCacheForTenant("default");
        ThreadLocalContextUtil.reset();
    }

    @Test
    void everythingWrittenOffIsRecoverableBeforeAnyRecovery() {
        assertThat(balance.getTotalWrittenOff()).isEqualByComparingTo(new BigDecimal("115"));
        assertThat(balance.getWrittenOffOutstanding()).isEqualByComparingTo(new BigDecimal("115"));
    }

    @Test
    void eachRecoveryLowersWhatIsStillRecoverable() {
        balance.setTotalRecovered(new BigDecimal("40"));
        assertThat(balance.getWrittenOffOutstanding()).isEqualByComparingTo(new BigDecimal("75"));

        balance.setTotalRecovered(new BigDecimal("115"));
        assertThat(balance.getWrittenOffOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void grossWrittenOffStaysUnchangedAsRecoveriesComeIn() {
        balance.setTotalRecovered(new BigDecimal("115"));

        // The gross figure is the accounting record of the loss and must not shrink; only the recoverable view does.
        assertThat(balance.getTotalWrittenOff()).isEqualByComparingTo(new BigDecimal("115"));
    }

    @Test
    void recoverableNeverGoesNegative() {
        // Defensive: a stored total that overshoots (data repair, historical migration) must not report a negative cap.
        balance.setTotalRecovered(new BigDecimal("200"));

        assertThat(balance.getWrittenOffOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
