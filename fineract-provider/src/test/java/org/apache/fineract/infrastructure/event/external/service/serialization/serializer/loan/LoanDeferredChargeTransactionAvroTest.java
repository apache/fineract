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

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionEnumDataV1;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanTransactionDataMapperImpl;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroDateTimeMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.ExternalIdMapper;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LoanDeferredChargeTransactionAvroTest {

    private final LoanTransactionDataMapperImpl mapper = new LoanTransactionDataMapperImpl(new AvroDateTimeMapper(),
            new ExternalIdMapper());

    @ParameterizedTest
    @EnumSource(value = LoanTransactionType.class, names = { "DEFERRED_CHARGE", "DEFERRED_CHARGE_AMORTIZATION",
            "DEFERRED_CHARGE_AMORTIZATION_ADJUSTMENT" })
    void deferredChargeTypeFlagsMapIntoAvroSchema(final LoanTransactionType type) {
        final LoanTransactionData transactionData = LoanTransactionData.builder().id(1L).loanId(2L)
                .type(LoanEnumerations.transactionType(type)).build();

        final LoanTransactionDataV1 avro = mapper.map(transactionData);
        final LoanTransactionEnumDataV1 typeAvro = avro.getType();

        assertEquals(type.getValue().longValue(), typeAvro.getId().longValue());
        assertEquals((type == LoanTransactionType.DEFERRED_CHARGE), Boolean.TRUE.equals(typeAvro.getDeferredCharge()));
        assertEquals((type == LoanTransactionType.DEFERRED_CHARGE_AMORTIZATION),
                Boolean.TRUE.equals(typeAvro.getDeferredChargeAmortization()));
        assertEquals((type == LoanTransactionType.DEFERRED_CHARGE_AMORTIZATION_ADJUSTMENT),
                Boolean.TRUE.equals(typeAvro.getDeferredChargeAmortizationAdjustment()));
    }

    @Test
    void deferredChargeEnumDataRoundTripsThroughAvroByteBuffer() throws IOException {
        final LoanTransactionEnumDataV1 original = LoanTransactionEnumDataV1.newBuilder().setId(50)
                .setCode("loanTransactionType.deferredCharge").setValue("Deferred Charge").setDeferredCharge(true)
                .setDeferredChargeAmortization(false).setDeferredChargeAmortizationAdjustment(false).build();

        final ByteBuffer buffer = original.toByteBuffer();
        final LoanTransactionEnumDataV1 roundTrip = LoanTransactionEnumDataV1.fromByteBuffer(buffer);

        assertEquals(Boolean.TRUE, roundTrip.getDeferredCharge());
        assertEquals(original.getId(), roundTrip.getId());
        assertEquals(original.getCode(), roundTrip.getCode());
    }
}
