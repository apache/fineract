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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanTransactionDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.ExternalEventCustomDataSerializer;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargePaidByReadService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LoanTransactionBusinessEventSerializerTest {

    @Mock
    private LoanReadPlatformService loanReadPlatformService;
    @Mock
    private LoanTransactionDataMapper loanTransactionMapper;
    @Mock
    private LoanChargePaidByReadService loanChargePaidByReadService;

    private LoanTransactionBusinessEventSerializer serializer;

    @BeforeEach
    void setUp() {
        final List<ExternalEventCustomDataSerializer<LoanTransactionBusinessEvent>> externalEventCustomDataSerializers = List
                .of(new ExternalEventCustomDataSerializer<>() {

                    @Override
                    public ByteBuffer serialize(final LoanTransactionBusinessEvent event) {
                        return ByteBuffer.wrap("test_data_for_loan_transaction".getBytes(UTF_8));
                    }

                    @Override
                    public String key() {
                        return "test_key_1";
                    }
                }, new ExternalEventCustomDataSerializer<>() {

                    @Override
                    public ByteBuffer serialize(final LoanTransactionBusinessEvent event) {
                        return ByteBuffer.wrap("test_data_for_loan_transaction_1".getBytes(UTF_8));
                    }

                    @Override
                    public String key() {
                        return "test_key_1";
                    }
                }, new ExternalEventCustomDataSerializer<>() {

                    @Override
                    public ByteBuffer serialize(final LoanTransactionBusinessEvent event) {
                        return ByteBuffer.wrap("test_data_for_loan_transaction_2".getBytes(UTF_8));
                    }

                    @Override
                    public String key() {
                        return "test_key_2";
                    }
                });
        serializer = new LoanTransactionBusinessEventSerializer(loanReadPlatformService, loanTransactionMapper, loanChargePaidByReadService,
                externalEventCustomDataSerializers);
    }

    @Test
    void testTransactionCustomDataSerialization() {
        final long loanId = 1;
        final long transactionId = 2;

        final Loan loan = mock(Loan.class);
        final LoanTransaction loanTransaction = mock(LoanTransaction.class);
        final LoanTransactionBusinessEvent event = mock(LoanTransactionBusinessEvent.class);
        final LoanTransactionData transactionData = mock(LoanTransactionData.class);

        when(loan.getId()).thenReturn(loanId);
        when(loanTransaction.getId()).thenReturn(transactionId);
        when(loanTransaction.getLoan()).thenReturn(loan);
        when(event.get()).thenReturn(loanTransaction);

        when(loanReadPlatformService.retrieveLoanTransaction(loanId, transactionId)).thenReturn(transactionData);
        when(loanChargePaidByReadService.fetchLoanChargesPaidByDataTransactionId(anyLong())).thenReturn(new ArrayList<>());

        final LoanTransactionDataV1 expectedAvroData = LoanTransactionDataV1.newBuilder().setId(transactionId).setLoanId(loanId)
                .setCustomData(new HashMap<>()).build();
        when(loanTransactionMapper.map(any(LoanTransactionData.class))).thenReturn(expectedAvroData);

        final LoanTransactionDataV1 result = (LoanTransactionDataV1) serializer.toAvroDTO(event);

        assertNotNull(result);
        assertNotNull(result.getCustomData());
        final Map<String, ByteBuffer> customData = result.getCustomData();
        assertEquals("test_data_for_loan_transaction_1", new String(customData.get("test_key_1").array(), UTF_8));
        assertEquals("test_data_for_loan_transaction_2", new String(customData.get("test_key_2").array(), UTF_8));
    }

}
