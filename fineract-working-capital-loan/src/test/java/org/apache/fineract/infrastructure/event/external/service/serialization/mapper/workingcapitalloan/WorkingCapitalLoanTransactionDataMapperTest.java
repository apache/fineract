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
package org.apache.fineract.infrastructure.event.external.service.serialization.mapper.workingcapitalloan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanTransactionDataV1;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.generic.CurrencyDataMapperImpl;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroDateTimeMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.ExternalIdMapper;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargePaidByData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanTransactionDataMapperTest {

    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2026, 8, 20);
    private static final LocalDate SUBMITTED_ON_DATE = LocalDate.of(2026, 8, 21);
    private static final LocalDate REVERSED_ON_DATE = LocalDate.of(2026, 8, 22);

    private final WorkingCapitalLoanTransactionDataMapper mapper = new WorkingCapitalLoanTransactionDataMapperImpl(
            new CurrencyDataMapperImpl(), new AvroDateTimeMapper(), new ExternalIdMapper());

    @Test
    public void map_nullSource_returnsNull() {
        assertNull(mapper.map(null));
    }

    @Test
    public void map_transactionData_mapsScalarNestedAndTypeFields() {
        final WorkingCapitalLoanTransactionData source = transactionData(LoanTransactionType.CHARGE_ADJUSTMENT, false, null);

        final WorkingCapitalLoanTransactionDataV1 result = mapper.map(source);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(200L, result.getWcLoanId());
        assertEquals(TRANSACTION_DATE.toString(), result.getTransactionDate());
        assertEquals(SUBMITTED_ON_DATE.toString(), result.getSubmittedOnDate());
        assertEquals(new BigDecimal("125.50"), result.getTransactionAmount());
        assertEquals("transaction-external-id", result.getExternalId());
        assertEquals("reversal-external-id", result.getReversalExternalId());
        assertEquals(Boolean.FALSE, result.getReversed());
        assertEquals("EUR", result.getCurrency().getCode());
        assertEquals(2, result.getCurrency().getDecimalPlaces());

        assertNotNull(result.getType());
        assertEquals(LoanTransactionType.CHARGE_ADJUSTMENT.getValue(), result.getType().getId());
        assertEquals(LoanTransactionType.CHARGE_ADJUSTMENT.getCode(), result.getType().getCode());
        assertEquals("Charge Adjustment", result.getType().getValue());
        assertEquals(Boolean.TRUE, result.getType().getChargeAdjustment());
        assertEquals(Boolean.FALSE, result.getType().getRepayment());

        assertNotNull(result.getClassification());
        assertEquals(7L, result.getClassification().getId());
        assertEquals("Discount", result.getClassification().getName());

        assertNotNull(result.getPaymentDetailData());
        assertEquals(8L, result.getPaymentDetailData().getId());
        assertEquals("account-1", result.getPaymentDetailData().getAccountNumber());
        assertEquals("check-1", result.getPaymentDetailData().getCheckNumber());
        assertEquals("cash", result.getPaymentDetailData().getPaymentType().getName());

        assertEquals(new BigDecimal("100.00"), result.getPrincipalPortion());
        assertEquals(new BigDecimal("20.00"), result.getFeeChargesPortion());
        assertEquals(new BigDecimal("5.00"), result.getPenaltyChargesPortion());
        assertEquals(new BigDecimal("0.50"), result.getOverpaymentPortion());
        assertNotNull(result.getChargePaidByList());
        assertEquals(1, result.getChargePaidByList().size());
        assertEquals(9L, result.getChargePaidByList().get(0).getId());
        assertEquals(new BigDecimal("20.00"), result.getChargePaidByList().get(0).getAmount());
        assertEquals("Service fee", result.getChargePaidByList().get(0).getName());
    }

    @Test
    public void map_reversedOnDate_marksTransactionAsReversed() {
        final WorkingCapitalLoanTransactionData source = transactionData(LoanTransactionType.REPAYMENT, false, REVERSED_ON_DATE);

        final WorkingCapitalLoanTransactionDataV1 result = mapper.map(source);

        assertEquals(Boolean.TRUE, result.getReversed());
        assertEquals(REVERSED_ON_DATE.toString(), result.getReversedOnDate());
    }

    private static WorkingCapitalLoanTransactionData transactionData(final LoanTransactionType transactionType, final boolean reversed,
            final LocalDate reversedOnDate) {
        final LoanTransactionEnumData type = LoanEnumerations.transactionType(transactionType);
        return WorkingCapitalLoanTransactionData.builder().id(100L).wcLoanId(200L)
                .currency(new CurrencyData("EUR", "Euro", 2, 1, "€", "currency.EUR")).type(type).transactionDate(TRANSACTION_DATE)
                .submittedOnDate(SUBMITTED_ON_DATE).transactionAmount(new BigDecimal("125.50"))
                .externalId(new ExternalId("transaction-external-id")).reversed(reversed)
                .reversalExternalId(new ExternalId("reversal-external-id")).reversedOnDate(reversedOnDate)
                .classification(CodeValueData.instance(7L, "Discount", "Discount classification", 1, true, false))
                .paymentDetailData(PaymentDetailData.builder().id(8L)
                        .paymentType(PaymentTypeData.builder().id(1L).name("cash").description("Cash").isCashPayment(true).position(1L)
                                .codeName("cash").isSystemDefined(true).build())
                        .accountNumber("account-1").checkNumber("check-1").routingCode("routing-1").receiptNumber("receipt-1")
                        .bankNumber("bank-1").build())
                .principalPortion(new BigDecimal("100.00")).feeChargesPortion(new BigDecimal("20.00"))
                .penaltyChargesPortion(new BigDecimal("5.00")).overpaymentPortion(new BigDecimal("0.50"))
                .chargePaidByList(List.of(WorkingCapitalLoanChargePaidByData.builder().id(9L).amount(new BigDecimal("20.00")).chargeId(10L)
                        .transactionId(100L).name("Service fee").build()))
                .build();
    }
}
