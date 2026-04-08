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
package org.apache.fineract.portfolio.savings.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.savings.data.DepositAccountDataValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FixedDepositAccountInterestCalculationServiceImplTest {

    private FixedDepositAccountInterestCalculationServiceImpl service;

    @Mock
    private DepositAccountDataValidator depositAccountDataValidator;
    @Mock
    private FromJsonHelper fromApiJsonHelper;
    private MockedStatic<MoneyHelper> moneyHelperStatic;

    @BeforeEach
    public void setUp() {
        moneyHelperStatic = Mockito.mockStatic(MoneyHelper.class);
        moneyHelperStatic.when(() -> MoneyHelper.getMathContext()).thenReturn(new MathContext(12, RoundingMode.UP));
        service = new FixedDepositAccountInterestCalculationServiceImpl(depositAccountDataValidator, fromApiJsonHelper);
    }

    @AfterEach
    public void deregister() {
        moneyHelperStatic.close();
    }

    @Test
    public void testCalculateInterestInternal1() {

        // Calculate interest
        BigDecimal expectedInterest = new BigDecimal("10509.4533691406250000"); // Expected interest calculated based
                                                                                // on provided values
        BigDecimal calculatedInterest = service.calculateInterestInternal(BigDecimal.valueOf(10000), BigDecimal.valueOf(5), 12L, 3L);

        // Verify the result
        assertEquals(expectedInterest, calculatedInterest);
    }

    @Test
    public void testCalculateInterestInternal2() {

        // Calculate interest
        BigDecimal expectedInterest = new BigDecimal("105.062500"); // Expected interest calculated based on provided
                                                                    // values
        BigDecimal calculatedInterest = service.calculateInterestInternal(BigDecimal.valueOf(100), BigDecimal.valueOf(5), 12L, 6L);

        // Verify the result
        assertEquals(expectedInterest, calculatedInterest);
    }

}
