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
package org.apache.fineract.accounting.journalentry.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.accounting.journalentry.data.ChargePaymentDTO;
import org.apache.fineract.accounting.journalentry.data.ChargeTaxPaymentDTO;
import org.junit.jupiter.api.Test;

class LoanCommonAccountingHelperTest {

    private static final Long TAX_ACCOUNT_ID = 77L;

    private final LoanCommonAccountingHelper underTest = new LoanCommonAccountingHelper(mock(AccountingProcessorHelper.class));

    @Test
    void aTaxedChargePaidInSeveralRowsIsMergedAndHasItsTaxSubtractedOnce() {
        // an instalment fee of 2 x 50.00 paid at once, carrying 5.00 of tax per instalment
        List<ChargePaymentDTO> net = underTest.computeNetChargePayments(
                List.of(new ChargePaymentDTO(1L, new BigDecimal("50.00"), 9L), new ChargePaymentDTO(1L, new BigDecimal("50.00"), 9L)),
                List.of(tax(9L, "5.00"), tax(9L, "5.00")));

        assertEquals(1, net.size());
        assertEquals(1L, net.get(0).getChargeId());
        assertEquals(9L, net.get(0).getLoanChargeId());
        assertEquals(0, new BigDecimal("90.00").compareTo(net.get(0).getAmount()));
    }

    @Test
    void untaxedRowsAreKeptAsTheyAre() {
        ChargePaymentDTO first = new ChargePaymentDTO(1L, new BigDecimal("50.00"), 9L);
        ChargePaymentDTO second = new ChargePaymentDTO(1L, new BigDecimal("50.00"), 9L);

        List<ChargePaymentDTO> net = underTest.computeNetChargePayments(List.of(first, second), List.of());

        assertEquals(2, net.size());
        assertSame(first, net.get(0));
        assertSame(second, net.get(1));
    }

    @Test
    void eachChargeHasItsOwnTaxSubtracted() {
        List<ChargePaymentDTO> net = underTest.computeNetChargePayments(List.of(new ChargePaymentDTO(1L, new BigDecimal("100.00"), 9L),
                new ChargePaymentDTO(2L, new BigDecimal("50.00"), 10L), new ChargePaymentDTO(3L, new BigDecimal("20.00"), 11L)),
                List.of(tax(9L, "10.00"), tax(10L, "5.00")));

        assertEquals(List.of(new BigDecimal("90.00"), new BigDecimal("45.00"), new BigDecimal("20.00")),
                net.stream().map(ChargePaymentDTO::getAmount).toList());
    }

    private static ChargeTaxPaymentDTO tax(Long loanChargeId, String amount) {
        return new ChargeTaxPaymentDTO(loanChargeId, TAX_ACCOUNT_ID, new BigDecimal(amount), false);
    }
}
