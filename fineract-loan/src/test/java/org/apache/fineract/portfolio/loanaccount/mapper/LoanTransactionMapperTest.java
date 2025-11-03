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
package org.apache.fineract.portfolio.loanaccount.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.mapper.CurrencyMapper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanTransactionMapperTest {

    @Mock
    private CurrencyMapper currencyMapper;

    @Mock
    private LoanTransaction loanTransaction;

    @Mock
    private Loan loan;

    @Mock
    private PaymentDetail paymentDetail;

    @InjectMocks
    private LoanTransactionMapperImpl mapper;

    @BeforeEach
    void setUp() {
        // Setup common mocks
        when(loanTransaction.getLoan()).thenReturn(loan);
        when(loan.getId()).thenReturn(1L);
        when(loan.getCurrency()).thenReturn(new MonetaryCurrency("USD", 2, 0));
        when(currencyMapper.map(any())).thenReturn(new CurrencyData("USD", "US Dollar", 2, 0, "$", "code"));
    }

    @Test
    public void testMapLoanTransaction_MapsAllFieldsCorrectly() {
        // Given
        LocalDate transactionDate = LocalDate.now(ZoneId.of("UTC"));

        // Setup transaction mocks
        when(loanTransaction.getId()).thenReturn(1L);
        when(loanTransaction.getTypeOf()).thenReturn(LoanTransactionType.REPAYMENT);
        when(loanTransaction.getTransactionDate()).thenReturn(transactionDate);
        when(loanTransaction.getDateOf()).thenReturn(transactionDate);
        when(loanTransaction.getAmount()).thenReturn(BigDecimal.valueOf(1000));
        when(loanTransaction.getPrincipalPortion()).thenReturn(BigDecimal.valueOf(800));
        when(loanTransaction.getInterestPortion()).thenReturn(BigDecimal.valueOf(100));
        when(loanTransaction.getFeeChargesPortion()).thenReturn(BigDecimal.valueOf(50));
        when(loanTransaction.getPenaltyChargesPortion()).thenReturn(BigDecimal.valueOf(50));
        when(loanTransaction.getPaymentDetail()).thenReturn(paymentDetail);
        when(loanTransaction.getExternalId()).thenReturn(ExternalId.generate());
        when(loanTransaction.isManuallyAdjustedOrReversed()).thenReturn(false);
        when(loanTransaction.getOffice()).thenReturn(Office.headOffice("Test Office", LocalDate.of(2022, 2, 12), null));
        when(loanTransaction.getLoan().getNetDisbursalAmount()).thenReturn(BigDecimal.valueOf(2000));

        // Setup payment detail mocks
        when(paymentDetail.toData()).thenReturn(new PaymentDetailData(1L, PaymentTypeData.instance(1L, "Cash"), "accountNumber",
                "checkNumber", "routingCode", "receiptNumber", "bankNumber"));

        // When
        LoanTransactionData result = mapper.mapLoanTransaction(loanTransaction);

        // Then - Verify all mapped fields
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(transactionDate, result.getDate());
        assertEquals(BigDecimal.valueOf(1000), result.getAmount());
        assertEquals(BigDecimal.valueOf(800), result.getPrincipalPortion());
        assertEquals(BigDecimal.valueOf(100), result.getInterestPortion());
        assertEquals(BigDecimal.valueOf(50), result.getFeeChargesPortion());
        assertEquals(BigDecimal.valueOf(50), result.getPenaltyChargesPortion());
        assertFalse(result.isManuallyReversed());
        assertEquals("REPAYMENT", result.getTransactionType());
        assertNotNull(result.getType());
        assertNotNull(result.getPaymentDetailData());
        assertNull(result.getOfficeId());
        assertEquals("Test Office", result.getOfficeName());
        assertEquals(1L, result.getLoanId());
        assertEquals(loan.getExternalId(), result.getExternalLoanId());
        assertEquals(BigDecimal.valueOf(2000), result.getNetDisbursalAmount());
        assertNotNull(result.getCurrency());

        // Verify ignored fields are null
        assertNull(result.getNumberOfRepayments());
        assertNull(result.getLoanRepaymentScheduleInstallments());
        assertNull(result.getWriteOffReasonOptions());
        assertNull(result.getChargeOffReasonOptions());
        assertNull(result.getPaymentTypeOptions());
        assertNull(result.getOverpaymentPortion());
        assertNull(result.getTransfer());
        assertNull(result.getFixedEmiAmount());
        assertNull(result.getPossibleNextRepaymentDate());
        assertNull(result.getAvailableDisbursementAmountWithOverApplied());
        assertNull(result.getRowIndex());
        assertNull(result.getDateFormat());
        assertNull(result.getLocale());
        assertNull(result.getPaymentTypeId());
        assertNull(result.getAccountNumber());
        assertNull(result.getCheckNumber());
        assertNull(result.getRoutingCode());
        assertNull(result.getReceiptNumber());
        assertNull(result.getBankNumber());
        assertNull(result.getAccountId());
        assertNull(result.getTransactionAmount());
    }
}
