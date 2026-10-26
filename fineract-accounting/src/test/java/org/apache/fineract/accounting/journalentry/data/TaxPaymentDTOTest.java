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
package org.apache.fineract.accounting.journalentry.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class TaxPaymentDTOTest {

    private static MockedStatic<MoneyHelper> moneyHelper;

    @BeforeAll
    static void setUp() {
        moneyHelper = mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterAll
    static void tearDown() {
        moneyHelper.close();
    }

    @Test
    void shareAndRemainderSplitEachComponentByTheLegAmounts() {
        final List<TaxPaymentDTO> taxPayments = List.of(new TaxPaymentDTO(null, 7L, new BigDecimal("10.00")),
                new TaxPaymentDTO(null, 8L, new BigDecimal("5.00")));

        final List<TaxPaymentDTO> share = TaxPaymentDTO.share(taxPayments, new BigDecimal("60"), new BigDecimal("100"));
        final List<TaxPaymentDTO> remainder = TaxPaymentDTO.remainder(taxPayments, share);

        assertThat(share).extracting(TaxPaymentDTO::getCreditAccountId).containsExactly(7L, 8L);
        assertThat(share).extracting(TaxPaymentDTO::getAmount).containsExactly(new BigDecimal("6.00"), new BigDecimal("3.00"));
        assertThat(remainder).extracting(TaxPaymentDTO::getAmount).containsExactly(new BigDecimal("4.00"), new BigDecimal("2.00"));
    }

    @Test
    void shareAndRemainderAlwaysAddUpToTheTaxWhenTheShareRounds() {
        final List<TaxPaymentDTO> taxPayments = List.of(new TaxPaymentDTO(null, 7L, new BigDecimal("1.00")));

        final List<TaxPaymentDTO> share = TaxPaymentDTO.share(taxPayments, BigDecimal.ONE, new BigDecimal("3"));
        final List<TaxPaymentDTO> remainder = TaxPaymentDTO.remainder(taxPayments, share);

        assertThat(share.get(0).getAmount()).isEqualByComparingTo("0.33");
        assertThat(share.get(0).getAmount().add(remainder.get(0).getAmount())).isEqualByComparingTo("1.00");
    }
}
