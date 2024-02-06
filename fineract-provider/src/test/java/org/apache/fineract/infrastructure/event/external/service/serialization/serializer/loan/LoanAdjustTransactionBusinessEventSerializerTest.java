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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanTransactionDataMapperImpl;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroDateTimeMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.ExternalIdMapper;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargePaidByReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

public class LoanAdjustTransactionBusinessEventSerializerTest {

    @Mock
    private LoanReadPlatformService service;
    @Mock
    private LoanChargePaidByReadPlatformService loanChargePaidByReadPlatformService;
    @Mock
    private AvroDateTimeMapper avroDateTimeMapper;
    @Mock
    private ExternalIdMapper externalIdMapper;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void loanTransactionReversedOnDateSerializationTest() {
        Loan loanForProcessing = Mockito.mock(Loan.class);
        LoanAdjustTransactionBusinessEventSerializer serializer = new LoanAdjustTransactionBusinessEventSerializer(service,
                new LoanTransactionDataMapperImpl(avroDateTimeMapper, externalIdMapper), loanChargePaidByReadPlatformService);
        LoanTransaction transactionToAdjust = Mockito.mock(LoanTransaction.class);
        LoanAdjustTransactionBusinessEvent.Data loanAdjustTransactionBusinessEventData = new LoanAdjustTransactionBusinessEvent.Data(
                transactionToAdjust);
        LocalDate reversedOnDate = LocalDate.now(ZoneId.systemDefault());
        String reversedLocalDate = reversedOnDate.format(DateTimeFormatter.ISO_DATE);
        LoanAdjustTransactionBusinessEvent businessEvent = new LoanAdjustTransactionBusinessEvent(loanAdjustTransactionBusinessEventData);

        LoanTransactionData transactionToAdjustData = new LoanTransactionData(1L, 1L, "", LoanEnumerations.transactionType(2), null, null,
                LocalDate.now(ZoneId.systemDefault()), BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0),
                BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0),
                new ExternalId("testExternalId"), null, null, BigDecimal.valueOf(0.0), LocalDate.now(ZoneId.systemDefault()).minusDays(4),
                true, new ExternalId("testReversalExternalId"), reversedOnDate, 1L, new ExternalId("testExternalLoanId"));

        when(service.retrieveLoanTransaction(anyLong(), anyLong())).thenReturn(transactionToAdjustData);
        when(loanChargePaidByReadPlatformService.getLoanChargesPaidByTransactionId(anyLong())).thenReturn(new ArrayList<>());
        when(transactionToAdjust.getLoan()).thenReturn(loanForProcessing);
        when(loanForProcessing.getId()).thenReturn(1L);
        when(transactionToAdjust.getId()).thenReturn(1L);
        when(avroDateTimeMapper.mapLocalDate(any())).thenReturn(reversedLocalDate);

        LoanTransactionAdjustmentDataV1 loanTransactionAdjustmentDataV1 = (LoanTransactionAdjustmentDataV1) serializer
                .toAvroDTO(businessEvent);
        assertEquals(reversedLocalDate, loanTransactionAdjustmentDataV1.getTransactionToAdjust().getReversedOnDate());
    }
}
