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
    void aChargePaidInSeveralRowsHasItsTaxSubtractedOnce() {
        // an instalment fee of 2 x 500.00 paid at once, carrying 93.50 + 93.49 of tax
        List<ChargePaymentDTO> net = underTest.computeNetChargePayments(
                List.of(new ChargePaymentDTO(1L, new BigDecimal("500.00"), 9L), new ChargePaymentDTO(1L, new BigDecimal("500.00"), 9L)),
                List.of(tax(9L, "93.50"), tax(9L, "93.49")));

        assertEquals(List.of(new BigDecimal("313.01"), new BigDecimal("500.00")), amounts(net));
        assertEquals(new BigDecimal("813.01"), sum(net));
    }

    @Test
    void aRowSmallerThanTheChargesTaxGoesNegativeAndTheChargeNetsOut() {
        // the rows are summed per GL account afterwards, so only the charge's total matters
        List<ChargePaymentDTO> net = underTest.computeNetChargePayments(
                List.of(new ChargePaymentDTO(1L, new BigDecimal("100.00"), 9L), new ChargePaymentDTO(1L, new BigDecimal("900.00"), 9L)),
                List.of(tax(9L, "18.70"), tax(9L, "168.29")));

        assertEquals(List.of(new BigDecimal("-86.99"), new BigDecimal("900.00")), amounts(net));
        assertEquals(new BigDecimal("813.01"), sum(net));
    }

    @Test
    void eachChargeHasItsOwnTaxSubtracted() {
        List<ChargePaymentDTO> net = underTest.computeNetChargePayments(List.of(new ChargePaymentDTO(1L, new BigDecimal("100.00"), 9L),
                new ChargePaymentDTO(2L, new BigDecimal("50.00"), 10L), new ChargePaymentDTO(3L, new BigDecimal("20.00"), 11L)),
                List.of(tax(9L, "18.70"), tax(10L, "9.35")));

        assertEquals(List.of(new BigDecimal("81.30"), new BigDecimal("40.65"), new BigDecimal("20.00")), amounts(net));
    }

    private static ChargeTaxPaymentDTO tax(Long loanChargeId, String amount) {
        return new ChargeTaxPaymentDTO(loanChargeId, TAX_ACCOUNT_ID, new BigDecimal(amount), false);
    }

    private static List<BigDecimal> amounts(List<ChargePaymentDTO> payments) {
        return payments.stream().map(ChargePaymentDTO::getAmount).toList();
    }

    private static BigDecimal sum(List<ChargePaymentDTO> payments) {
        return payments.stream().map(ChargePaymentDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
