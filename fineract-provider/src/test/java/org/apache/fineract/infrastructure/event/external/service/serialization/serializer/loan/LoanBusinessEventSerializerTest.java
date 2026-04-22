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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.avro.loan.v1.CollectionDataV1;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanAccountDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.ExternalEventCustomDataSerializer;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanSummaryData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryBalancesRepository;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanSummaryDataProvider;
import org.apache.fineract.portfolio.loanaccount.service.LoanSummaryProviderDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LoanBusinessEventSerializerTest {

    @Mock
    private LoanReadPlatformService loanReadPlatformService;
    @Mock
    private LoanAccountDataMapper mapper;
    @Mock
    private LoanChargeReadPlatformService loanChargeReadPlatformService;
    @Mock
    private DelinquencyReadPlatformService delinquencyReadPlatformService;
    @Mock
    private LoanInstallmentLevelDelinquencyEventProducer installmentLevelDelinquencyEventProducer;
    @Mock
    private LoanSummaryBalancesRepository loanSummaryBalancesRepository;
    @Mock
    private LoanSummaryProviderDelegate loanSummaryProviderDelegate;
    @Mock
    private LoanSummaryDataProvider loanSummaryDataProvider;

    private LoanBusinessEventSerializer loanBusinessEventSerializer;

    @BeforeEach
    void setUp() {
        final List<ExternalEventCustomDataSerializer<LoanBusinessEvent>> externalEventCustomDataSerializers = List
                .of(new ExternalEventCustomDataSerializer<>() {

                    @Override
                    public ByteBuffer serialize(final LoanBusinessEvent event) {
                        return ByteBuffer.wrap("test_data_for_loan".getBytes(UTF_8));
                    }

                    @Override
                    public String key() {
                        return "test_key_1";
                    }
                }, new ExternalEventCustomDataSerializer<>() {

                    @Override
                    public ByteBuffer serialize(final LoanBusinessEvent event) {
                        return ByteBuffer.wrap("test_data_for_loan_1".getBytes(UTF_8));
                    }

                    @Override
                    public String key() {
                        return "test_key_1";
                    }
                }, new ExternalEventCustomDataSerializer<>() {

                    @Override
                    public ByteBuffer serialize(final LoanBusinessEvent event) {
                        return ByteBuffer.wrap("test_data_for_loan_2".getBytes(UTF_8));
                    }

                    @Override
                    public String key() {
                        return "test_key_2";
                    }
                });

        loanBusinessEventSerializer = new LoanBusinessEventSerializer(loanReadPlatformService, mapper, loanChargeReadPlatformService,
                delinquencyReadPlatformService, installmentLevelDelinquencyEventProducer, loanSummaryBalancesRepository,
                loanSummaryProviderDelegate, externalEventCustomDataSerializers);
    }

    @Test
    void testLoanCustomDataSerialization() {
        final long loanId = 1L;
        final Loan loan = mock(Loan.class);
        final LoanBusinessEvent event = mock(LoanBusinessEvent.class);
        final LoanAccountData loanAccountData = mock(LoanAccountData.class);
        final CollectionData collectionData = mock(CollectionData.class);
        final CurrencyData currencyData = mock(CurrencyData.class);
        final LoanSummary loanSummary = mock(LoanSummary.class);
        final LoanSummaryData loanSummaryData = mock(LoanSummaryData.class);

        when(loan.getId()).thenReturn(loanId);
        when(event.get()).thenReturn(loan);
        when(loan.getActiveLoanTermVariations()).thenReturn(new ArrayList<>());
        when(loan.getSummary()).thenReturn(loanSummary);

        when(loanAccountData.getCurrency()).thenReturn(currencyData);
        when(loanAccountData.getTransactionProcessingStrategyCode()).thenReturn("strategy-code");
        when(loanAccountData.getSummary()).thenReturn(loanSummaryData);
        when(loanReadPlatformService.retrieveOne(loanId)).thenReturn(loanAccountData);
        when(loanReadPlatformService.fetchRepaymentScheduleData(loanAccountData)).thenReturn(loanAccountData);

        when(delinquencyReadPlatformService.calculateLoanCollectionData(anyLong())).thenReturn(collectionData);
        when(installmentLevelDelinquencyEventProducer.calculateInstallmentLevelDelinquencyData(any(), any())).thenReturn(new ArrayList<>());

        when(loanSummaryProviderDelegate.resolveLoanSummaryDataProvider(any())).thenReturn(loanSummaryDataProvider);
        when(loanSummaryDataProvider.withOnlyCurrencyData(any())).thenReturn(loanSummaryData);

        final CollectionDataV1 collectionDataV1 = CollectionDataV1.newBuilder()
                .setDelinquentDate(LocalDate.now(ZoneId.systemDefault()).toString()).setDelinquentDays(0)
                .setInstallmentDelinquencyBuckets(new ArrayList<>()).build();

        final LoanAccountDataV1 expectedAvroData = LoanAccountDataV1.newBuilder().setId(loanId).setCustomData(new HashMap<>())
                .setDelinquent(collectionDataV1).build();

        when(mapper.map(any(LoanAccountData.class))).thenReturn(expectedAvroData);

        final LoanAccountDataV1 result = (LoanAccountDataV1) loanBusinessEventSerializer.toAvroDTO(event);

        assertNotNull(result);
        assertNotNull(result.getCustomData());
        final Map<String, ByteBuffer> customData = result.getCustomData();
        assertEquals("test_data_for_loan_1", new String(customData.get("test_key_1").array(), UTF_8));
        assertEquals("test_data_for_loan_2", new String(customData.get("test_key_2").array(), UTF_8));
    }

}
