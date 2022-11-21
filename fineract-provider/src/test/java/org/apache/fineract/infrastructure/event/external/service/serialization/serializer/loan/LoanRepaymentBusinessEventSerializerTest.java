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
package org.apache.fineract.infrastructure.event.external.service.serialization.serializer.loan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.fineract.avro.generic.v1.CurrencyDataV1;
import org.apache.fineract.avro.loan.v1.LoanRepaymentDueDataV1;
import org.apache.fineract.avro.loan.v1.RepaymentDueDataV1;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.repayment.LoanRepaymentDueBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroDateTimeMapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanRepaymentBusinessEventSerializerTest {

    @Mock
    private AvroDateTimeMapper mapper;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
    }

    @Test
    public void testLoanRepaymentEventPayloadSerialization() throws IOException {
        // given
        LoanRepaymentBusinessEventSerializer serializer = new LoanRepaymentBusinessEventSerializer(mapper);

        LocalDate loanInstallmentRepaymentDueDate = DateUtils.getBusinessLocalDate().plusDays(1);

        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanProduct loanProduct = Mockito.mock(LoanProduct.class);
        LoanSummary loanSummary = Mockito.mock(LoanSummary.class);
        MonetaryCurrency loanCurrency = Mockito.mock(MonetaryCurrency.class);
        MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);

        LoanRepaymentScheduleInstallment repaymentInstallment = new LoanRepaymentScheduleInstallment(loanForProcessing, 1,
                LocalDate.now(ZoneId.systemDefault()), loanInstallmentRepaymentDueDate, BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), false, new HashSet<>(), BigDecimal.valueOf(0.0));
        LoanRepaymentDueBusinessEvent event = new LoanRepaymentDueBusinessEvent(repaymentInstallment);

        when(loanForProcessing.getId()).thenReturn(1L);
        when(loanForProcessing.getAccountNumber()).thenReturn("0001");
        when(loanForProcessing.getExternalId()).thenReturn("externalId");
        when(loanForProcessing.getLoanSummary()).thenReturn(loanSummary);
        when(loanSummary.getTotalOutstanding()).thenReturn(BigDecimal.valueOf(0.0));
        when(loanForProcessing.getCurrency()).thenReturn(loanCurrency);
        when(loanCurrency.getCode()).thenReturn("CODE");
        when(loanCurrency.getCurrencyInMultiplesOf()).thenReturn(1);
        when(loanCurrency.getDigitsAfterDecimal()).thenReturn(1);
        when(mapper.mapLocalDate(any())).thenReturn(loanInstallmentRepaymentDueDate.format(DateTimeFormatter.ISO_DATE));
        moneyHelper.when(() -> MoneyHelper.getRoundingMode()).thenReturn(RoundingMode.UP);

        // when
        LoanRepaymentDueDataV1 data = (LoanRepaymentDueDataV1) serializer.toAvroDTO(event);

        // then
        CurrencyDataV1 currency = CurrencyDataV1.newBuilder().setCode("CODE").setDecimalPlaces(1).setInMultiplesOf(1).build();
        RepaymentDueDataV1 repaymentDue = new RepaymentDueDataV1(1, BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0));
        LoanRepaymentDueDataV1 expectedSerializedData = new LoanRepaymentDueDataV1(1L, "0001", "externalId",
                loanInstallmentRepaymentDueDate.format(DateTimeFormatter.ISO_DATE), currency, BigDecimal.valueOf(0.0), repaymentDue);

        assertEquals(data, expectedSerializedData);
        moneyHelper.close();
    }
}
