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
package org.apache.fineract.investor.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.avro.loan.v1.LoanChargeDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DataEnricherProcessor;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.investor.enricher.LoanAccountDataV1Enricher;
import org.apache.fineract.investor.enricher.LoanChargeDataV1Enricher;
import org.apache.fineract.investor.enricher.LoanTransactionDataV1Enricher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EnricherTest {

    private static final LocalDate ACTUAL_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String PURCHASE_PRICE_RATIO = "100.123";
    private static final String SETTLEMENT_DATE = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now(ZoneId.systemDefault()));

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, ACTUAL_DATE)));

    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testLoanAccountDataEnricher() {
        LoanAccountDataV1Enricher loanAccountDataV1Enricher = mock(LoanAccountDataV1Enricher.class);
        when(loanAccountDataV1Enricher.isDataTypeSupported(any())).thenCallRealMethod();
        doAnswer(a -> {
            LoanAccountDataV1 data = a.getArgument(0, LoanAccountDataV1.class);
            data.setExternalOwnerId("1");
            data.setSettlementDate(SETTLEMENT_DATE);
            data.setPurchasePriceRatio(PURCHASE_PRICE_RATIO);
            return null;
        }).when(loanAccountDataV1Enricher).enrich(any(LoanAccountDataV1.class));

        DataEnricherProcessor dataEnricherProcessor = new DataEnricherProcessor(Optional.of(Arrays.asList(loanAccountDataV1Enricher)));

        LoanAccountDataV1 original = new LoanAccountDataV1();
        dataEnricherProcessor.enrich(original);

        verify(loanAccountDataV1Enricher, times(1)).enrich(any(LoanAccountDataV1.class));

        assertEquals("1", original.getExternalOwnerId());
        assertEquals(SETTLEMENT_DATE, original.getSettlementDate());
        assertEquals(PURCHASE_PRICE_RATIO, original.getPurchasePriceRatio());
    }

    @Test
    public void testLoanChargeDataEnricher() {
        LoanChargeDataV1Enricher loanChargeDataV1Enricher = mock(LoanChargeDataV1Enricher.class);
        when(loanChargeDataV1Enricher.isDataTypeSupported(any())).thenCallRealMethod();
        doAnswer(a -> {
            LoanChargeDataV1 data = a.getArgument(0, LoanChargeDataV1.class);
            data.setExternalOwnerId("2");
            return null;
        }).when(loanChargeDataV1Enricher).enrich(any(LoanChargeDataV1.class));

        DataEnricherProcessor dataEnricherProcessor = new DataEnricherProcessor(Optional.of(Arrays.asList(loanChargeDataV1Enricher)));

        LoanChargeDataV1 data = new LoanChargeDataV1();
        dataEnricherProcessor.enrich(data);

        verify(loanChargeDataV1Enricher, times(1)).enrich(any(LoanChargeDataV1.class));

        assertEquals("2", data.getExternalOwnerId());

    }

    @Test
    public void testLoanTransactionDataEnricher() {
        LoanTransactionDataV1Enricher loanTransactionDataV1Enricher = mock(LoanTransactionDataV1Enricher.class);
        when(loanTransactionDataV1Enricher.isDataTypeSupported(any())).thenCallRealMethod();
        doAnswer(a -> {
            LoanTransactionDataV1 data = a.getArgument(0, LoanTransactionDataV1.class);
            data.setExternalOwnerId("3");
            return null;
        }).when(loanTransactionDataV1Enricher).enrich(any(LoanTransactionDataV1.class));

        DataEnricherProcessor dataEnricherProcessor = new DataEnricherProcessor(Optional.of(Arrays.asList(loanTransactionDataV1Enricher)));

        LoanTransactionDataV1 data = new LoanTransactionDataV1();
        dataEnricherProcessor.enrich(data);

        verify(loanTransactionDataV1Enricher, times(1)).enrich(any(LoanTransactionDataV1.class));

        assertEquals("3", data.getExternalOwnerId());
    }

    @Test
    public void testAllEventEnricherWorksCorrectly() {
        LoanAccountDataV1Enricher loanAccountDataV1Enricher = mock(LoanAccountDataV1Enricher.class);
        when(loanAccountDataV1Enricher.isDataTypeSupported(any())).thenCallRealMethod();
        LoanChargeDataV1Enricher loanChargeDataV1Enricher = mock(LoanChargeDataV1Enricher.class);
        when(loanChargeDataV1Enricher.isDataTypeSupported(any())).thenCallRealMethod();
        LoanTransactionDataV1Enricher loanTransactionDataV1Enricher = mock(LoanTransactionDataV1Enricher.class);
        when(loanTransactionDataV1Enricher.isDataTypeSupported(any())).thenCallRealMethod();

        doAnswer(a -> {
            LoanAccountDataV1 data = a.getArgument(0, LoanAccountDataV1.class);
            data.setExternalOwnerId("1");
            data.setSettlementDate(SETTLEMENT_DATE);
            data.setPurchasePriceRatio(PURCHASE_PRICE_RATIO);
            return null;
        }).when(loanAccountDataV1Enricher).enrich(any(LoanAccountDataV1.class));

        lenient().doAnswer(a -> {
            LoanChargeDataV1 data = a.getArgument(0, LoanChargeDataV1.class);
            data.setExternalOwnerId("2");
            return null;
        }).when(loanChargeDataV1Enricher).enrich(any(LoanChargeDataV1.class));

        lenient().doAnswer(a -> {
            LoanTransactionDataV1 data = a.getArgument(0, LoanTransactionDataV1.class);
            data.setExternalOwnerId("3");
            return null;
        }).when(loanTransactionDataV1Enricher).enrich(any(LoanTransactionDataV1.class));

        DataEnricherProcessor dataEnricherProcessor = new DataEnricherProcessor(
                Optional.of(Arrays.asList(loanAccountDataV1Enricher, loanChargeDataV1Enricher, loanTransactionDataV1Enricher)));

        LoanAccountDataV1 data = new LoanAccountDataV1();
        dataEnricherProcessor.enrich(data);

        verify(loanAccountDataV1Enricher, times(1)).enrich(any(LoanAccountDataV1.class));
        verify(loanChargeDataV1Enricher, times(0)).enrich(any(LoanChargeDataV1.class));
        verify(loanTransactionDataV1Enricher, times(0)).enrich(any(LoanTransactionDataV1.class));
        verify(loanAccountDataV1Enricher, times(1)).isDataTypeSupported(any());
        verify(loanChargeDataV1Enricher, times(1)).isDataTypeSupported(any());
        verify(loanTransactionDataV1Enricher, times(1)).isDataTypeSupported(any());

        assertEquals("1", data.getExternalOwnerId());
        assertEquals(SETTLEMENT_DATE, data.getSettlementDate());
        assertEquals(PURCHASE_PRICE_RATIO, data.getPurchasePriceRatio());

    }
}
